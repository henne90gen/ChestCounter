package de.henne90gen.chestcounter.service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import de.henne90gen.chestcounter.IChestCounter;
import de.henne90gen.chestcounter.db.IChestDB;
import de.henne90gen.chestcounter.dtos.AmountResult;
import de.henne90gen.chestcounter.dtos.Chest;
import de.henne90gen.chestcounter.dtos.ChestContent;
import de.henne90gen.chestcounter.dtos.Chests;
import javax.annotation.Nonnull;

public class ChestService implements IChestService {

	private final IChestCounter mod;

	private final IChestDB db;

	public ChestService(IChestCounter mod, IChestDB db) {
		this.mod = mod;
		this.db = db;
	}

	@Override
	public void save(Chest chest) {
		mod.log("Saving " + chest.id + " with " + chest.chestContent.items.toString());
		try {
			Chests chests = db.loadChests(chest.worldID);
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

			db.saveChests(chests, chest.worldID);
		} catch (Exception e) {
			mod.logError(e);
		}
	}

	@Override
	public void delete(Chest chest) {
		mod.log("Deleting " + chest.id);
		try {
			Chests chests = db.loadChests(chest.worldID);
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

			db.saveChests(chests, chest.worldID);
		} catch (Exception e) {
			mod.logError(e);
		}
	}

	@Override
	public void updateLabel(Chest chest) {
		mod.log("Updating label of " + chest.id + " to " + chest.chestContent.label);
		try {
			Chests chests = db.loadChests(chest.worldID);
			if (chests == null) {
				return;
			}

			for (Map.Entry<String, ChestContent> entry : chests.entrySet()) {
				if (entry.getKey().equals(chest.id)) {
					entry.getValue().label = chest.chestContent.label;
				}
			}

			db.saveChests(chests, chest.worldID);
		} catch (Exception e) {
			mod.logError(e);
		}
	}

	@Override
	public Map<String, Integer> getItemCountsForLabel(@Nonnull String worldID, @Nonnull String label) {
		try {
			mod.log("Querying for " + label + " in world " + worldID);
			Map<String, Integer> itemCounts = new LinkedHashMap<>();
			Chests chests = db.loadChests(worldID);

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
		} catch (IOException e) {
			mod.logError(e);
			return null;
		}
	}

	@Override
	public Map<String, List<String>> getAllLabels(String worldID) {
		try {
			Chests chests = db.loadChests(worldID);
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

	private List<String> findChests(Chests chests, String label) {
		return chests.entrySet()
				.stream()
				.filter(entry -> label.equals(entry.getValue().label))
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());
	}

	@Override
	public ChestContent searchForChest(Chest chest) {
		try {
			Chests chests = db.loadChests(chest.worldID);
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
			Chests chests = db.loadChests(worldID);
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

	@Override
	public Chests getChests(String worldID) {
		try {
			return db.loadChests(worldID);
		} catch (IOException e) {
			mod.logError(e);
			return new Chests();
		}
	}
}
