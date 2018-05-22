package de.henne90gen.chestcounter.eventhandlers;

import java.util.*;

import de.henne90gen.chestcounter.ChestCounter;
import de.henne90gen.chestcounter.ChestDB;
import de.henne90gen.chestcounter.dtos.Chest;
import de.henne90gen.chestcounter.dtos.ChestContent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ChestEventHandler {

	private static final int INVENTORY_SIZE = 36;

	private final ChestCounter mod;
	private final ChestDB chestDB;

	private List<BlockPos> chestPositions;

	private Chest chest;

	public ChestEventHandler(ChestCounter mod) {
		this.mod = mod;
		this.chestPositions = new ArrayList<>();
		this.chestDB = new ChestDB(mod);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void close(GuiOpenEvent event) {
		if (event.getGui() == null && chest != null) {
			chestDB.save(chest);
			chest = null;
			chestPositions.clear();
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
			chest.worldID = mod.getWorldID();
			chest.id = chestDB.createChestID(chestPositions);
			chest.chestContent = countItems(currentContainer);
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
		chestPositions = mod.getChestPositions(event.getWorld(), event.getPos());
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void harvestBlock(BlockEvent.BreakEvent event) {
		TileEntity tileEntity = event.getWorld().getTileEntity(event.getPos());
		if (tileEntity instanceof TileEntityChest) {
			Chest chestToDelete = new Chest();
			chestToDelete.worldID = mod.getWorldID();
			chestToDelete.id = chestDB.createChestID(Collections.singletonList(event.getPos()));

			ChestContent chestContent = chestDB.getChestContent(chestToDelete);

			try {
				chestDB.delete(chestToDelete).join();
			} catch (InterruptedException ignored) {
			}

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
					chest.worldID = mod.getWorldID();
					chest.id = chestDB.createChestID(Collections.singletonList(position));
					chest.chestContent.label = chestContent.label;
					chestDB.save(chest);
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
			chest.worldID = mod.getWorldID();
			chest.id = chestDB.createChestID(mod.getChestPositions(event.getWorld(), event.getPos()));
			chestDB.save(chest);
		}
	}
}
