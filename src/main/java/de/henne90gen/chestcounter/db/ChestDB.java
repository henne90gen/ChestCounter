package de.henne90gen.chestcounter.db;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import de.henne90gen.chestcounter.ChestCounter;
import de.henne90gen.chestcounter.dtos.Chest;
import de.henne90gen.chestcounter.dtos.ChestStorage;
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
		ChestStorage storage = new ChestStorage();
		storage.version = ChestStorage.CURRENT_VERSION;
		storage.worlds = worlds;
        synchronized (mod.fileLock) {
            File jsonFile = new File(filename);
            try (FileWriter writer = new FileWriter(jsonFile)) {
                gson.toJson(storage, writer);
            }
        }
    }

	private ChestWorlds readChestWorlds() throws IOException {
		File jsonFile = new File(filename);
		if (!jsonFile.exists()) {
			return null;
		}

		ChestStorage storage;
		synchronized (mod.fileLock) {
			try (FileReader reader = new FileReader(jsonFile)) {
				storage = gson.fromJson(reader, ChestStorage.class);
			} catch (IllegalStateException e) {
				mod.logError(e);
				return null;
			}
		}

		if (storage == null) {
			return null;
		}
		if (storage.version != ChestStorage.CURRENT_VERSION) {
			// TODO decide what to do with this
			return null;
		}
		return storage.worlds;
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
