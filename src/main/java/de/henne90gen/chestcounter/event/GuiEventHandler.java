package de.henne90gen.chestcounter.event;

import de.henne90gen.chestcounter.ChestCounter;
import de.henne90gen.chestcounter.Helper;
import de.henne90gen.chestcounter.db.entities.ChestConfig;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.inventory.container.Container;
import net.minecraftforge.client.event.GuiScreenEvent;

public abstract class GuiEventHandler {

    protected boolean shouldNotHandleGuiEvent(GuiScreenEvent event, ChestConfig config) {
        return !config.enabled
                || !(event.getGui() instanceof ContainerScreen)
                || event.getGui() instanceof CreativeScreen;
    }

    protected void keyPressedOnTextField(GuiScreenEvent.KeyboardKeyPressedEvent.Pre event, TextFieldWidget textField) {
        boolean keyPressed = textField.keyPressed(event.getKeyCode(), event.getScanCode(), event.getModifiers());
        event.setCanceled(keyPressed);
        boolean disallowedKeyPressed = event.getKeyCode() == 69/*e*/ || event.getKeyCode() == 76/*l*/;
        boolean isFocused = textField.isFocused();
        if (disallowedKeyPressed && isFocused) {
            event.setCanceled(true);
        }
    }

    protected void saveCurrentChest(ChestCounter mod, GuiScreenEvent event) {
        if (mod.currentChest == null || !(event.getGui() instanceof ChestScreen)) {
            return;
        }

        Container currentContainer = ((ChestScreen) event.getGui()).getContainer();
        mod.currentChest.items = Helper.countItems(Helper.inventoryIterator(currentContainer));
        mod.chestService.save(mod.currentChest);
    }
}
