package de.henne90gen.chestcounter;

import de.henne90gen.chestcounter.db.ChestDB;
import de.henne90gen.chestcounter.db.FileChestDB;
import de.henne90gen.chestcounter.db.entities.ChestContent;
import de.henne90gen.chestcounter.db.entities.Chests;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class FileChestDBTest {

    @Test
    public void loadChestsWithNoFile() {
        String filename = "./loads-chests-no-file-test.json";
        FileChestDB chestDB = new FileChestDB(filename);

        String worldID = "TestWorld:0";
        Chests result = chestDB.loadChests(worldID);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void loadChestsWithNoWorld() throws IOException {
        String filename = "./loads-chests-no-world-test.json";
        FileChestDB chestDB = new FileChestDB(filename);

        String chestLabel = "TestLabel";
        String itemName = "Dirt";
        int itemAmount = 5;
        String chestID = "1,2,3";
        String worldID = "TestWorld:0";

        File chestCounter = new File(filename);
        writeTestFile(chestCounter, worldID, chestID, chestLabel, itemName, itemAmount);

        try {
            Chests result = chestDB.loadChests("NonExistentWorld:0");
            assertNotNull(result);
        } finally {
            chestCounter.delete();
        }
    }

    @Test
    public void loadsChestsCorrectly() throws IOException {
        String filename = "./loads-chests-test.json";
        FileChestDB chestDB = new FileChestDB(filename);

        String chestLabel = "TestLabel";
        String itemName = "Dirt";
        int itemAmount = 5;
        String chestID = "1,2,3";
        String worldID = "TestWorld:0";

        File chestCounter = new File(filename);
        writeTestFile(chestCounter, worldID, chestID, chestLabel, itemName, itemAmount);

        try {
            Chests result = chestDB.loadChests(worldID);
            assertEquals(1, result.size());
            assertTrue(result.containsKey(chestID));

            ChestContent resultContent = result.get(chestID);
            assertEquals(chestLabel, resultContent.label);
            assertEquals(1, resultContent.items.size());
            assertTrue(itemName, resultContent.items.containsKey(itemName));
            assertEquals(Integer.valueOf(itemAmount), resultContent.items.get(itemName));
        } finally {
            chestCounter.delete();
        }
    }

    @Test
    public void savesChestsCorrectly() {
        String filename = "./writes-chests-test.json";
        FileChestDB chestDB = new FileChestDB(filename);

        String worldID = "TestWorld:0";
        Chests chests = new Chests();
        try {
            chestDB.saveChests(chests, worldID);

            Chests chestsResult = chestDB.loadChests(worldID);
            assertNotNull(chestsResult);
            assertEquals(0, chestsResult.size());
        } finally {
            new File(filename).delete();
        }
    }

    @Test
    public void deleteWorldCorrectly() throws IOException {
        String filename = "./loads-chests-test.json";
        FileChestDB chestDB = new FileChestDB(filename);

        String chestLabel = "TestLabel";
        String itemName = "Dirt";
        int itemAmount = 5;
        String chestID = "1,2,3";
        String worldID = "TestWorld:0";

        File chestCounter = new File(filename);
        writeTestFile(chestCounter, worldID, chestID, chestLabel, itemName, itemAmount);

        try {
            chestDB.deleteWorld(worldID);

            Chests result = chestDB.loadChests(worldID);
            assertNotNull(result);
        } finally {
            chestCounter.delete();
        }
    }

    @Test
    public void deleteWorldDoesNotExist() throws IOException {
        String filename = "./loads-chests-test.json";
        FileChestDB chestDB = new FileChestDB(filename);

        String chestLabel = "TestLabel";
        String itemName = "Dirt";
        int itemAmount = 5;
        String chestID = "1,2,3";
        String worldID = "TestWorld:0";

        File chestCounter = new File(filename);
        writeTestFile(chestCounter, worldID, chestID, chestLabel, itemName, itemAmount);

        try {
            chestDB.deleteWorld("NonExistentWorld:0");

            Chests result = chestDB.loadChests(worldID);
            assertEquals(1, result.size());
            assertTrue(result.containsKey(chestID));

            ChestContent resultContent = result.get(chestID);
            assertEquals(chestLabel, resultContent.label);
            assertEquals(1, resultContent.items.size());
            assertTrue(itemName, resultContent.items.containsKey(itemName));
            assertEquals(Integer.valueOf(itemAmount), resultContent.items.get(itemName));
        } finally {
            chestCounter.delete();
        }
    }

    @Test
    public void parallelUpdateWorks() throws InterruptedException {
        final Logger LOGGER = LogManager.getLogger();

        List<Thread> threads = new ArrayList<>();
        String filename = "./parallel-test.json";
        ChestDB db = new FileChestDB(filename);
        String worldID = "MyWorld";

        Chests initialChests = new Chests();
        ChestContent initialChestContent = new ChestContent();
        initialChestContent.label = "TestChestLabel";
        initialChestContent.items.put("Glass", 5);
        initialChests.put("TestChestId", initialChestContent);
        db.saveChests(initialChests, worldID);

        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    LOGGER.info("Running iteration " + j);

                    Chests chests = db.loadChests(worldID);
                    chests.get("TestChestId").label = "OtherTestChestLabel";
                    db.saveChests(chests, worldID);

                    chests = db.loadChests(worldID);
                    chests.get("TestChestId").label = "TestChestLabel";
                    db.saveChests(chests, worldID);
                }
            });
            thread.start();
            threads.add(thread);
        }

        for (Thread thread : threads) {
            thread.join();
        }

        Chests finalChests = db.loadChests(worldID);
        assertNotNull(finalChests);
        assertEquals(1, finalChests.size());
        assertTrue(finalChests.containsKey("TestChestId"));
        ChestContent chestContent = finalChests.get("TestChestId");
        assertNotNull(chestContent);
        assertEquals("TestChestLabel", chestContent.label);
        assertEquals(1, chestContent.items.size());
        assertTrue(chestContent.items.containsKey("Glass"));
        assertEquals(new Integer(5), chestContent.items.get("Glass"));

        File dbFile = new File(filename);
        dbFile.deleteOnExit();
    }

    public static void writeTestFile(File file, String worldID, String chestID, String chestLabel, String itemName, int itemAmount)
            throws IOException {
        FileWriter writer = new FileWriter(file);

        writer.write("{" +
                "\"version\": 1," +
                "\"worlds\": {\""
                + worldID
                + "\":{\""
                + chestID
                + "\":{\"items\":{\"" + itemName + "\":" + itemAmount + "},\"label\":\"" + chestLabel + "\"}}}}");
        writer.close();
    }
}
