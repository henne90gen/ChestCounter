package de.henne90gen.chestcounter.event;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.henne90gen.chestcounter.ChestCounter;
import de.henne90gen.chestcounter.Helper;
import de.henne90gen.chestcounter.Renderer;
import de.henne90gen.chestcounter.service.dtos.Chest;
import de.henne90gen.chestcounter.service.dtos.ChestSearchResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ToggleWidget;
import net.minecraft.inventory.container.Container;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ChestGuiEventHandler {

    private static final Logger LOGGER = LogManager.getLogger();

    private final ChestCounter mod;

    private Chest currentChest = null;

    private TextFieldWidget searchField = null;
    private ToggleWidget searchToggle = null;
    private ChestSearchResult lastSearchResult = null;

    private TextFieldWidget labelField = null;

    public ChestGuiEventHandler(ChestCounter mod) {
        this.mod = mod;
    }

    @SubscribeEvent
    public void initGui(GuiScreenEvent.InitGuiEvent.Post event) {
        if (shouldNotHandleGuiEvent(event)) {
            return;
        }

        ContainerScreen<?> screen = (ContainerScreen<?>) event.getGui();
        addSearchFieldToScreen(event, screen);
        addSearchToggleToScreen(event, screen);

        if (event.getGui() instanceof ChestScreen) {
            addChestLabelToScreen(event, screen);
        }

        search();
    }

    @SubscribeEvent
    public void keyPressed(GuiScreenEvent.KeyboardKeyPressedEvent.Pre event) {
        if (shouldNotHandleGuiEvent(event)) {
            return;
        }
        LOGGER.info("KeyPressed: " + event.getGui().getClass());

        keyPressedOnTextField(event, searchField);
        if (event.isCanceled()) {
            search();
            return;
        }

        if (event.getGui() instanceof ChestScreen) {
            keyPressedOnTextField(event, labelField);
            if (event.isCanceled()) {
                if (currentChest != null) {
                    mod.chestService.updateLabel(currentChest.worldId, currentChest.id, labelField.getText());
                }
                search();
            }
        }
    }

    @SubscribeEvent
    public void charTyped(GuiScreenEvent.KeyboardCharTypedEvent.Pre event) {
        if (shouldNotHandleGuiEvent(event)) {
            return;
        }

        if (event.getGui() instanceof ChestScreen && currentChest != null) {
            event.setCanceled(labelField.charTyped(event.getCodePoint(), event.getModifiers()));
            mod.chestService.updateLabel(currentChest.worldId, currentChest.id, labelField.getText());
            if (event.isCanceled()) {
                String text = labelField.getText();
                labelField.setText(text.substring(0, text.length() - 1));
            }
        }

        event.setCanceled(searchField.charTyped(event.getCodePoint(), event.getModifiers()));

        search();
    }

    @SubscribeEvent
    public void mouseClicked(GuiScreenEvent.MouseClickedEvent.Pre event) {
        if (shouldNotHandleGuiEvent(event)) {
            return;
        }

        if (searchField != null) {
            searchField.mouseClicked(event.getMouseX(), event.getMouseY(), event.getButton());
        }
        if (searchToggle != null) {
            searchToggle.mouseClicked(event.getMouseX(), event.getMouseY(), event.getButton());
        }
        if (labelField != null) {
            labelField.mouseClicked(event.getMouseX(), event.getMouseY(), event.getButton());
        }
    }

    @SubscribeEvent
    public void mouseReleased(GuiScreenEvent.MouseReleasedEvent.Pre event) {
        if (shouldNotHandleGuiEvent(event)) {
            return;
        }

        if (searchField != null) {
            searchField.mouseReleased(event.getMouseX(), event.getMouseY(), event.getButton());
        }
        if (searchToggle != null) {
            if (searchToggle.mouseReleased(event.getMouseX(), event.getMouseY(), event.getButton()) &&
                    searchToggle.isMouseOver(event.getMouseX(), event.getMouseY())) {
                boolean triggered = !searchToggle.isStateTriggered();
                searchToggle.setStateTriggered(triggered);
            }
        }
        if (labelField != null) {
            labelField.mouseReleased(event.getMouseX(), event.getMouseY(), event.getButton());
        }

        // this only works for picking up items, not for dropping them back into the inventory
        saveCurrentChest(event);
        search();
    }

    @SubscribeEvent
    public void renderSearchResult(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (shouldNotHandleGuiEvent(event)) {
            return;
        }

        if (lastSearchResult == null) {
            return;
        }

        boolean byId = false;
        if (searchToggle != null) {
            byId = !searchToggle.isStateTriggered();
        }
        ContainerScreen<?> screen = (ContainerScreen<?>) event.getGui();
        Renderer.renderSearchResult(lastSearchResult, byId, screen);
    }

    @SubscribeEvent
    public void blockClicked(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getWorld().isRemote()) {
            return;
        }

        BlockPos pos = event.getPos();
        World world = event.getWorld();
        TileEntity tileEntity = world.getTileEntity(pos);
        if (!(tileEntity instanceof ChestTileEntity)) {
            return;
        }

        String worldId = Helper.getWorldID();
        String chestId = Helper.getChestId(world, pos);
        currentChest = mod.chestService.getChest(worldId, chestId);
        LOGGER.debug("Setting current chest to: " + currentChest.label + "(" + currentChest.id + ")");
    }

    @SubscribeEvent
    public void render(RenderWorldLastEvent event) {
        List<Chest> chests = mod.chestService.getChests(Helper.getWorldID());

        if (chests == null) {
            return;
        }

        float maxDistance = 10.0F;
        float partialTickTime = event.getPartialTicks();
        MatrixStack matrixStack = event.getMatrixStack();
        Renderer.renderChestLabels(chests, lastSearchResult, maxDistance, partialTickTime, matrixStack);
    }

    private boolean shouldNotHandleGuiEvent(GuiScreenEvent event) {
        return !(event.getGui() instanceof ContainerScreen) || event.getGui() instanceof CreativeScreen;
    }

    private void saveCurrentChest(GuiScreenEvent event) {
        if (currentChest == null || !(event.getGui() instanceof ChestScreen)) {
            return;
        }

        Container currentContainer = ((ChestScreen) event.getGui()).getContainer();
        currentChest.items = Helper.countItems(Helper.inventoryIterator(currentContainer));
        mod.chestService.save(currentChest);
    }

    private void addSearchToggleToScreen(GuiScreenEvent.InitGuiEvent.Post event, ContainerScreen<?> screen) {
        int guiLeft = screen.getGuiLeft();
        int xSize = screen.getXSize();

        int x = guiLeft + xSize + Renderer.MARGIN;
        searchToggle = new ToggleWidget(
                x - 20, 2,
                12, 15,
                false
        );
        final ResourceLocation RECIPE_BOOK = new ResourceLocation("textures/gui/recipe_book.png");
        searchToggle.initTextureValues(152, 41, 28, 18, RECIPE_BOOK);
        searchToggle.setStateTriggered(true);
        event.addWidget(searchToggle);
    }

    private void addChestLabelToScreen(GuiScreenEvent.InitGuiEvent.Post event, ContainerScreen<?> screen) {
        int guiLeft = screen.getGuiLeft();
        int guiTop = screen.getGuiTop();
        ChestScreen chestScreen = (ChestScreen) event.getGui();
        int width = 128;
        int numRows = chestScreen.getContainer().getNumRows();
        int offsetLeft = 40;
        if (numRows == 6) {
            // large chest
            offsetLeft = 75;
            width = 93;
        }
        int offsetTop = 5;
        labelField = new TextFieldWidget(
                Minecraft.getInstance().fontRenderer,
                guiLeft + offsetLeft, guiTop + offsetTop,
                width, 10,
                "Chest Label"
        );
        if (currentChest != null && currentChest.label != null) {
            labelField.setText(currentChest.label);
        }
        event.addWidget(labelField);
    }

    private void addSearchFieldToScreen(GuiScreenEvent.InitGuiEvent.Post event, ContainerScreen<?> screen) {
        int guiLeft = screen.getGuiLeft();
        int xSize = screen.getXSize();

        int x = guiLeft + xSize + Renderer.MARGIN;
        searchField = new TextFieldWidget(
                Minecraft.getInstance().fontRenderer,
                x, 2,
                100, 10,
                "Search"
        );
        if (lastSearchResult != null && lastSearchResult.search != null) {
            searchField.setText(lastSearchResult.search);
        }
        event.addWidget(searchField);
    }

    private void search() {
        if (searchField == null) {
            return;
        }
        lastSearchResult = mod.chestService.getItemCounts(Helper.getWorldID(), searchField.getText());
    }

    private void keyPressedOnTextField(GuiScreenEvent.KeyboardKeyPressedEvent.Pre event, TextFieldWidget textField) {
        event.setCanceled(textField.keyPressed(event.getKeyCode(), event.getScanCode(), event.getModifiers()));
        boolean disallowedKeyPressed = event.getKeyCode() == 69/*e*/ || event.getKeyCode() == 76/*l*/;
        if (disallowedKeyPressed && textField.isFocused()) {
            event.setCanceled(true);
        }
    }
}
