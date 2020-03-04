package de.henne90gen.chestcounter;

import java.util.*;

import de.henne90gen.chestcounter.dtos.Chest;
import de.henne90gen.chestcounter.dtos.ChestContent;
import de.henne90gen.chestcounter.dtos.Chests;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Color;

public class ChestEventHandler {

    private static final int INVENTORY_SIZE = 36;

    private static Minecraft mc = Minecraft.getMinecraft();

    private final ChestCounter mod;

    private List<BlockPos> chestPositions;

    private Chest chest;

    public ChestEventHandler(ChestCounter mod) {
        this.mod = mod;
        this.chestPositions = new ArrayList<>();
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
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
    @SideOnly(Side.CLIENT)
    public void guiIsOpen(GuiContainerEvent event) {
        if (shouldNotHandleEvent(event)) {
            return;
        }

        Minecraft mc = FMLClientHandler.instance().getClient();
        if (mc.currentScreen instanceof GuiContainer) {
            Container currentContainer = ((GuiContainer) mc.currentScreen).inventorySlots;

            chest = new Chest();
            chest.worldID = Helper.instance.getWorldID();
            chest.id = Helper.instance.createChestID(chestPositions);
            chest.chestContent = countItems(currentContainer);
            chest.chestContent.label = Helper.instance.createDefaultLabel(chestPositions);
        }
    }

    private boolean shouldNotHandleEvent(GuiContainerEvent event) {
        return chestPositions.isEmpty() || event.getGuiContainer() == null ||
                event.getGuiContainer().mc == null ||
                event.getGuiContainer().mc.world == null ||
                !event.getGuiContainer().mc.world.isRemote;
    }

    private ChestContent countItems(Container currentContainer) {
        Map<String, Integer> counter = new LinkedHashMap<>();
        for (int i = 0; i < currentContainer.inventorySlots.size() - INVENTORY_SIZE; i++) {
            ItemStack stack = currentContainer.inventorySlots.get(i).getStack();
            String itemName = stack.getDisplayName();
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
    @SideOnly(Side.CLIENT)
    public void interact(PlayerInteractEvent event) {
        chestPositions = Helper.instance.getChestPositions(event.getWorld(), event.getPos());
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void harvestBlock(BlockEvent.BreakEvent event) {
        TileEntity tileEntity = event.getWorld().getTileEntity(event.getPos());
        if (tileEntity instanceof TileEntityChest) {
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
                if (entity instanceof TileEntityChest) {
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
    @SideOnly(Side.CLIENT)
    public void place(BlockEvent.PlaceEvent event) {
        TileEntity tileEntity = event.getWorld().getTileEntity(event.getPos());
        if (tileEntity instanceof TileEntityChest) {
            Chest chest = new Chest();
            chest.worldID = Helper.instance.getWorldID();
            List<BlockPos> chestPositions = Helper.instance.getChestPositions(event.getWorld(), event.getPos());
            chest.id = Helper.instance.createChestID(chestPositions);
            chest.chestContent.label = chest.id;
            Helper.instance.runInThread(() -> mod.chestService.save(chest));
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void render(RenderWorldLastEvent event) {
        Chests chests = mod.chestService.getChests(Helper.instance.getWorldID());

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

        RenderManager renderManager = mc.getRenderManager();

        float playerX = (float) (mc.player.lastTickPosX + (mc.player.posX - mc.player.lastTickPosX) * partialTickTime);
        float playerY = (float) (mc.player.lastTickPosY + (mc.player.posY - mc.player.lastTickPosY) * partialTickTime);
        float playerZ = (float) (mc.player.lastTickPosZ + (mc.player.posZ - mc.player.lastTickPosZ) * partialTickTime);

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
        GL11.glRotatef(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
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
