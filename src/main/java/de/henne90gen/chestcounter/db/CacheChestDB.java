package de.henne90gen.chestcounter.db;

import de.henne90gen.chestcounter.db.entities.Chests;

import java.util.LinkedHashMap;
import java.util.Map;

public class CacheChestDB implements ChestDB {

	private final ChestDB delegate;

	private final Map<String, Chests> cachedChests = new LinkedHashMap<>();

	public CacheChestDB(ChestDB delegate) {
		this.delegate = delegate;
	}

	@Override
	public Chests loadChests(String worldID) {
		if (!cachedChests.containsKey(worldID)) {
			cachedChests.put(worldID, delegate.loadChests(worldID));
		}
		return cachedChests.get(worldID);
	}

	@Override
	public void saveChests(Chests chests, String worldID) {
		cachedChests.put(worldID, chests);
		delegate.saveChests(chests, worldID);
	}

	@Override
	public void deleteWorld(String worldID) {
		cachedChests.remove(worldID);
		delegate.deleteWorld(worldID);
	}
}
