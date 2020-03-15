package de.henne90gen.chestcounter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.dimension.DimensionType;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class Helper {

	private static final int INVENTORY_SIZE = 36;

	public static void runInThread(Runnable runnable) {
		Thread thread = new Thread(runnable);
		thread.setDaemon(true);
		thread.start();
	}

	public static String getChestId(List<BlockPos> positions) {
		// copy and sort incoming list
		positions = new ArrayList<>(positions);
		positions.sort(getBlockPosComparator());

		List<String> positionStrings = positions.stream()
				.map(blockPos -> blockPos.getX() + "," + blockPos.getY() + "," + blockPos.getZ())
				.collect(Collectors.toList());
		return String.join(":", positionStrings);
	}

	public static BlockPos getBlockPosFromChestID(String chestId) {
		String[] parts = chestId.split(":");
		if (parts.length == 0) {
			return new BlockPos(0, 0, 0);
		}
		String[] coords = parts[0].split(",");
		float x = Float.parseFloat(coords[0]);
		float y = Float.parseFloat(coords[1]);
		float z = Float.parseFloat(coords[2]);
		return new BlockPos(x, y, z);
	}

	public static Comparator<BlockPos> getBlockPosComparator() {
		return (block, other) -> {
			if (block.getX() < other.getX()) {
				return -1;
			} else if (block.getX() == other.getX()) {
				if (block.getY() < other.getY()) {
					return -1;
				} else if (block.getY() == other.getY()) {
					if (block.getZ() < other.getZ()) {
						return -1;
					} else if (block.getZ() == other.getZ()) {
						return 0;
					}
				}
			}
			return 1;
		};
	}

	@Nonnull
	public static String getWorldID() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) {
			return "";
		}

		DimensionType dimension = mc.player.dimension;
		String worldName;
		ServerData currentServerData = mc.getCurrentServerData();
		if (currentServerData != null) {
			worldName = currentServerData.serverIP;
		} else {
			if (mc.world != null) {
				MinecraftServer server = mc.world.getServer();
				if (server != null) {
					worldName = server.getWorldName();
				} else {
					worldName = "default";
				}
			} else {
				worldName = "default";
			}
		}

		return worldName + ":" + dimension;
	}

	public static String createDefaultLabel(String chestId) {
		if (chestId == null || chestId.isEmpty()) {
			return "";
		}
		return chestId.replace(",", " ");
	}

	public static String getChestId(IWorld world, BlockPos position) {
		List<BlockPos> chestPositions = new ArrayList<>();
		BlockPos[] positions = {
				position,
				position.north(),
				position.east(),
				position.south(),
				position.west()
		};
		for (BlockPos pos : positions) {
			TileEntity tileEntity = world.getTileEntity(pos);
			if (tileEntity instanceof ChestTileEntity) {
				chestPositions.add(pos);
			}
		}
		return getChestId(chestPositions);
	}

	public static Map<String, Integer> countItemsInContainer(Container currentContainer) {
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
		return counter;
	}

	public static List<BlockPos> getCubeAroundPosition(BlockPos pos) {
		List<BlockPos> positions = new ArrayList<>();

		positions.add(pos);

		positions.add(pos.up());
		positions.add(pos.down());
		positions.add(pos.south());
		positions.add(pos.north());
		positions.add(pos.east());
		positions.add(pos.west());
		positions.add(pos.south().west());
		positions.add(pos.south().east());
		positions.add(pos.north().west());
		positions.add(pos.north().east());

		positions.add(pos.up().south());
		positions.add(pos.up().north());
		positions.add(pos.up().east());
		positions.add(pos.up().west());
		positions.add(pos.up().south().west());
		positions.add(pos.up().south().east());
		positions.add(pos.up().north().west());
		positions.add(pos.up().north().east());

		positions.add(pos.down().south());
		positions.add(pos.down().north());
		positions.add(pos.down().east());
		positions.add(pos.down().west());
		positions.add(pos.down().south().west());
		positions.add(pos.down().south().east());
		positions.add(pos.down().north().west());
		positions.add(pos.down().north().east());

		return positions;
	}
}
