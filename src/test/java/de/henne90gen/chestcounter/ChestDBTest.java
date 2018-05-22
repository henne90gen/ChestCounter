package de.henne90gen.chestcounter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.henne90gen.chestcounter.dtos.ChestContent;
import de.henne90gen.chestcounter.dtos.ChestWorlds;
import de.henne90gen.chestcounter.dtos.Chests;
import net.minecraft.util.math.BlockPos;
import org.junit.Test;

import static org.junit.Assert.*;

@SuppressWarnings("ALL")
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

	@Test
	public void canSortBlockPositionsX() {
		canSortBlockPositions(2, 2, 3);
	}

	@Test
	public void canSortBlockPositionsY() {
		canSortBlockPositions(1, 3, 3);
	}

	@Test
	public void canSortBlockPositionsZ() {
		canSortBlockPositions(1, 2, 4);
	}

	@Test
	public void canSortBlockPositionsEqual() {
		canSortBlockPositions(1, 2, 3);
	}

	@Test
	public void canSortBlockPositionsZSmaller() {
		canSortBlockPositions(1, 2, 2, true);
	}

	private void canSortBlockPositions(int x2, int y2, int z2) {
		canSortBlockPositions(x2, y2, z2, false);
	}

	private void canSortBlockPositions(int x2, int y2, int z2, boolean reverse) {
		Comparator<BlockPos> blockPosComparator = chestDB().getBlockPosComparator();
		BlockPos blockPos1 = new BlockPos(1, 2, 3);
		BlockPos blockPos2 = new BlockPos(x2, y2, z2);
		List<BlockPos> blockPos = new ArrayList<>();
		blockPos.add(blockPos2);
		blockPos.add(blockPos1);

		blockPos.sort(blockPosComparator);

		if (reverse) {
			assertEquals(blockPos1, blockPos.get(1));
			assertEquals(blockPos2, blockPos.get(0));
		} else {
			assertEquals(blockPos1, blockPos.get(0));
			assertEquals(blockPos2, blockPos.get(1));
		}
	}

	@Test
	public void createsChestIDForDoubleChestCorrectly() {
		BlockPos blockPos1 = new BlockPos(1, 2, 3);
		BlockPos blockPos2 = new BlockPos(2, 2, 3);
		List<BlockPos> blockPos = new ArrayList<>();
		blockPos.add(blockPos2);
		blockPos.add(blockPos1);

		String chestID = chestDB().createChestID(blockPos);
		assertEquals("1,2,3:2,2,3", chestID);
	}

	@Test
	public void createsChestIDForSingleChestCorrectly() {
		String chestID = chestDB().createChestID(
				Collections.singletonList(new BlockPos(1, 2, 3))
		);
		assertEquals("1,2,3", chestID);
	}
}