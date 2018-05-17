package de.henne90gen.chestcounter;

import com.google.gson.Gson;
import net.minecraft.util.math.BlockPos;

import java.io.*;
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
            ChestContainer chestContainer = loadChestContainer();
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

            writeChestContainer(chestContainer);
        } catch (Exception e) {
            ChestCounter.logError(e);
        }
    }

    public static void delete(String id) {
        runInThread(() -> deleteWrapper(id));
    }

    public static void deleteWrapper(String id) {
        ChestCounter.logger.info("Deleting {}", id);
        try {
            ChestContainer chestContainer = loadChestContainer();
            if (chestContainer == null) {
                return;
            }
            for (String key : chestContainer.chests.keySet()) {
                if (key.contains(id)) {
                    chestContainer.chests.remove(key);
                }
            }

            writeChestContainer(chestContainer);
        } catch (Exception e) {
            ChestCounter.logError(e);
        }
    }

    public static void writeChestContainer(ChestContainer chestContainer) throws IOException {
        File jsonFile = new File(JSON_FILE_NAME);
        if (!jsonFile.exists()) {
            return;
        }
        try (FileWriter writer = new FileWriter(jsonFile)) {
            gson.toJson(chestContainer, writer);
        }
    }

    public static ChestContainer loadChestContainer() throws IOException {
        File jsonFile = new File(JSON_FILE_NAME);
        if (!jsonFile.exists()) {
            return null;
        }
        ChestContainer chestContainer;
        try (FileReader reader = new FileReader(jsonFile)) {
            chestContainer = gson.fromJson(reader, ChestContainer.class);
        }
        return chestContainer;
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
