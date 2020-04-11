package de.henne90gen.chestcounter.service;

import de.henne90gen.chestcounter.db.entities.ChestConfig;
import de.henne90gen.chestcounter.service.dtos.Chest;
import de.henne90gen.chestcounter.service.dtos.ChestSearchResult;

import java.util.List;

public interface IChestService {
	/**
	 * Saves the given chest to the underlying data store.
	 *
	 * @param chest
	 */
	void save(Chest chest);

	/**
	 * Deletes the chest that has a chest id that contains the given partial chest id.
	 * This also splits double chests, removing all items from it in the process.
	 *
	 * @param worldId
	 * @param partialChestId
	 */
	void delete(String worldId, String partialChestId);

	/**
	 * Updates the label of the chest with the given id.
	 *
	 * @param worldId
	 * @param chestId
	 * @param label
	 */
	void updateLabel(String worldId, String chestId, String label);

	/**
	 * Retrieves a specific chest by its chest id.
	 *
	 * @param worldId
	 * @param chestId
	 * @return
	 */
	Chest getChest(String worldId, String chestId);

	/**
	 * TODO sort this by chest label and then by item name (different interface is necessary)
	 *
	 * @param worldId
	 * @param queryString
	 * @return
	 */
	ChestSearchResult getItemCounts(String worldId, String queryString);

	/**
	 * Retrieves all the chests for a world.
	 *
	 * @param worldId
	 * @return
	 */
	List<Chest> getChests(String worldId);

	/**
	 * Retrieve the current mod configuration
	 *
	 * @return
	 */
	ChestConfig getConfig();
}
