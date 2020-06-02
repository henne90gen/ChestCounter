package de.henne90gen.chestcounter.service;

import de.henne90gen.chestcounter.Helper;
import de.henne90gen.chestcounter.db.ChestDB;
import de.henne90gen.chestcounter.db.entities.ChestConfig;
import de.henne90gen.chestcounter.db.entities.ChestContent;
import de.henne90gen.chestcounter.db.entities.Chests;
import de.henne90gen.chestcounter.service.dtos.Chest;
import de.henne90gen.chestcounter.service.dtos.ChestSearchResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
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
				if (!key.contains(partialChestId)) {
					continue;
				}

				ChestContent content = chests.remove(key);
				LOGGER.info("Deleted " + key);

				if (key.contains(":") && !partialChestId.contains(":")) {
					String otherHalfKey = key
							.replace(partialChestId, "")
							.replace(":", "");
					content.items = new LinkedHashMap<>();
					chests.put(otherHalfKey, content);
				}
				break;
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
				String lowerCaseItemName = itemEntry.getKey().toLowerCase();
				if (!lowerCaseItemName.contains(queryString.toLowerCase())) {
					continue;
				}

				String chestId = chestEntry.getKey();
				String label = chestEntry.getValue().label;
				if (label == null || label.isEmpty()) {
					label = chestId;
				}

				Vec3d position = getAveragePosition(chestId);
				updateResultMap(result.byLabel, itemEntry, position, label);
				updateResultMap(result.byId, itemEntry, position, chestId);
			}
		}

		return result;
	}

	private Vec3d getAveragePosition(String chestId) {
		Vec3d position = new Vec3d(0, 0, 0);

		List<BlockPos> positions = Helper.extractPositionsFromChestId(chestId);
		for (BlockPos pos : positions) {
			position = position.add(pos.getX(), pos.getY(), pos.getZ());
		}

		return new Vec3d(position.x / positions.size(), position.y / positions.size(), position.z / positions.size());
	}

	private void updateResultMap(Map<String, ChestSearchResult.Entry> result, Map.Entry<String, Integer> itemEntry, Vec3d pos, String key) {
		ChestSearchResult.Entry resultEntry = result.getOrDefault(key, new ChestSearchResult.Entry());

		resultEntry.positions.add(pos);

		Integer amount = resultEntry.items.getOrDefault(itemEntry.getKey(), 0);
		amount += itemEntry.getValue();
		resultEntry.items.put(itemEntry.getKey(), amount);

		result.put(key, resultEntry);
	}

	@Override
	public List<Chest> getChests(String worldId) {
		Chests chests = db.loadChests(worldId);
		return chests.entrySet().stream().map(this.convertChestsEntryToChest(worldId)).collect(Collectors.toList());
	}

	@Override
	public ChestConfig getConfig() {
		return db.loadChestConfig();
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
