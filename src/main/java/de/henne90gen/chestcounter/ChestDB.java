package de.henne90gen.chestcounter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import de.henne90gen.chestcounter.dtos.*;
import javax.annotation.Nonnull;

public class ChestDB implements IChestDB {

	private final Gson gson = new Gson();

	private final IChestCounter mod;

	public ChestDB(IChestCounter mod) {
		this.mod = mod;
	}

	@Override
	public void save(Chest chest) {
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

	@Override
	public void delete(Chest chest) {
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

	@Override
	public void updateLabel(Chest chest) {
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

	@Override
	public Map<String, Integer> getItemCountsForLabel(@Nonnull String worldID, @Nonnull String label)
			throws IOException
	{
		mod.log("Querying for " + label + " in world " + worldID);
		Map<String, Integer> itemCounts = new LinkedHashMap<>();
		Chests chests = loadChests(worldID);

		for (ChestContent chestContent : chests.values()) {
			if (!label.equals(chestContent.label)) {
				continue;
			}

			for (Map.Entry<String, Integer> entry : chestContent.items.entrySet()) {
				Integer amount = itemCounts.getOrDefault(entry.getKey(), 0);
				amount += entry.getValue();
				if (amount > 0) {
					itemCounts.put(entry.getKey(), amount);
				}
			}
		}

		return itemCounts;
	}

	@Override
	public Map<String, List<String>> getAllLabels(String worldID) {
		try {
			Chests chests = loadChests(worldID);
			return chests.values()
					.stream()
					.map(chestContent -> chestContent.label)
					.distinct()
					.collect(Collectors.toMap(label -> label, label -> findChests(chests, label)));
		} catch (IOException e) {
			mod.logError(e);
			return Collections.emptyMap();
		}
	}

	@Override
	public List<String> findChests(Chests chests, String label) {
		return chests.entrySet()
				.stream()
				.filter(entry -> label.equals(entry.getValue().label))
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());
	}

	@Override
	public ChestContent searchForChest(Chest chest) {
		try {
			Chests chests = loadChests(chest.worldID);
			for (Map.Entry<String, ChestContent> entry : chests.entrySet()) {
				if (entry.getKey().contains(chest.id) || chest.id.contains(entry.getKey())) {
					return entry.getValue();
				}
			}
			return null;
		} catch (IOException e) {
			mod.logError(e);
			return null;
		}
	}

	@Override
	public Map<String, AmountResult> getItemCounts(String worldID, String queryString) {
		try {
			Chests chests = loadChests(worldID);
			if (chests == null) {
				return Collections.emptyMap();
			}

			Map<String, AmountResult> amount = new LinkedHashMap<>();
			for (Map.Entry<String, ChestContent> chestEntry : chests.entrySet()) {
				for (Map.Entry<String, Integer> itemEntry : chestEntry.getValue().items.entrySet()) {
					if (itemEntry.getKey().toLowerCase().contains(queryString.toLowerCase())) {
						AmountResult itemAmount = amount.getOrDefault(itemEntry.getKey(), new AmountResult());
						itemAmount.amount += itemEntry.getValue();
						itemAmount.labels.add(chestEntry.getValue().label);
						amount.put(itemEntry.getKey(), itemAmount);
					}
				}
			}
			return amount;
		} catch (IOException e) {
			mod.logError(e);
		}
		return Collections.emptyMap();
	}
}
