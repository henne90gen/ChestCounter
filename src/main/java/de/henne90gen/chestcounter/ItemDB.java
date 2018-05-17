package de.henne90gen.chestcounter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import de.henne90gen.chestcounter.dtos.Chest;
import de.henne90gen.chestcounter.dtos.Chests;
import de.henne90gen.chestcounter.dtos.ChestWorlds;
import net.minecraft.util.math.BlockPos;

public class ItemDB {

	private static final String JSON_FILE_NAME = "./chestcount.json";

	private static final Gson gson = new Gson();

	public static void save(Chest chest) {
		runInThread(() -> saveWrapper(chest));
	}

	private static void saveWrapper(Chest chest) {
		ChestCounter.logger.info("Saving {} with {}", chest.id, chest.chestContent.items.toString());
		try {
			ChestWorlds worlds = readChestWorlds();
			if (worlds == null) {
				worlds = new ChestWorlds();
			}
			Chests chests = worlds.get(chest.worldID);
			if (chests == null) {
				chests = new Chests();
			}

			// Create copy of keySet to prevent ConcurrentModificationException
			HashSet<String> keys = new HashSet<>(chests.keySet());
			// Remove all chests that are now part of this chest
			for (String key : keys) {
				if (chest.id.contains(key)) {
					chests.remove(key);
				}
			}

			chests.put(chest.id, chest.chestContent);
			worlds.put(chest.worldID, chests);

			writeChestWorlds(worlds);
		} catch (Exception e) {
			ChestCounter.logError(e);
		}
	}

	public static void delete(Chest chest) {
		runInThread(() -> deleteWrapper(chest));
	}

	public static void deleteWrapper(Chest chest) {
		ChestCounter.logger.info("Deleting {}", chest.id);
		try {
			ChestWorlds worlds = readChestWorlds();
			if (worlds == null) {
				return;
			}
			Chests chests = worlds.get(chest.worldID);
			if (chests == null) {
				return;
			}

			// Create copy of keySet to prevent ConcurrentModificationException
			HashSet<String> keys = new HashSet<>(chests.keySet());
			for (String key : keys) {
				if (key.contains(chest.id)) {
					chests.remove(key);
				}
			}

			writeChestWorlds(worlds);
		} catch (Exception e) {
			ChestCounter.logError(e);
		}
	}

	public static void writeChestWorlds(ChestWorlds worlds) throws IOException {
		File jsonFile = new File(JSON_FILE_NAME);
		try (FileWriter writer = new FileWriter(jsonFile)) {
			gson.toJson(worlds, writer);
		}
	}

	public static ChestWorlds readChestWorlds() throws IOException {
		File jsonFile = new File(JSON_FILE_NAME);
		if (!jsonFile.exists()) {
			return null;
		}
		ChestWorlds worlds;
		try (FileReader reader = new FileReader(jsonFile)) {
			worlds = gson.fromJson(reader, ChestWorlds.class);
		}
		return worlds;
	}

	public static String buildID(List<BlockPos> positions) {
		// copy and sort incoming list
		positions = new ArrayList<>(positions);
		positions.sort(getBlockPosComparator());

		List<String> positionStrings = positions.stream()
				.map(blockPos -> blockPos.getX() + "," + blockPos.getY() + "," + blockPos.getZ())
				.collect(Collectors.toList());
		return String.join(":", positionStrings);
	}

	private static void runInThread(Runnable runnable) {
		Thread thread = new Thread(runnable);
		thread.setDaemon(true);
		thread.start();
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
}
