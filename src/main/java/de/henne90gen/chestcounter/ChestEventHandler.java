package de.henne90gen.chestcounter;

import com.sun.jna.platform.unix.X11;
import de.henne90gen.chestcounter.dtos.AmountResult;
import de.henne90gen.chestcounter.dtos.Chest;
import de.henne90gen.chestcounter.dtos.ChestContent;
import de.henne90gen.chestcounter.dtos.Chests;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.util.*;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ChestEventHandler {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final int INVENTORY_SIZE = 36;

    private static final int MARGIN = 5;

    private final ChestCounter mod;
    private List<BlockPos> chestPositions;
    private Chest chest;

    private TextFieldWidget searchField = null;
    private Map<String, AmountResult> lastSearchResult = null;

    private TextFieldWidget labelField = null;

    public ChestEventHandler(ChestCounter mod) {
        this.mod = mod;
        this.chestPositions = new ArrayList<>();
    }

    @SubscribeEvent
    public void worldLoaded(WorldEvent.Load event) {
        IntegratedServer integratedServer = Minecraft.getInstance().getIntegratedServer();
        mod.registerCommands(integratedServer);
    }

    @SubscribeEvent
    public void close(GuiOpenEvent event) {
        if (event.getGui() == null && chest != null) {
            Helper.instance.runInThread(() -> {
                mod.chestService.save(chest);
                chest = null;
                chestPositions.clear();
            });
        }
    }

    @SubscribeEvent
    public void guiIsOpen(GuiContainerEvent event) {
        if (shouldNotHandleEvent(event)) {
            return;
        }

        Minecraft mc = event.getGuiContainer().getMinecraft();
        if (mc.currentScreen instanceof ChestScreen) {
            Container currentContainer = ((ChestScreen) mc.currentScreen).getContainer();

            chest = new Chest();
            chest.worldID = Helper.instance.getWorldID();
            chest.id = Helper.instance.createChestID(chestPositions);
            chest.chestContent = countItems(currentContainer);
            chest.chestContent.label = Helper.instance.createDefaultLabel(chestPositions);
        }
    }

    private boolean shouldNotHandleEvent(GuiContainerEvent event) {
        return chestPositions.isEmpty() || event.getGuiContainer() == null ||
                event.getGuiContainer().getMinecraft().world == null ||
                !event.getGuiContainer().getMinecraft().world.isRemote();
    }

    private ChestContent countItems(Container currentContainer) {
        Map<String, Integer> counter = new LinkedHashMap<>();
        for (int i = 0; i < currentContainer.inventorySlots.size() - INVENTORY_SIZE; i++) {
            ItemStack stack = currentContainer.inventorySlots.get(i).getStack();
            String itemName = stack.getDisplayName().getString();
            if ("Air".equals(itemName)) {
                continue;
            }
            Integer currentCount = counter.get(itemName);
            if (currentCount == null) {
                currentCount = 0;
            }
            currentCount += stack.getCount();
            counter.put(itemName, currentCount);
        }
        ChestContent chestContent = new ChestContent();
        chestContent.items = counter;
        return chestContent;
    }

    @SubscribeEvent
    public void interact(PlayerInteractEvent event) {
        if (!event.getWorld().isRemote()) {
            return;
        }
        chestPositions = Helper.instance.getChestPositions(event.getWorld(), event.getPos());
    }

    @SubscribeEvent
    public void harvestBlock(BlockEvent.BreakEvent event) {
        if (!event.getWorld().isRemote()) {
            return;
        }

        TileEntity tileEntity = event.getWorld().getTileEntity(event.getPos());
        if (tileEntity instanceof ChestTileEntity) {
            Chest chestToDelete = new Chest();
            chestToDelete.worldID = Helper.instance.getWorldID();
            chestToDelete.id = Helper.instance.createChestID(Collections.singletonList(event.getPos()));

            ChestContent chestContent = mod.chestService.searchForChest(chestToDelete);

            mod.chestService.delete(chestToDelete);

            if (chestContent == null) {
                return;
            }
            BlockPos[] positions = {
                    event.getPos().north(),
                    event.getPos().east(),
                    event.getPos().south(),
                    event.getPos().west()
            };
            for (BlockPos position : positions) {
                TileEntity entity = event.getWorld().getTileEntity(position);
                if (entity instanceof ChestTileEntity) {
                    Chest chest = new Chest();
                    chest.worldID = Helper.instance.getWorldID();
                    chest.id = Helper.instance.createChestID(Collections.singletonList(position));
                    chest.chestContent.label = chestContent.label;
                    Helper.instance.runInThread(() -> mod.chestService.save(chest));
                    break;
                }
            }
        }
    }

    @SubscribeEvent
    public void place(BlockEvent.EntityPlaceEvent event) {
        if (!event.getWorld().isRemote()) {
            return;
        }

        TileEntity tileEntity = event.getWorld().getTileEntity(event.getPos());
        if (tileEntity instanceof ChestTileEntity) {
            Chest chest = new Chest();
            chest.worldID = Helper.instance.getWorldID();
            List<BlockPos> chestPositions = Helper.instance.getChestPositions(event.getWorld(), event.getPos());
            chest.id = Helper.instance.createChestID(chestPositions);
            chest.chestContent.label = chest.id;
            Helper.instance.runInThread(() -> mod.chestService.save(chest));
        }
    }

    @SubscribeEvent
    public void initGui(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.getGui() instanceof ContainerScreen) {
            LOGGER.info("Opened inventory (" + event.getGui().getClass() + ")");

            int guiLeft = ((ContainerScreen) event.getGui()).getGuiLeft();
            int guiTop = ((ContainerScreen) event.getGui()).getGuiTop();
            int xSize = ((ContainerScreen) event.getGui()).getXSize();

            int x = guiLeft + xSize + MARGIN;
            searchField = new TextFieldWidget(
                    Minecraft.getInstance().fontRenderer,
                    x, guiTop,
                    100, 15,
                    "Search"
            );
            event.addWidget(searchField);

            if (event.getGui() instanceof ChestScreen) {
                LOGGER.info("Opened chest");
                int width = 100;
                labelField = new TextFieldWidget(
                        Minecraft.getInstance().fontRenderer,
                        guiLeft - width, guiTop,
                        width, 15,
                        "Chest Label"
                );
                event.addWidget(labelField);
            }
        }
    }

    @SubscribeEvent
    public void keyPressed(GuiScreenEvent.KeyboardKeyPressedEvent.Pre event) {
        if (event.getGui() instanceof ContainerScreen) {
            event.setCanceled(searchField.keyPressed(event.getKeyCode(), event.getScanCode(), event.getModifiers()));
            if (event.getKeyCode() == 69 /*e*/ && searchField.isFocused()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void charTyped(GuiScreenEvent.KeyboardCharTypedEvent.Pre event) {
        if (event.getGui() instanceof ContainerScreen) {
            event.setCanceled(searchField.charTyped(event.getCodePoint(), event.getModifiers()));
            lastSearchResult = mod.chestService.getItemCounts(Helper.instance.getWorldID(), searchField.getText());
        }
    }

    @SubscribeEvent
    public void renderGui(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (lastSearchResult == null) {
            return;
        }

        if (!(event.getGui() instanceof ContainerScreen)) {
            return;
        }

        int guiLeft = ((ContainerScreen) event.getGui()).getGuiLeft();
        int guiTop = ((ContainerScreen) event.getGui()).getGuiTop();
        int xSize = ((ContainerScreen) event.getGui()).getXSize();

        int RESULT_MARGIN = 25;
        int currentY = guiTop + RESULT_MARGIN;
        for (Map.Entry<String, AmountResult> entry : lastSearchResult.entrySet()) {
            Minecraft.getInstance().fontRenderer.drawString(entry.getKey(), guiLeft + xSize + MARGIN, currentY, 0xffffff);
            currentY += MARGIN * 2;
            // TODO render full search result
        }
    }

    @SubscribeEvent
    public void render(RenderWorldLastEvent event) {
        Chests chests = mod.chestService.getChests(Helper.instance.getWorldID());

        if (chests == null) {
            return;
        }

        int color = 0xFFFFFF;
        float partialTickTime = event.getPartialTicks();
        float maxDistance = 10.0F;

        for (Map.Entry<String, ChestContent> entry : chests.entrySet()) {
            ChestContent content = entry.getValue();
            String[] text = {content.label};
            BlockPos pos = Helper.instance.getBlockPosFromChestID(entry.getKey());
            renderText(text, pos.getX(), pos.getY(), pos.getZ(), maxDistance, color, partialTickTime);
        }
    }

    private void renderText(String[] text, float x, float y, float z, float maxDistance, int color, float partialTickTime) {
        // TODO refactor this into a different class
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        float playerX = (float) (mc.player.lastTickPosX + (mc.player.getPosX() - mc.player.lastTickPosX) * partialTickTime);
        float playerY = (float) (mc.player.lastTickPosY + (mc.player.getPosY() - mc.player.lastTickPosY) * partialTickTime);
        float playerZ = (float) (mc.player.lastTickPosZ + (mc.player.getPosZ() - mc.player.lastTickPosZ) * partialTickTime);

        float dx = (x - playerX) + 0.5F;
        float dy = (y - playerY) + 0.5F;
        float dz = (z - playerZ) + 0.5F;
        float distance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (distance >= maxDistance) {
            return;
        }

        float scale = 0.03f;
        GL11.glColor4f(1f, 1f, 1f, 0.5f);
        GL11.glPushMatrix();
        GL11.glTranslatef(dx, dy, dz);
        GL11.glRotatef(-mc.player.cameraYaw, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(mc.player.cameraYaw, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(-scale, -scale, scale);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        int textWidth = 0;
        for (String thisMessage : text) {
            int thisMessageWidth = mc.fontRenderer.getStringWidth(thisMessage);

            if (thisMessageWidth > textWidth)
                textWidth = thisMessageWidth;
        }

        int lineHeight = 10;
        int i = 0;
        for (String message : text) {
            mc.fontRenderer.drawString(message, -textWidth / 2, i * lineHeight, color);
            i++;
        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glPopMatrix();
    }
}
