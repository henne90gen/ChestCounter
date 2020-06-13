package de.henne90gen.chestcounter.event;

import de.henne90gen.chestcounter.ChestCounter;
import de.henne90gen.chestcounter.db.entities.ChestConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class GuiLabelEventHandler extends GuiEventHandler {

    private static final Logger LOGGER = LogManager.getLogger();

    private final ChestCounter mod;

    private TextFieldWidget labelField = null;

    public GuiLabelEventHandler(ChestCounter mod) {
        this.mod = mod;
    }

    @SubscribeEvent
    public void initGui(GuiScreenEvent.InitGuiEvent.Post event) {
        if (!(event.getGui() instanceof ContainerScreen)
                || event.getGui() instanceof CreativeScreen) {
            return;
        }

        ContainerScreen<?> screen = (ContainerScreen<?>) event.getGui();
        if (event.getGui() instanceof ChestScreen) {
            addChestLabelToScreen(event, screen);
        }
    }

    @SubscribeEvent
    public void keyPressed(GuiScreenEvent.KeyboardKeyPressedEvent.Pre event) {
        ChestConfig config = mod.chestService.getConfig();
        if (shouldNotHandleGuiEvent(event, config)) {
            return;
        }

        if (!(event.getGui() instanceof ChestScreen)) {
            return;
        }

        keyPressedOnTextField(event, labelField);
        if (!event.isCanceled()) {
            return;
        }

        if (mod.currentChest != null) {
            mod.chestService.updateLabel(mod.currentChest.worldId, mod.currentChest.id, labelField.getText());
        }
        mod.search();
    }

    @SubscribeEvent
    public void charTyped(GuiScreenEvent.KeyboardCharTypedEvent.Pre event) {
        ChestConfig config = mod.chestService.getConfig();
        if (shouldNotHandleGuiEvent(event, config)) {
            return;
        }

        if (!(event.getGui() instanceof ChestScreen) || mod.currentChest == null) {
            return;
        }

        event.setCanceled(labelField.charTyped(event.getCodePoint(), event.getModifiers()));
        mod.chestService.updateLabel(mod.currentChest.worldId, mod.currentChest.id, labelField.getText());
        if (event.isCanceled()) {
            String text = labelField.getText();
            labelField.setText(text);
        }
    }

    @SubscribeEvent
    public void mouseClicked(GuiScreenEvent.MouseClickedEvent.Pre event) {
        ChestConfig config = mod.chestService.getConfig();
        if (shouldNotHandleGuiEvent(event, config)) {
            return;
        }

        if (labelField == null) {
            return;
        }

        labelField.mouseClicked(event.getMouseX(), event.getMouseY(), event.getButton());
    }

    @SubscribeEvent
    public void mouseReleased(GuiScreenEvent.MouseReleasedEvent.Pre event) {
        ChestConfig config = mod.chestService.getConfig();
        if (shouldNotHandleGuiEvent(event, config)) {
            return;
        }

        if (labelField != null) {
            labelField.mouseReleased(event.getMouseX(), event.getMouseY(), event.getButton());
        }

        // this only works for picking up items, not for dropping them back into the inventory
        saveCurrentChest(mod, event);
        mod.search();
    }

    @SubscribeEvent
    public void renderGui(GuiScreenEvent.DrawScreenEvent.Pre event) {
        ChestConfig config = mod.chestService.getConfig();
        boolean visible = config.enabled;
        if (labelField != null) {
            labelField.visible = visible;
        }
    }

    private void addChestLabelToScreen(GuiScreenEvent.InitGuiEvent.Post event, ContainerScreen<?> screen) {
        int guiLeft = screen.getGuiLeft();
        int guiTop = screen.getGuiTop();
        int width = 165;
        int offsetLeft = 5;
        int offsetTop = -12;
        labelField = new TextFieldWidget(
                Minecraft.getInstance().fontRenderer,
                guiLeft + offsetLeft, guiTop + offsetTop,
                width, 10,
                "Chest Label"
        );
        ChestConfig config = mod.chestService.getConfig();
        labelField.visible = config.enabled;
        if (mod.currentChest != null && mod.currentChest.label != null) {
            labelField.setText(mod.currentChest.label);
        }
        event.addWidget(labelField);
    }
}
