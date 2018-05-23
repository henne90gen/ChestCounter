package de.henne90gen.chestcounter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import de.henne90gen.chestcounter.dtos.Chest;
import de.henne90gen.chestcounter.dtos.ChestContent;
import de.henne90gen.chestcounter.dtos.ChestWorlds;
import de.henne90gen.chestcounter.dtos.Chests;
import net.minecraft.util.math.BlockPos;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

@SuppressWarnings("ALL")
public class ChestDBTest {

	@Test
	@Ignore
	public void getAllLabels() {

	}

	@Test
	@Ignore
	public void searchForChest() {

	}

	@Test
	public void getLabelItemCount() throws IOException {
		String filename = "./label-item-count-test.json";
		String worldID = "TestWorld:0";
		Chests chests = new Chests();

		String testLabel = "test";
		ChestContent chestContent1 = new ChestContent();
		chestContent1.label = testLabel;
		chestContent1.items.put("Glass", 1);
		chests.put("1,2,3", chestContent1);

		ChestContent chestContent2 = new ChestContent();
		chestContent2.label = "another";
		chestContent2.items.put("Glass", 2);
		chestContent2.items.put("Sand", 3);
		chests.put("4,5,6", chestContent2);

		chestDB(filename).writeChests(chests, worldID);

		Map<String, Integer> itemCounts = chestDB(filename).getItemCountsForLabel(worldID, testLabel);
		assertNotNull(itemCounts);
		assertEquals(1, itemCounts.size());
		assertTrue(itemCounts.containsKey("Glass"));
		assertEquals(new Integer(1), itemCounts.get("Glass"));

		new File(filename).delete();
	}

	@Test
	public void updateLabelNoFileExists() throws IOException, InterruptedException {
		String filename = "./update-label-no-file-test.json";
		String worldID = "TestWorld:0";
		String chestID = "1,2,3";
		String chestLabel = "TestLabel";
		Chest chest = new Chest();
		chest.worldID = worldID;
		chest.id = chestID;
		chest.chestContent.label = chestLabel;

		chestDB(filename).updateLabel(chest);

		assertFalse(new File(filename).exists());
	}

	@Test
	public void saveDoesNotDropLabel() throws IOException, InterruptedException {
		String filename = "./update-label-save-test.json";
		String worldID = "TestWorld:0";
		String chestID = "1,2,3";
		String chestLabel = "TestLabel";
		String itemName = "Glass";
		int itemAmount = 5;
		Chest chest = new Chest();
		chest.worldID = worldID;
		chest.id = chestID;
		String newChestLabel = "NewTestLabel";
		chest.chestContent.label = newChestLabel;

		File jsonFile = new File(filename);
		writeTestFile(jsonFile, worldID, chestID, chestLabel, itemName, itemAmount);

		chestDB(filename).updateLabel(chest);

		Chest saveChest = new Chest();
		saveChest.id = chestID;
		saveChest.worldID = worldID;
		saveChest.chestContent.items.put("Glass", 5);
		chestDB(filename).save(saveChest);

		Chests chests = chestDB(filename).loadChests(worldID);
		assertTrue(chests.containsKey(chestID));
		assertEquals(newChestLabel, chests.get(chestID).label);

		jsonFile.delete();
	}

	@Test
	public void updateLabel() throws IOException, InterruptedException {
		String filename = "./update-label-test.json";
		String worldID = "TestWorld:0";
		String chestID = "1,2,3";
		String chestLabel = "TestLabel";
		String itemName = "Glass";
		int itemAmount = 5;
		Chest chest = new Chest();
		chest.worldID = worldID;
		chest.id = chestID;
		String newChestLabel = "NewTestLabel";
		chest.chestContent.label = newChestLabel;

		File jsonFile = new File(filename);
		writeTestFile(jsonFile, worldID, chestID, chestLabel, itemName, itemAmount);

		chestDB(filename).updateLabel(chest);

		Chests chests = chestDB(filename).loadChests(worldID);
		assertTrue(chests.containsKey(chestID));
		assertEquals(newChestLabel, chests.get(chestID).label);

		jsonFile.delete();
	}

	@Test
	public void delete() throws IOException, InterruptedException {
		String filename = "./delete-test.json";
		String worldID = "TestWorld:0";
		String chestID = "1,2,3";
		String chestLabel = "TestLabel";
		String itemName = "Glass";
		int itemAmount = 5;
		Chest chest = new Chest();
		chest.worldID = worldID;
		chest.id = chestID;

		File jsonFile = new File(filename);
		writeTestFile(jsonFile, worldID, chestID, chestLabel, itemName, itemAmount);

		chestDB(filename).delete(chest);

		Chests chests = chestDB(filename).loadChests(worldID);
		assertNotNull(chests);
		assertEquals(0, chests.size());

		jsonFile.delete();
	}

