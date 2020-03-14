package de.henne90gen.chestcounter.db;

import de.henne90gen.chestcounter.db.entities.Chests;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheChestDB implements ChestDB {

	private final ChestDB delegate;

	private final Map<String, Chests> cachedChests = new ConcurrentHashMap<>();

	public CacheChestDB(ChestDB delegate) {
		this.delegate = delegate;
	}

	@Nonnull
	@Override
	public Chests loadChests(String worldID) {
		if (!cachedChests.containsKey(worldID)) {
			Chests chests = delegate.loadChests(worldID);
			cachedChests.put(worldID, chests);
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
