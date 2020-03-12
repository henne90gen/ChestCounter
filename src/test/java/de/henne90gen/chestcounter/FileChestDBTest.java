package de.henne90gen.chestcounter;

import de.henne90gen.chestcounter.db.FileChestDB;
import de.henne90gen.chestcounter.db.entities.ChestContent;
import de.henne90gen.chestcounter.db.entities.Chests;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.Assert.*;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class FileChestDBTest {

	@Test
	public void loadChestsWithNoFile() {
		String filename = "./loads-chests-no-file-test.json";
		FileChestDB chestDB = new FileChestDB(filename);

		String worldID = "TestWorld:0";
		Chests result = chestDB.loadChests(worldID);
		assertNull(result);
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
			assertNull(result);
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
			assertNull(result);
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
