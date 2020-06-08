package de.henne90gen.chestcounter.db;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.henne90gen.chestcounter.db.entities.ChestConfig;
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
        ChestStorage chestStorage = readChestStorage();
        if (chestStorage == null) {
            return new Chests();
        }

        ChestWorlds worlds = chestStorage.worlds;
        if (worlds == null) {
            return new Chests();
        }

        if (!worlds.containsKey(worldID)) {
            worlds.put(worldID, new Chests());
            chestStorage.worlds = worlds;
            writeChestStorage(chestStorage);
        }

        return worlds.get(worldID);
    }

    @Override
    public void saveChests(Chests chests, String worldID) {
        ChestStorage chestStorage = readChestStorage();
        if (chestStorage == null) {
            chestStorage = new ChestStorage();
        }

        if (chestStorage.worlds == null) {
            chestStorage.worlds = new ChestWorlds();
        }

        chestStorage.worlds.put(worldID, chests);

        writeChestStorage(chestStorage);
    }

    @Override
    public void deleteWorld(String worldID) {
        ChestStorage chestStorage = readChestStorage();
        if (chestStorage == null) {
            return;
        }

        ChestWorlds worlds = chestStorage.worlds;
        if (worlds == null) {
            return;
        }

        worlds.remove(worldID);

        chestStorage.worlds = worlds;
        writeChestStorage(chestStorage);
    }

    @Nonnull
	@Override
    public ChestConfig loadChestConfig() {
        ChestStorage chestStorage = readChestStorage();
        if (chestStorage == null) {
            return new ChestConfig();
        }
        ChestConfig config = chestStorage.config;
        if (config == null) {
            return new ChestConfig();
        }
        return config;
    }

    @Override
    public void saveChestConfig(ChestConfig config) {
        ChestStorage chestStorage = readChestStorage();
        if (chestStorage == null) {
            chestStorage = new ChestStorage();
        }
        if (config == null) {
            config = new ChestConfig();
        }
        chestStorage.config = config;
        writeChestStorage(chestStorage);
    }

    private void writeChestStorage(ChestStorage storage) {
        synchronized (this) {
            File jsonFile = new File(filename);
            try (FileWriter writer = new FileWriter(jsonFile)) {
                gson.toJson(storage, writer);
            } catch (IOException e) {
                LOGGER.error(e);
            }
        }
    }

    private ChestStorage readChestStorage() {
        File jsonFile = new File(filename);
        if (!jsonFile.exists()) {
            return null;
        }

        JsonObject json;
        synchronized (this) {
            try (FileReader reader = new FileReader(jsonFile)) {
                json = gson.fromJson(reader, JsonObject.class);
            } catch (IllegalStateException | IOException e) {
                LOGGER.error(e);
                return null;
            }
        }

        if (json == null) {
            return null;
        }
        if (!json.has("version")) {
            LOGGER.error("JSON file " + filename + " does not contain a version number.");
            return null;
        }

        int version;
        try {
            version = json.get("version").getAsInt();
        } catch (ClassCastException | IllegalStateException e) {
            LOGGER.error(e);
            return null;
        }

        if (version <= 0 || version > ChestStorage.CURRENT_VERSION) {
            // something went wrong while parsing the version
            return null;
        }

        return withSchemaMigrations(version, json);
    }

    private ChestStorage withSchemaMigrations(int version, JsonObject json) {
        if (version != ChestStorage.CURRENT_VERSION) {
            performSchemaMigrations(version, json);
        }

        ChestStorage storage = gson.fromJson(json.toString(), ChestStorage.class);

        if (version != ChestStorage.CURRENT_VERSION || storage.config == null) {
            if (storage.config == null) {
                storage.config = new ChestConfig();
            }
            writeChestStorage(storage);
        }
        return storage;
    }

    private void performSchemaMigrations(int version, JsonObject json) {
        if (version <= 1) {
            migrateToVersion2(json);
        }
        if (version <= 2) {
            migrateToVersion3(json);
        }
    }

    private void bumpVersion(JsonObject json, int newVersion) {
        json.remove("version");
        json.addProperty("version", newVersion);
    }

    /**
     * Adds the config sub-tree
     * Adds the property searchResultPlacement to the config object
     */
    private void migrateToVersion2(JsonObject json) {
        bumpVersion(json, 2);

        JsonObject configJson = new JsonObject();
        configJson.addProperty("searchResultPlacement", "RIGHT_OF_INVENTORY");
        json.add("config", configJson);
    }

    /**
     * Adds enabled flag to the config object
     */
    private void migrateToVersion3(JsonObject json) {
        bumpVersion(json, 3);

        JsonObject configJson = json.getAsJsonObject("config");
        if (configJson == null) {
            // the config will be added with default values later on
            return;
        }

        configJson.addProperty("enabled", true);
    }
}
