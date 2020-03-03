package de.henne90gen.chestcounter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import de.henne90gen.chestcounter.db.ChestDB;
import de.henne90gen.chestcounter.dtos.ChestContent;
import de.henne90gen.chestcounter.dtos.Chests;
import org.junit.Test;

import static org.junit.Assert.*;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class ChestDBTest {

    @Test
    public void loadChestsWithNoFile() {
        String filename = "./loads-chests-no-file-test.json";
        String worldID = "TestWorld:0";
        try {
            Chests result = chestDB(filename).loadChests(worldID);
            assertNull(result);
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    public void loadChestsWithNoWorld() throws IOException {
        String filename = "./loads-chests-no-world-test.json";
        String chestLabel = "TestLabel";
        String itemName = "Dirt";
        int itemAmount = 5;
        String chestID = "1,2,3";
        String worldID = "TestWorld:0";

        File chestCounter = new File(filename);
        writeTestFile(chestCounter, worldID, chestID, chestLabel, itemName, itemAmount);

        try {
            Chests result = chestDB(filename).loadChests("NonExistentWorld:0");
            assertNull(result);
        } catch (IOException e) {
            fail();
        } finally {
            chestCounter.delete();
        }
    }

    @Test
    public void loadsChestsCorrectly() throws IOException {
        String filename = "./loads-chests-test.json";
        String chestLabel = "TestLabel";
        String itemName = "Dirt";
        int itemAmount = 5;
        String chestID = "1,2,3";
        String worldID = "TestWorld:0";

        File chestCounter = new File(filename);
        writeTestFile(chestCounter, worldID, chestID, chestLabel, itemName, itemAmount);

        try {
            Chests result = chestDB(filename).loadChests(worldID);
            assertEquals(1, result.size());
            assertTrue(result.containsKey(chestID));

            ChestContent resultContent = result.get(chestID);
            assertEquals(chestLabel, resultContent.label);
            assertEquals(1, resultContent.items.size());
            assertTrue(itemName, resultContent.items.containsKey(itemName));
            assertEquals(new Integer(itemAmount), resultContent.items.get(itemName));
        } catch (IOException e) {
            fail();
        } finally {
            chestCounter.delete();
        }
    }

    @Test
    public void savesChestsCorrectly() {
        String filename = "./writes-chests-test.json";
        String worldID = "TestWorld:0";
        Chests chests = new Chests();
        try {
            chestDB(filename).saveChests(chests, worldID);

            Chests chestsResult = chestDB(filename).loadChests(worldID);
            assertNotNull(chestsResult);
            assertEquals(0, chestsResult.size());
        } catch (IOException e) {
            fail();
        } finally {
            new File(filename).delete();
        }
    }

    @Test
    public void deleteWorldCorrectly() throws IOException {
        String filename = "./loads-chests-test.json";
        String chestLabel = "TestLabel";
        String itemName = "Dirt";
        int itemAmount = 5;
        String chestID = "1,2,3";
        String worldID = "TestWorld:0";

        File chestCounter = new File(filename);
        writeTestFile(chestCounter, worldID, chestID, chestLabel, itemName, itemAmount);

        try {
            chestDB(filename).deleteWorld(worldID);

            Chests result = chestDB(filename).loadChests(worldID);
            assertNull(result);
        } catch (IOException e) {
            fail();
        } finally {
            chestCounter.delete();
        }
    }

    @Test
    public void deleteWorldDoesNotExist() throws IOException {
        String filename = "./loads-chests-test.json";
        String chestLabel = "TestLabel";
        String itemName = "Dirt";
        int itemAmount = 5;
        String chestID = "1,2,3";
        String worldID = "TestWorld:0";

        File chestCounter = new File(filename);
        writeTestFile(chestCounter, worldID, chestID, chestLabel, itemName, itemAmount);

        try {
            chestDB(filename).deleteWorld("NonExistentWorld:0");

            Chests result = chestDB(filename).loadChests(worldID);
            assertEquals(1, result.size());
            assertTrue(result.containsKey(chestID));

            ChestContent resultContent = result.get(chestID);
            assertEquals(chestLabel, resultContent.label);
            assertEquals(1, resultContent.items.size());
            assertTrue(itemName, resultContent.items.containsKey(itemName));
            assertEquals(new Integer(itemAmount), resultContent.items.get(itemName));        } catch (IOException e) {
            fail();
        } finally {
            chestCounter.delete();
        }
    }

    private void writeTestFile(File file, String worldID, String chestID, String chestLabel, String itemName, int itemAmount)
            throws IOException {
        FileWriter writer = new FileWriter(file);

        writer.write("{\""
                + worldID
                + "\":{\""
                + chestID
                + "\":{\"items\":{\"" + itemName + "\":" + itemAmount + "},\"label\":\"" + chestLabel + "\"}}}");
        writer.close();
    }

	private ChestDB chestDB(String filename) {
		return new ChestDB(new ChestCounter(), filename);
	}
}
