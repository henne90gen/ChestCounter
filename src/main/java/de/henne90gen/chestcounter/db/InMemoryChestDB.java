package de.henne90gen.chestcounter.db;

import de.henne90gen.chestcounter.db.entities.Chests;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.Map;

public class InMemoryChestDB implements ChestDB {

	private final Map<String, Chests> chests = new LinkedHashMap<>();

	@Nonnull
    @Override
	public Chests loadChests(String worldID) {
		if (!chests.containsKey(worldID)) {
			chests.put(worldID, new Chests());
		}
		return chests.get(worldID);
	}

	@Override
	public void saveChests(Chests chests, String worldID) {
		this.chests.put(worldID, chests);
	}

	@Override
	public void deleteWorld(String worldID) {
		chests.remove(worldID);
	}
}
