package de.henne90gen.chestcounter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import de.henne90gen.chestcounter.db.ChestDB;
import de.henne90gen.chestcounter.service.ChestService;
import de.henne90gen.chestcounter.dtos.Chest;
import de.henne90gen.chestcounter.dtos.Chests;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

@SuppressWarnings("ALL")
public class ChestServiceTest {

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

		String testLabel = "test";
		Chest chest1 = new Chest();
		chest1.id = "1,2,3";
		chest1.worldID = worldID;
		chest1.chestContent.label = testLabel;
		chest1.chestContent.items.put("Glass", 1);
		chestService(filename).save(chest1);

		Chest chest2  = new Chest();
		chest2.id = "4,5,6";
		chest2.worldID = worldID;
		chest2.chestContent.label = "another";
		chest2.chestContent.items.put("Glass", 2);
		chest2.chestContent.items.put("Sand", 3);
		chestService(filename).save(chest2);

		Map<String, Integer> itemCounts = chestService(filename).getItemCountsForLabel(worldID, testLabel);
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

		chestService(filename).updateLabel(chest);

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

		chestService(filename).updateLabel(chest);

		Chest saveChest = new Chest();
		saveChest.id = chestID;
		saveChest.worldID = worldID;
		saveChest.chestContent.items.put("Glass", 5);
		chestService(filename).save(saveChest);

		Chests chests = chestService(filename).getChests(worldID);
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

		chestService(filename).updateLabel(chest);

		Chests chests = chestService(filename).getChests(worldID);
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

		chestService(filename).delete(chest);

		Chests chests = chestService(filename).getChests(worldID);
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
		chestService(filename).delete(chest);
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

		chestService(filename).save(chest);

		Chests chests = chestService(filename).getChests(worldID);
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

		chestService(filename).save(chest);

		File jsonFile = new File(filename);
		assertTrue(jsonFile.exists());

		Chests chests = chestService(filename).getChests(worldID);
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

	private ChestService chestService(String filename) {
		return new ChestService(new TestChestCounter(filename), new ChestDB(filename));
	}

	private ChestService chestService() {
		return chestService("");
	}
}
