package de.henne90gen.chestcounter.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import de.henne90gen.chestcounter.dtos.AmountResult;
import de.henne90gen.chestcounter.dtos.Chest;
import de.henne90gen.chestcounter.dtos.ChestContent;
import de.henne90gen.chestcounter.dtos.Chests;
import javax.annotation.Nonnull;

public interface IChestService {
	void save(Chest chest);

	void delete(Chest chest);

	void updateLabel(Chest chest);

	Map<String, Integer> getItemCountsForLabel(@Nonnull String worldID, @Nonnull String label)
			throws IOException;

	Map<String, List<String>> getAllLabels(String worldID);

	ChestContent searchForChest(Chest chest);

	Map<String, AmountResult> getItemCounts(String worldID, String queryString);

	Chests getChests(String worldID);

    void deleteWorld(String worldID);
}
