package de.henne90gen.chestcounter.service;

import de.henne90gen.chestcounter.service.dtos.Chest;
import de.henne90gen.chestcounter.service.dtos.ChestSearchResult;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IChestService {
    void save(Chest chest);

    void delete(String worldId, String chestId);

    void updateLabel(String worldId, String chestId, String label);

    /**
     * Retrieves a specific chest by its chest id.
     *
     * @param worldID
     * @param chestId
     * @return
     */
    Chest getChest(String worldID, String chestId);

    /**
     * TODO sort this by chest label and then by item name (different interface is necessary)
     *
     * @param worldID
     * @param queryString
     * @return
     */
    ChestSearchResult getItemCounts(String worldID, String queryString);

    /**
     * Retrieves all the chests for a world.
     *
     * @param worldID
     * @return
     */
    List<Chest> getChests(String worldID);
}
