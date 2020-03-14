package de.henne90gen.chestcounter.db;

import com.google.gson.Gson;
import de.henne90gen.chestcounter.db.entities.ChestStorage;
import de.henne90gen.chestcounter.db.entities.ChestWorlds;
import de.henne90gen.chestcounter.db.entities.Chests;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileChestDB implements ChestDB {

	private static final Logger LOGGER = LogManager.getLogger();

	private static final Gson gson = new Gson();

	private final String filename;

	public FileChestDB(String filename) {
		this.filename = filename;
	}

	@Nonnull
    @Override
	public Chests loadChests(String worldID) {
		ChestWorlds worlds = readChestWorlds();
		if (worlds == null) {
			return new Chests();
		}
		if (!worlds.containsKey(worldID)) {
			worlds.put(worldID, new Chests());
			writeChestWorlds(worlds);
		}
		return worlds.get(worldID);
	}

	@Override
	public void saveChests(Chests chests, String worldID) {
		ChestWorlds worlds = readChestWorlds();
		if (worlds == null) {
			worlds = new ChestWorlds();
		}
		worlds.put(worldID, chests);

		writeChestWorlds(worlds);
	}

	@Override
	public void deleteWorld(String worldID) {
		ChestWorlds worlds = readChestWorlds();
		if (worlds == null) {
			return;
		}

		worlds.remove(worldID);

		writeChestWorlds(worlds);
	}

	private void writeChestWorlds(ChestWorlds worlds) {
		ChestStorage storage = new ChestStorage();
		storage.version = ChestStorage.CURRENT_VERSION;
		storage.worlds = worlds;
		synchronized (this) {
			File jsonFile = new File(filename);
			try (FileWriter writer = new FileWriter(jsonFile)) {
				gson.toJson(storage, writer);
			} catch (IOException e) {
				LOGGER.error(e);
			}
		}
	}

	private ChestWorlds readChestWorlds() {
		File jsonFile = new File(filename);
		if (!jsonFile.exists()) {
			return null;
		}

		ChestStorage storage;
		synchronized (this) {
			try (FileReader reader = new FileReader(jsonFile)) {
				storage = gson.fromJson(reader, ChestStorage.class);
			} catch (IllegalStateException | IOException e) {
				LOGGER.error(e);
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
}
