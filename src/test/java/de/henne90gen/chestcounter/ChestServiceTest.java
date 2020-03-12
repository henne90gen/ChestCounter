package de.henne90gen.chestcounter;

import de.henne90gen.chestcounter.db.ChestDB;
import de.henne90gen.chestcounter.db.InMemoryChestDB;
import de.henne90gen.chestcounter.db.entities.ChestContent;
import de.henne90gen.chestcounter.db.entities.Chests;
import de.henne90gen.chestcounter.service.ChestService;
import de.henne90gen.chestcounter.service.dtos.Chest;
import de.henne90gen.chestcounter.service.dtos.ChestSearchResult;
import org.junit.Test;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@SuppressWarnings("ALL")
public class ChestServiceTest {

	@Test
	public void getLabelItemCount() {
		InMemoryChestDB db = new InMemoryChestDB();
		ChestService chestService = new ChestService(db);

		String worldID = "TestWorld:0";

		String testLabel = "test";
		Chest chest1 = new Chest();
		chest1.id = "1,2,3";
		chest1.worldID = worldID;
		chest1.label = testLabel;
		chest1.items.put("Glass", 1);
		chestService.save(chest1);

		Chest chest2 = new Chest();
		chest2.id = "4,5,6";
		chest2.worldID = worldID;
		chest2.label = "another";
		chest2.items.put("Glass", 2);
		chest2.items.put("Sand", 3);
		chestService.save(chest2);

		Map<String, Integer> itemCounts = chestService.getItemCountsForLabel(worldID, testLabel);
		assertNotNull(itemCounts);
		assertEquals(1, itemCounts.size());
		assertTrue(itemCounts.containsKey("Glass"));
		assertEquals(new Integer(1), itemCounts.get("Glass"));
	}

	@Test
	public void updateLabelNoData() {
		InMemoryChestDB db = new InMemoryChestDB();
		ChestService chestService = new ChestService(db);

		String worldID = "TestWorld:0";
		String chestID = "1,2,3";
		String chestLabel = "TestLabel";
		chestService.updateLabel(worldID, chestID, chestLabel);

		Chests chests = db.loadChests(worldID);
		assertNotNull(chests);
		assertEquals(1, chests.size());
		assertTrue(chests.containsKey(chestID));
		ChestContent resultChest = chests.get(chestID);
		assertEquals(chestLabel, resultChest.label);
		assertEquals(0, resultChest.items.size());
	}

	@Test
	public void saveDoesNotDropLabel() throws IOException, InterruptedException {
		InMemoryChestDB db = new InMemoryChestDB();
		ChestService chestService = new ChestService(db);

		String worldID = "TestWorld:0";
		String chestID = "1,2,3";
		String chestLabel = "TestLabel";
		String itemName = "Glass";
		int itemAmount = 5;

		writeDataToDB(db, worldID, chestID, chestLabel, itemName, itemAmount);

		String newChestLabel = "NewTestLabel";
		chestService.updateLabel(worldID, chestID, newChestLabel);

		Chest saveChest = new Chest();
		saveChest.id = chestID;
		saveChest.worldID = worldID;
		saveChest.items.put("Glass", 5);
		chestService.save(saveChest);

		List<Chest> chests = chestService.getChests(worldID);
		assertNotNull(chests);
		assertEquals(1, chests.size());
		Chest resultChest = chests.get(0);
		assertEquals(worldID, resultChest.worldID);
		assertEquals(chestID, resultChest.id);
		assertEquals(newChestLabel, resultChest.label);
		assertEquals(1, resultChest.items.size());
		assertTrue(resultChest.items.containsKey(itemName));
		assertEquals(new Integer(itemAmount), resultChest.items.get(itemName));
	}

	@Test
	public void updateLabel() {
		InMemoryChestDB db = new InMemoryChestDB();
		ChestService chestService = new ChestService(db);

		String worldID = "TestWorld:0";
		String chestID = "1,2,3";
		String chestLabel = "TestLabel";
		String itemName = "Glass";
		int itemAmount = 5;

		writeDataToDB(db, worldID, chestID, chestLabel, itemName, itemAmount);

		String newChestLabel = "NewTestLabel";
		chestService.updateLabel(worldID, chestID, newChestLabel);

		List<Chest> chests = chestService.getChests(worldID);
		assertNotNull(chests);
		assertEquals(1, chests.size());
		Chest resultChest = chests.get(0);
		assertEquals(worldID, resultChest.worldID);
		assertEquals(chestID, resultChest.id);
		assertEquals(newChestLabel, resultChest.label);
		assertEquals(1, resultChest.items.size());
		assertTrue(resultChest.items.containsKey(itemName));
		assertEquals(new Integer(itemAmount), resultChest.items.get(itemName));
	}

