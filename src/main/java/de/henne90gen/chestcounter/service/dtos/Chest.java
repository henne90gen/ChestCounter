package de.henne90gen.chestcounter.service.dtos;

import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Chest {
	public String worldId;
	public String id;
	public String label;
	public Map<String, Integer> items = new LinkedHashMap<>();

	public boolean isDoubleChest() {
		return id.contains(":");
	}

	public List<BlockPos> getBlockPositions() {
		ArrayList<BlockPos> positions = new ArrayList<>();
		String[] parts = id.split(":");
		for (String part : parts) {
			String[] split = part.split(",");
			int x = Integer.parseInt(split[0]);
			int y = Integer.parseInt(split[1]);
			int z = Integer.parseInt(split[2]);
			positions.add(new BlockPos(x, y, z));
		}
		return positions;
	}
}
