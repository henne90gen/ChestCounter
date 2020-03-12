package de.henne90gen.chestcounter.db;

import de.henne90gen.chestcounter.db.entities.Chests;

public interface ChestDB {
	Chests loadChests(String worldID);

	void saveChests(Chests chests, String worldID);

	void deleteWorld(String worldID);
}
