package de.henne90gen.chestcounter;

import de.henne90gen.chestcounter.service.dtos.Chest;
import de.henne90gen.chestcounter.service.dtos.ChestSearchResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ChestGuiEventHandler {

	private static final Logger LOGGER = LogManager.getLogger();

	private static final int MARGIN = 5;

	private final ChestCounter mod;

	private Chest currentChest = null;

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
				int width = 75;
				int offsetLeft = 40;
				int offsetTop = 5;
				labelField = new TextFieldWidget(
						Minecraft.getInstance().fontRenderer,
						guiLeft + offsetLeft, guiTop + offsetTop,
						width, 10,
						"Chest Label"
				);
				if (currentChest != null) {
					labelField.setText(currentChest.label);
				}
				event.addWidget(labelField);
			}

			search();
		}
	}

	@SubscribeEvent
	public void keyPressed(GuiScreenEvent.KeyboardKeyPressedEvent.Pre event) {
		if (event.getGui() instanceof ContainerScreen) {
			// TODO pressing 'l' opens the achievements window -> prevent that from happening while the search box has focus
			keyPressedOnTextField(event, searchField);
			if (event.isCanceled()) {
				search();
				return;
			}

			if (event.getGui() instanceof ChestScreen) {
				keyPressedOnTextField(event, labelField);
				if (event.isCanceled()) {
					if (currentChest != null) {
						mod.chestService.updateLabel(currentChest.worldID, currentChest.id, labelField.getText());
					}
					search();
				}
			}
		}
	}

	private void search() {
		if (searchField == null) {
			return;
		}
		lastSearchResult = mod.chestService.getItemCounts(Helper.instance.getWorldID(), searchField.getText());
	}

	private void keyPressedOnTextField(GuiScreenEvent.KeyboardKeyPressedEvent.Pre event, TextFieldWidget textField) {
		event.setCanceled(textField.keyPressed(event.getKeyCode(), event.getScanCode(), event.getModifiers()));
		if (event.getKeyCode() == 69 /*e*/ && textField.isFocused()) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void charTyped(GuiScreenEvent.KeyboardCharTypedEvent.Pre event) {
		if (event.getGui() instanceof ContainerScreen) {
			event.setCanceled(searchField.charTyped(event.getCodePoint(), event.getModifiers()));
			search();

			if (event.getGui() instanceof ChestScreen) {
				event.setCanceled(labelField.charTyped(event.getCodePoint(), event.getModifiers()));
				// TODO update label in case currentChest is null
				if (currentChest != null && event.isCanceled()) {
					mod.chestService.updateLabel(currentChest.worldID, currentChest.id, labelField.getText());
					search();
				}
			}
		}
	}

	@SubscribeEvent
	public void mouseClicked(GuiScreenEvent.MouseClickedEvent.Pre event) {
		if (event.getGui() instanceof ContainerScreen) {
			search();
			event.setCanceled(searchField.mouseClicked(event.getMouseX(), event.getMouseY(), event.getButton()));
			if (labelField != null) {
				event.setCanceled(labelField.mouseClicked(event.getMouseX(), event.getMouseY(), event.getButton()));
			}
		}
	}

	@SubscribeEvent
	public void mouseReleased(GuiScreenEvent.MouseReleasedEvent.Pre event) {
		if (event.getGui() instanceof ContainerScreen) {
			search();
			event.setCanceled(searchField.mouseReleased(event.getMouseX(), event.getMouseY(), event.getButton()));
			if (labelField != null) {
				event.setCanceled(labelField.mouseReleased(event.getMouseX(), event.getMouseY(), event.getButton()));
			}
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
		for (Map.Entry<String, Map<String, Integer>> entry : lastSearchResult.entrySet()) {
			Minecraft.getInstance().fontRenderer.drawString(entry.getKey(), guiLeft + xSize + MARGIN, currentY, 0xffffff);
			currentY += MARGIN * 2;
			Map<String, Integer> value = entry.getValue();
			for (Map.Entry<String, Integer> amountEntry : value.entrySet()) {
				String amountString = amountEntry.getKey() + ": " + amountEntry.getValue();
				int xOffset = MARGIN * 2;
				Minecraft.getInstance().fontRenderer.drawString(amountString, guiLeft + xSize + MARGIN + xOffset, currentY, 0xffffff);
				currentY += MARGIN * 2;
			}
			currentY += MARGIN;
		}
	}

	@SubscribeEvent
	public void blockClicked(PlayerInteractEvent.RightClickBlock event) {
		BlockPos pos = event.getPos();
		World world = event.getWorld();
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity instanceof ChestTileEntity) {
			String worldId = Helper.instance.getWorldID();
			List<BlockPos> chestPositions = Helper.instance.getChestPositions(world, pos);
			String chestId = Helper.instance.getChestId(chestPositions);
			currentChest = mod.chestService.getChest(worldId, chestId);
		}
	}
}
