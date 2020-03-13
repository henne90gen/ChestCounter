package de.henne90gen.chestcounter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.dimension.DimensionType;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Helper {

	public static Helper instance = new Helper();

	private Helper() {

	}

	public void runInThread(Runnable runnable) {
		Thread thread = new Thread(runnable);
		thread.setDaemon(true);
		thread.start();
	}

	public String getChestId(List<BlockPos> positions) {
		// copy and sort incoming list
		positions = new ArrayList<>(positions);
		positions.sort(getBlockPosComparator());

		List<String> positionStrings = positions.stream()
				.map(blockPos -> blockPos.getX() + "," + blockPos.getY() + "," + blockPos.getZ())
				.collect(Collectors.toList());
		return String.join(":", positionStrings);
	}

	public BlockPos getBlockPosFromChestID(String chestId) {
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

	public Comparator<BlockPos> getBlockPosComparator() {
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
	public String getWorldID() {
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

	public String createDefaultLabel(List<BlockPos> chestPositions) {
		if (chestPositions.isEmpty()) {
			return "";
		}
		BlockPos pos = chestPositions.get(0);
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		return x + " " + y + " " + z;
	}

	public String getChestId(IWorld world, BlockPos position) {
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
}
