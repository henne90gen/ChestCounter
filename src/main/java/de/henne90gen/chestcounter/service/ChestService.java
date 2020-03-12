package de.henne90gen.chestcounter.service;

import de.henne90gen.chestcounter.db.ChestDB;
import de.henne90gen.chestcounter.db.entities.ChestContent;
import de.henne90gen.chestcounter.db.entities.Chests;
import de.henne90gen.chestcounter.service.dtos.Chest;
import de.henne90gen.chestcounter.service.dtos.ChestSearchResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ChestService implements IChestService {

	private static final Logger LOGGER = LogManager.getLogger();

	private final ChestDB db;

	public ChestService(ChestDB db) {
		this.db = db;
	}

	@Override
	public void save(Chest chest) {
		if (chest == null) {
			return;
		}
		String id = chest.id;
		Map<String, Integer> items = chest.items;
		LOGGER.info("Saving " + id + " with " + items.toString());
		try {
			Chests chests = db.loadChests(chest.worldID);
			if (chests == null) {
				chests = new Chests();
			}

			// Create copy of keySet to prevent ConcurrentModificationException
			HashSet<String> keys = new HashSet<>(chests.keySet());
			// Remove all chests that are now part of this chest, but keep their labels
			for (String key : keys) {
				if (id.contains(key)) {
					ChestContent chestContent = chests.remove(key);
					if (chestContent.label != null) {
						chest.label = chestContent.label;
					}
					break;
				}
			}
			ChestContent chestContent = new ChestContent();
			chestContent.label = chest.label;
			chestContent.items = chest.items;
			chests.put(id, chestContent);

			db.saveChests(chests, chest.worldID);
		} catch (Exception e) {
			LOGGER.error(e);
		}
	}

	@Override
	public void delete(String worldId, String chestId) {
		if (worldId == null || chestId == null) {
			return;
		}
		LOGGER.info("Deleting " + chestId);
		try {
			Chests chests = db.loadChests(worldId);
			if (chests == null) {
				return;
			}

			// Create copy of keySet to prevent ConcurrentModificationException
			HashSet<String> keys = new HashSet<>(chests.keySet());
			for (String key : keys) {
				if (key.contains(chestId)) {
					chests.remove(key);
				}
			}

			db.saveChests(chests, worldId);
		} catch (Exception e) {
			LOGGER.error(e);
		}
	}

	@Override
	public void updateLabel(Chest chest) {
		if (chest == null) {
			return;
		}
		LOGGER.info("Updating label of " + chest.id + " to " + chest.label);
		try {
			Chests chests = db.loadChests(chest.worldID);
			if (chests == null) {
				chests = new Chests();
			}

			ChestContent chestContent = chests.get(chest.id);
			if (chestContent == null) {
				chestContent = new ChestContent();
			}
			chestContent.label = chest.label;
			chests.put(chest.id, chestContent);

			db.saveChests(chests, chest.worldID);
		} catch (Exception e) {
			LOGGER.error(e);
		}
	}

	@Override
	public Map<String, Integer> getItemCountsForLabel(@Nonnull String worldID, @Nonnull String label) {
		LOGGER.info("Querying for " + label + " in world " + worldID);
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
	}

	@Override
	public Map<String, List<String>> getAllLabels(String worldID) {
		Chests chests = db.loadChests(worldID);
		return chests.values()
				.stream()
				.map(chestContent -> chestContent.label)
				.distinct()
				.collect(Collectors.toMap(label -> label, label -> findChests(chests, label)));
	}

	private List<String> findChests(Chests chests, String label) {
		return chests.entrySet()
				.stream()
				.filter(entry -> label.equals(entry.getValue().label))
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());
	}

	@Override
	public ChestContent searchForChest(String worldID, String chestId) {
		Chests chests = db.loadChests(worldID);
		for (Map.Entry<String, ChestContent> entry : chests.entrySet()) {
			if (entry.getKey().contains(chestId) || chestId.contains(entry.getKey())) {
				return entry.getValue();
			}
		}
		return null;
	}

	@Override
	public ChestSearchResult getItemCounts(String worldID, String queryString) {
		ChestSearchResult result = new ChestSearchResult();
		Chests chests = db.loadChests(worldID);
		if (chests == null) {
			return result;
		}

		for (Map.Entry<String, ChestContent> chestEntry : chests.entrySet()) {
			for (Map.Entry<String, Integer> itemEntry : chestEntry.getValue().items.entrySet()) {
				if (itemEntry.getKey().toLowerCase().contains(queryString.toLowerCase())) {
					// TODO rewrite this
//                        AmountResult itemAmount = amount.getOrDefault(itemEntry.getKey(), new AmountResult());
//                        itemAmount.amount += itemEntry.getValue();
//                        itemAmount.labels.add(chestEntry.getValue().label);
//                        amount.put(itemEntry.getKey(), itemAmount);
				}
			}
		}
		return result;
	}

	@Override
	public List<Chest> getChests(String worldId) {
		Chests chests = db.loadChests(worldId);
		return chests.entrySet().stream().map(this.convertChestsEntryToChest(worldId)).collect(Collectors.toList());
	}

	private Function<Map.Entry<String, ChestContent>, Chest> convertChestsEntryToChest(String worldId) {
		return (entry) -> {
			Chest chest = new Chest();
			chest.worldID = worldId;
			chest.id = entry.getKey();
			chest.label = entry.getValue().label;
			chest.items = entry.getValue().items;
			return chest;
		};
	}
}
