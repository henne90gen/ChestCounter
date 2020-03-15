package de.henne90gen.chestcounter.service;

import de.henne90gen.chestcounter.Helper;
import de.henne90gen.chestcounter.db.ChestDB;
import de.henne90gen.chestcounter.db.entities.ChestContent;
import de.henne90gen.chestcounter.db.entities.Chests;
import de.henne90gen.chestcounter.service.dtos.Chest;
import de.henne90gen.chestcounter.service.dtos.ChestSearchResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
		LOGGER.trace("Saving " + id + " with " + items.toString());
		try {
			Chests chests = db.loadChests(chest.worldId);

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

			db.saveChests(chests, chest.worldId);
		} catch (Exception e) {
			LOGGER.error(e);
		}
	}

	@Override
	public void delete(String worldId, String partialChestId) {
		if (worldId == null || partialChestId == null) {
			return;
		}
		LOGGER.debug("Deleting " + partialChestId);
		try {
			Chests chests = db.loadChests(worldId);

			// Create copy of keySet to prevent ConcurrentModificationException
			HashSet<String> keys = new HashSet<>(chests.keySet());
			for (String key : keys) {
				if (key.contains(partialChestId)) {
					chests.remove(key);
					LOGGER.info("Deleted " + key);
				}
			}

			db.saveChests(chests, worldId);
		} catch (Exception e) {
			LOGGER.error(e);
		}
	}

	@Override
	public void updateLabel(String worldId, String chestId, String label) {
		if (worldId == null || chestId == null || label == null) {
			return;
		}
		label = label.isEmpty() ? Helper.createDefaultLabel(label) : label;

		LOGGER.info("Updating label of " + chestId + " to " + label);

		Chests chests = db.loadChests(worldId);
		ChestContent chestContent = chests.get(chestId);
		if (chestContent == null) {
			chestContent = new ChestContent();
		}
		chestContent.label = label;
		chests.put(chestId, chestContent);

		db.saveChests(chests, worldId);
	}

	@Override
	public Chest getChest(String worldId, String chestId) {
		Chests chests = db.loadChests(worldId);
		for (Map.Entry<String, ChestContent> entry : chests.entrySet()) {
			if (entry.getKey().contains(chestId) || chestId.contains(entry.getKey())) {
				Chest chest = new Chest();
				chest.worldId = worldId;
				chest.id = chestId;
				chest.label = entry.getValue().label;
				chest.items = entry.getValue().items;
				return chest;
			}
		}

		Chest chest = new Chest();
		chest.worldId = worldId;
		chest.id = chestId;
		return chest;
	}

	@Override
	public ChestSearchResult getItemCounts(String worldID, String queryString) {
		ChestSearchResult result = new ChestSearchResult();
		result.search = queryString;
		Chests chests = db.loadChests(worldID);

		for (Map.Entry<String, ChestContent> chestEntry : chests.entrySet()) {
			for (Map.Entry<String, Integer> itemEntry : chestEntry.getValue().items.entrySet()) {
				if (itemEntry.getKey().toLowerCase().contains(queryString.toLowerCase())) {
					String label;
					if (chestEntry.getValue().label == null || chestEntry.getValue().label.isEmpty()) {
						label = chestEntry.getKey();
					} else {
						label = chestEntry.getValue().label;
					}
					updateResultMap(result.byLabel, itemEntry, label);
					updateResultMap(result.byId, itemEntry, chestEntry.getKey());
				}
			}
		}
		return result;
	}

	private void updateResultMap(Map<String, Map<String, Integer>> result, Map.Entry<String, Integer> itemEntry, String key) {
		Map<String, Integer> amountMap = result.getOrDefault(key, new LinkedHashMap<>());
		Integer amount = amountMap.getOrDefault(itemEntry.getKey(), 0);

		amount += itemEntry.getValue();

		amountMap.put(itemEntry.getKey(), amount);
		result.put(key, amountMap);
	}

	@Override
	public List<Chest> getChests(String worldId) {
		Chests chests = db.loadChests(worldId);
		return chests.entrySet().stream().map(this.convertChestsEntryToChest(worldId)).collect(Collectors.toList());
	}

	private Function<Map.Entry<String, ChestContent>, Chest> convertChestsEntryToChest(String worldId) {
		return (entry) -> {
			Chest chest = new Chest();
			chest.worldId = worldId;
			chest.id = entry.getKey();
			chest.label = entry.getValue().label;
			chest.items = entry.getValue().items;
			return chest;
		};
	}
}
