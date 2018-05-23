package de.henne90gen.chestcounter.db;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import de.henne90gen.chestcounter.dtos.Chests;

public class ChestDBCache implements IChestDB {

	private final IChestDB delegate;

	private final Map<String, Chests> cachedChests;

	public ChestDBCache(IChestDB delegate) {
		this.delegate = delegate;
		cachedChests = new LinkedHashMap<>();
	}

	@Override
	public Chests loadChests(String worldID) throws IOException {
		if (!cachedChests.containsKey(worldID)) {
			cachedChests.put(worldID, delegate.loadChests(worldID));
		}
		return cachedChests.get(worldID);
	}

	@Override
	public void saveChests(Chests chests, String worldID) throws IOException {
		cachedChests.put(worldID, chests);
		delegate.saveChests(chests, worldID);
	}
}
