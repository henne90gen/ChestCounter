package de.henne90gen.chestcounter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;

public class Helper {

	public static Helper instance = new Helper();

	private Helper() {

	}

	public void runInThread(Runnable runnable) {
		Thread thread = new Thread(runnable);
		thread.setDaemon(true);
		thread.start();
	}

	public String createChestID(List<BlockPos> positions) {
		// copy and sort incoming list
		positions = new ArrayList<>(positions);
		positions.sort(getBlockPosComparator());

		List<String> positionStrings = positions.stream()
				.map(blockPos -> blockPos.getX() + "," + blockPos.getY() + "," + blockPos.getZ())
				.collect(Collectors.toList());
		return String.join(":", positionStrings);
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
	public List<BlockPos> getChestPositions(World world, BlockPos position) {
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
			if (tileEntity instanceof TileEntityChest) {
				chestPositions.add(pos);
			}
		}
		return chestPositions;
	}

	@Nonnull
	public String getWorldID() {
		int dimension = FMLClientHandler.instance().getClient().player.dimension;

		String worldName;
		ServerData currentServerData = Minecraft.getMinecraft().getCurrentServerData();
		if (currentServerData != null) {
			worldName = currentServerData.serverIP;
		} else {
			worldName = FMLClientHandler.instance().getServer().getWorldName();
		}

		return worldName + ":" + dimension;
	}

	public String createDefaultLabel(List<BlockPos> chestPositions) {
		if (chestPositions.isEmpty()){
			return "";
		}
		BlockPos pos = chestPositions.get(0);
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		return x + " " + y + " " + z;
	}
}
