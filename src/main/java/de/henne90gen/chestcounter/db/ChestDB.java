package de.henne90gen.chestcounter.db;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import de.henne90gen.chestcounter.ChestCounter;
import de.henne90gen.chestcounter.dtos.ChestWorlds;
import de.henne90gen.chestcounter.dtos.Chests;

public class ChestDB implements IChestDB {

    private static final Gson gson = new Gson();

	private final ChestCounter mod;
	private final String filename;

	public ChestDB(ChestCounter mod, String filename) {
		this.mod = mod;
		this.filename = filename;
	}

	@Override
	public Chests loadChests(String worldID) throws IOException {
		ChestWorlds worlds = readChestWorlds();
		if (worlds == null) {
			return null;
		}
		return worlds.get(worldID);
	}

    @Override
    public void saveChests(Chests chests, String worldID) throws IOException {
        ChestWorlds worlds = readChestWorlds();
        if (worlds == null) {
            worlds = new ChestWorlds();
        }
        worlds.put(worldID, chests);

        writeChestWorlds(worlds);
    }

    private void writeChestWorlds(ChestWorlds worlds) throws IOException {
        synchronized (mod.fileLock) {
            File jsonFile = new File(filename);
            try (FileWriter writer = new FileWriter(jsonFile)) {
                gson.toJson(worlds, writer);
            }
        }
    }

	private ChestWorlds readChestWorlds() throws IOException {
		File jsonFile = new File(filename);
		if (!jsonFile.exists()) {
			return null;
		}
		ChestWorlds worlds = null;
		synchronized (mod.fileLock) {
			try (FileReader reader = new FileReader(jsonFile)) {
				worlds = gson.fromJson(reader, ChestWorlds.class);
			} catch (IllegalStateException e) {
				mod.logError(e);
			}
		}
		return worlds;
	}

    @Override
    public void deleteWorld(String worldID) throws IOException {
        ChestWorlds worlds = readChestWorlds();
        if (worlds == null) {
            return;
        }

        worlds.remove(worldID);

        writeChestWorlds(worlds);
    }
}