	@Test
	public void deleteNoFileExists() {
		Chest chest = new Chest();
		chest.worldID = "TestWorld:0";
		chest.id = "1,2,3";
		String filename = "./delete-no-file-test.json";
		chestDB(filename).delete(chest);
		assertFalse(new File(filename).exists());
	}

	@Test
	public void saveFileExists() throws InterruptedException, IOException {
		String filename = "./save-no-file-test.json";
		File chestCounter = new File(filename);
		String chestID = "1,2,3";
		String worldID = "TestWorld:0";
		writeTestFile(chestCounter, worldID, chestID, "TestLabel", "Sand", 5);

		Chest chest = new Chest();
		chest.id = chestID;
		chest.worldID = worldID;
		String itemName = "Glass";
		int itemAmount = 5;
		chest.chestContent.items.put(itemName, itemAmount);

		chestDB(filename).save(chest);

		Chests chests = chestDB(filename).loadChests(worldID);
		assertNotNull(chests);
		assertTrue(chests.containsKey(chest.id));
		Map<String, Integer> items = chests.get(chest.id).items;
		assertNotNull(items);
		assertEquals(1, items.size());
		assertTrue(items.containsKey(itemName));
		assertEquals(new Integer(itemAmount), items.get(itemName));

		new File(filename).delete();
	}

	@Test
	public void saveNoFileExists() throws IOException, InterruptedException {
		String filename = "./save-no-file-test.json";
		Chest chest = new Chest();
		chest.id = "1,2,3";
		String worldID = "TestWorld:0";
		chest.worldID = worldID;
		String itemName = "Glass";
		int itemAmount = 5;
		chest.chestContent.items.put(itemName, itemAmount);
		String chestLabel = "TestLabel";
		chest.chestContent.label = chestLabel;

		chestDB(filename).save(chest);

		File jsonFile = new File(filename);
		assertTrue(jsonFile.exists());
		Chests chests = chestDB(filename).loadChests(worldID);
		assertNotNull(chests);
		assertTrue(chests.containsKey(chest.id));
		assertEquals(chestLabel, chests.get(chest.id).label);
		Map<String, Integer> items = chests.get(chest.id).items;
		assertNotNull(items);
		assertEquals(1, items.size());
		assertTrue(items.containsKey(itemName));
		assertEquals(new Integer(itemAmount), items.get(itemName));

		jsonFile.delete();
	}

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
	public void writesChestsCorrectly() {
		String filename = "./writes-chests-test.json";
		String worldID = "TestWorld:0";
		Chests chests = new Chests();
		try {
			chestDB(filename).writeChests(chests, worldID);

			ChestWorlds chestWorlds = chestDB(filename).readChestWorlds();
			assertNotNull(chestWorlds);
			assertTrue(chestWorlds.containsKey(worldID));
			assertEquals(1, chestWorlds.size());

			Chests actual = chestWorlds.get(worldID);
			assertEquals(0, actual.size());
		} catch (IOException e) {
			fail();
		} finally {
			new File(filename).delete();
		}
	}

	@Test
	public void readsChestWorldsCorrectly() throws IOException {
		String filename = "./chest-load-test.json";
		String worldID = "HelloWorld:0";
		String chestID = "1,2,3";
		String chestLabel = "TestLabel";
		String itemName = "Glass";
		int itemAmount = 64;

		File chestCounter = new File(filename);
		writeTestFile(chestCounter, worldID, chestID, chestLabel, itemName, itemAmount);

		try {
			ChestWorlds chestWorlds = chestDB(filename).readChestWorlds();
			assertEquals(1, chestWorlds.size());
			assertTrue(chestWorlds.containsKey(worldID));

			Chests chests = chestWorlds.get(worldID);
			assertEquals(1, chests.size());
			assertTrue(chests.containsKey(chestID));

			ChestContent chestContent = chests.get(chestID);
			assertEquals(chestLabel, chestContent.label);
			assertEquals(1, chestContent.items.size());

			assertTrue(chestContent.items.containsKey("Glass"));
			assertEquals(new Integer(64), chestContent.items.get("Glass"));
		} finally {
			chestCounter.delete();
		}
	}

	private void writeTestFile(File file, String worldID, String chestID, String chestLabel, String itemName, int itemAmount)
			throws IOException
	{
		FileWriter writer = new FileWriter(file);

		writer.write("{\""
				+ worldID
				+ "\":{\""
				+ chestID
				+ "\":{\"items\":{\"" + itemName + "\":" + itemAmount + "},\"label\":\"" + chestLabel + "\"}}}");
		writer.close();
	}

	@Test
	public void readsChestWorldsCorrectlyIfNoFileExists() {
		try {
			ChestWorlds chestWorlds = chestDB("./no-such-file.json").readChestWorlds();
			assertNull(chestWorlds);
		} catch (IOException e) {
			fail();
		}
	}

	private ChestDB chestDB(String filename) {
		return new ChestDB(new TestChestCounter(filename));
	}

	private ChestDB chestDB() {
		return chestDB("");
	}
}
