package de.henne90gen.chestcounter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import de.henne90gen.chestcounter.dtos.Chest;
import de.henne90gen.chestcounter.dtos.ChestContent;
import de.henne90gen.chestcounter.dtos.ChestWorlds;
import de.henne90gen.chestcounter.dtos.Chests;
import net.minecraft.util.math.BlockPos;

public class ChestDB {

	private final Gson gson = new Gson();

	private final IChestCounter mod;

	public ChestDB(IChestCounter mod) {
		this.mod = mod;
	}

	public Thread save(Chest chest) {
		return runInThread(() -> saveWrapper(chest));
	}

	private void saveWrapper(Chest chest) {
		mod.log("Saving " + chest.id + " with " + chest.chestContent.items.toString());
		try {
			Chests chests = loadChests(chest.worldID);
			if (chests == null) {
				chests = new Chests();
			}

			// Create copy of keySet to prevent ConcurrentModificationException
			HashSet<String> keys = new HashSet<>(chests.keySet());
			// Remove all chests that are now part of this chest, but keep their labels
			for (String key : keys) {
				if (chest.id.contains(key)) {
					ChestContent chestContent = chests.remove(key);
					if (chestContent.label != null) {
						chest.chestContent.label = chestContent.label;
					}
					break;
				}
			}

			chests.put(chest.id, chest.chestContent);

			writeChests(chests, chest.worldID);
		} catch (Exception e) {
			mod.logError(e);
		}
	}

	public Thread delete(Chest chest) {
		return runInThread(() -> deleteWrapper(chest));
	}

	private void deleteWrapper(Chest chest) {
		mod.log("Deleting " + chest.id);
		try {
			Chests chests = loadChests(chest.worldID);
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

			writeChests(chests, chest.worldID);
		} catch (Exception e) {
			mod.logError(e);
		}
	}

	public Thread updateLabel(Chest chest) {
		return runInThread(() -> updateLabelWrapper(chest));
	}

	private void updateLabelWrapper(Chest chest) {
		mod.log("Updating label of " + chest.id + " to " + chest.chestContent.label);
		try {
			Chests chests = loadChests(chest.worldID);
			if (chests == null) {
				return;
			}

			for (Map.Entry<String, ChestContent> entry : chests.entrySet()) {
				if (entry.getKey().equals(chest.id)) {
					entry.getValue().label = chest.chestContent.label;
				}
			}

			writeChests(chests, chest.worldID);
		} catch (Exception e) {
			mod.logError(e);
		}
	}

	public Chests loadChests(String worldID) throws IOException {
		ChestWorlds worlds = readChestWorlds();
		if (worlds == null) {
			return null;
		}
		Chests chests = worlds.get(worldID);
		if (chests == null) {
			return null;
		}
		return chests;
	}

	public void writeChests(Chests chests, String worldID) throws IOException {
		ChestWorlds worlds = readChestWorlds();
		if (worlds == null) {
			worlds = new ChestWorlds();
		}
		worlds.put(worldID, chests);

		File jsonFile = new File(mod.getChestDBFilename());
		try (FileWriter writer = new FileWriter(jsonFile)) {
			gson.toJson(worlds, writer);
		}
	}

	public ChestWorlds readChestWorlds() throws IOException {
		File jsonFile = new File(mod.getChestDBFilename());
		if (!jsonFile.exists()) {
			return null;
		}
		ChestWorlds worlds;
		try (FileReader reader = new FileReader(jsonFile)) {
			worlds = gson.fromJson(reader, ChestWorlds.class);
		}
		return worlds;
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

	private Thread runInThread(Runnable runnable) {
		Thread thread = new Thread(runnable);
		thread.setDaemon(true);
		thread.start();
		return thread;
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
}