	@Test
	public void delete() {
		InMemoryChestDB db = new InMemoryChestDB();
		ChestService chestService = new ChestService(db);

		String worldID = "TestWorld:0";
		String chestID = "1,2,3";
		String chestLabel = "TestLabel";
		String itemName = "Glass";
		int itemAmount = 5;

		writeDataToDB(db, worldID, chestID, chestLabel, itemName, itemAmount);

		chestService.delete(worldID, chestID);

		List<Chest> chests = chestService.getChests(worldID);
		assertNotNull(chests);
		assertEquals(0, chests.size());
	}

	@Test
	public void save() {
		InMemoryChestDB db = new InMemoryChestDB();
		ChestService chestService = new ChestService(db);

		String chestID = "1,2,3";
		String worldID = "TestWorld:0";

		writeDataToDB(db, worldID, chestID, "TestLabel", "Sand", 5);

		Chest chest = new Chest();
		chest.id = chestID;
		chest.worldID = worldID;
		String itemName = "Glass";
		int itemAmount = 5;
		chest.items.put(itemName, itemAmount);

		chestService.save(chest);

		List<Chest> chests = chestService.getChests(worldID);
		assertNotNull(chests);
		assertEquals(1, chests.size());
		Chest resultChest = chests.get(0);
		assertEquals(worldID, resultChest.worldID);
		assertEquals(chestID, resultChest.id);
		Map<String, Integer> items = chests.get(0).items;
		assertNotNull(items);
		assertEquals(1, items.size());
		assertTrue(items.containsKey(itemName));
		assertEquals(new Integer(itemAmount), items.get(itemName));
	}

	@Test
	public void saveToEmptyDB() {
		InMemoryChestDB db = new InMemoryChestDB();
		ChestService chestService = new ChestService(db);

		String worldID = "TestWorld:0";
		String itemName = "Glass";
		int itemAmount = 5;
		String chestLabel = "TestLabel";
		Chest chest = new Chest();
		chest.id = "1,2,3";
		chest.worldID = worldID;
		chest.label = chestLabel;
		chest.items.put(itemName, itemAmount);

		chestService.save(chest);

		Chests chests = db.loadChests(worldID);
		assertNotNull(chests);
		assertTrue(chests.containsKey(chest.id));
		assertEquals(chestLabel, chests.get(chest.id).label);
		Map<String, Integer> items = chests.get(chest.id).items;
		assertNotNull(items);
		assertEquals(1, items.size());
		assertTrue(items.containsKey(itemName));
		assertEquals(new Integer(itemAmount), items.get(itemName));
	}

	@Test
	public void getItemCountsWorks() {
		InMemoryChestDB db = new InMemoryChestDB();
		ChestService chestService = new ChestService(db);

		String chestID = "1,2,3";
		String worldID = "TestWorld:0";

		Chest chest = new Chest();
		chest.id = chestID;
		chest.worldID = worldID;
		chest.items.put("Glass", 5);
		chest.items.put("Sand", 32);
		chestService.save(chest);

		ChestSearchResult searchResult = chestService.getItemCounts(worldID, "sa");
		assertNotNull(searchResult);
		assertEquals(1, searchResult.size());
		assertTrue(searchResult.containsKey(chestID));
		Map<String, Integer> chestResult = searchResult.get(chestID);
		assertTrue(chestResult.containsKey("Sand"));
		assertEquals(new Integer(32), chestResult.get("Sand"));
	}

	@Test
	public void getChestWorks() {
		InMemoryChestDB db = new InMemoryChestDB();
		ChestService chestService = new ChestService(db);

		String chestID = "1,2,3";
		String worldID = "TestWorld:0";

		Chest chest = new Chest();
		chest.id = chestID;
		chest.worldID = worldID;
		chest.items.put("Glass", 5);
		chestService.save(chest);

		Chest chestResult = chestService.getChest(worldID, chestID);
		assertNotNull(chestResult);
		assertEquals(worldID, chestResult.worldID);
		assertEquals(chestID, chestResult.id);
		assertEquals(chestID, chestResult.label);
		assertEquals(1, chestResult.items.size());
	}

	private void writeDataToDB(ChestDB db, String worldID, String chestID, String chestLabel, String itemName, int itemAmount) {
		Chests chests = new Chests();
		ChestContent chestContent = new ChestContent();
		chestContent.label = chestLabel;
		chestContent.items = new LinkedHashMap<>();
		chestContent.items.put(itemName, itemAmount);
		chests.put(chestID, chestContent);
		db.saveChests(chests, worldID);
	}
}
