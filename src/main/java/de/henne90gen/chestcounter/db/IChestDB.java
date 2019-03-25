package de.henne90gen.chestcounter.db;

import java.io.IOException;

import de.henne90gen.chestcounter.dtos.Chests;

public interface IChestDB {
	Chests loadChests(String worldID) throws IOException;

	void saveChests(Chests chests, String worldID) throws IOException;

    void deleteWorld(String worldID) throws IOException;
}
