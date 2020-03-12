package de.henne90gen.chestcounter;

import de.henne90gen.chestcounter.service.dtos.AmountResult;
import de.henne90gen.chestcounter.service.dtos.ChestSearchResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ChestGuiEventHandler {

	private static final Logger LOGGER = LogManager.getLogger();

	private static final int MARGIN = 5;

	private final ChestCounter mod;

	private TextFieldWidget searchField = null;
	private ChestSearchResult lastSearchResult = null;

	private TextFieldWidget labelField = null;

	public ChestGuiEventHandler(ChestCounter mod) {
		this.mod = mod;
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
			if (event.isCanceled()) {
				lastSearchResult = mod.chestService.getItemCounts(Helper.instance.getWorldID(), searchField.getText());
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
		// TODO rewrite this
//		for (Map.Entry<String, AmountResult> entry : lastSearchResult.entrySet()) {
//			Minecraft.getInstance().fontRenderer.drawString(entry.getKey(), guiLeft + xSize + MARGIN, currentY, 0xffffff);
//			currentY += MARGIN * 2;
//			AmountResult value = entry.getValue();
//			for (Map.Entry<String, Integer> amountEntry : entry.getValue().entrySet()) {
//				String amountString = amountEntry.getKey() + ": " + amountEntry.getValue();
//				Minecraft.getInstance().fontRenderer.drawString(amountString, guiLeft + xSize + MARGIN, currentY, 0xffffff);
//				currentY += MARGIN * 2;
//			}
//		}
	}
}
