package de.henne90gen.chestcounter;

import com.google.gson.Gson;
import net.minecraft.util.math.BlockPos;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class ItemDB {

    private static final String JSON_FILE_NAME = "./chestcount.json";

    private static final Gson gson = new Gson();

    public static void save(Chest chest) {
        runInThread(() -> saveWrapper(chest));
    }

    private static void saveWrapper(Chest chest) {
        ChestCounter.logger.info("Saving {} with {}", chest.id, chest.items.toString());
        try {
            File jsonFile = new File(JSON_FILE_NAME);
            ChestContainer chestContainer = null;
            if (jsonFile.exists()) {
                FileReader reader = new FileReader(jsonFile);
                chestContainer = gson.fromJson(reader, ChestContainer.class);
                reader.close();
            }
            if (chestContainer == null) {
                chestContainer = new ChestContainer();
            }

            // Create copy of keySet to prevent ConcurrentModificationException
            HashSet<String> keys = new HashSet<>(chestContainer.chests.keySet());
            // Remove all chests that are now part of this chest
            for (String key : keys) {
                if (chest.id.contains(key)) {
                    chestContainer.chests.remove(key);
                }
            }

            chestContainer.chests.put(chest.id, chest);

            FileWriter writer = new FileWriter(jsonFile);
            gson.toJson(chestContainer, writer);
            writer.close();
        } catch (Exception e) {
            logError(e);
        }
    }

    public static void delete(String id) {
        runInThread(() -> deleteWrapper(id));
    }

    public static void deleteWrapper(String id) {
        ChestCounter.logger.info("Deleting {}", id);
        try {
            File jsonFile = new File(JSON_FILE_NAME);
            if (!jsonFile.exists()) {
                return;
            }
            FileReader reader = new FileReader(jsonFile);
            ChestContainer chestContainer = gson.fromJson(reader, ChestContainer.class);
            reader.close();
            if (chestContainer == null) {
                return;
            }
            for (String key : chestContainer.chests.keySet()) {
                if (key.contains(id)) {
                    chestContainer.chests.remove(key);
                }
            }

            FileWriter writer = new FileWriter(jsonFile);
            gson.toJson(chestContainer, writer);
            writer.close();
        } catch (Exception e) {
            logError(e);
        }
    }

    private static void logError(Exception e) {
        ChestCounter.logger.error("Something went wrong!", e);
    }

    public static String buildID(List<BlockPos> positions) {
        // copy and sort incoming list
        positions = new ArrayList<>(positions);
        positions.sort(getBlockPosComparator());

        List<String> positionStrings = positions.stream()
                .map(blockPos -> blockPos.getX() + "," + blockPos.getY() + "," + blockPos.getZ())
                .collect(Collectors.toList());
        return String.join(":", positionStrings);
    }

    private static void runInThread(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.start();
    }

    public static Comparator<BlockPos> getBlockPosComparator() {
        return (block, other) -> {
            if (block.getX() < other.getX()) {
                return -1;
            } else if (block.getX() == other.getX()) {
                if (block.getY() < other.getY()) {
                    return -1;
                } else if (block.getY() == other.getY()) {
                    if (block.getZ() < other.getZ()) {
                        return -1;
                    } else if (block.getZ() == other.getZ()) {
                        return 0;
                    }
                }
            }
            return 1;
        };
    }
}
