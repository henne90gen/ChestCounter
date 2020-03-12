package de.henne90gen.chestcounter.service;

import de.henne90gen.chestcounter.db.entities.ChestContent;
import de.henne90gen.chestcounter.service.dtos.Chest;
import de.henne90gen.chestcounter.service.dtos.ChestSearchResult;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IChestService {
	void save(Chest chest);

	void delete(String worldId, String chestId);

	void updateLabel(Chest chest);

	Map<String, Integer> getItemCountsForLabel(@Nonnull String worldID, @Nonnull String label)
			throws IOException;

	Map<String, List<String>> getAllLabels(String worldID);

	ChestContent searchForChest(String worldId, String chestId);

	ChestSearchResult getItemCounts(String worldID, String queryString);

	List<Chest> getChests(String worldID);
}
