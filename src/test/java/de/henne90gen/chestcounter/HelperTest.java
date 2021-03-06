package de.henne90gen.chestcounter;

import com.mojang.datafixers.util.Pair;
import net.minecraft.util.math.BlockPos;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class HelperTest {
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
        Comparator<BlockPos> blockPosComparator = Helper.getBlockPosComparator();
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

        String chestID = Helper.formatPositionsToChestId(blockPos);
        assertEquals("1,2,3:2,2,3", chestID);
    }

    @Test
    public void createsChestIDForSingleChestCorrectly() {
        String chestID = Helper.formatPositionsToChestId(
                Collections.singletonList(new BlockPos(1, 2, 3))
        );
        assertEquals("1,2,3", chestID);
    }

    @Test
    public void extractsPositionsFromChestIdCorrectly() {
        String chestId = "1,2,3";
        List<BlockPos> positions = Helper.extractPositionsFromChestId(chestId);

        assertEquals(1, positions.size());
        assertEquals(1, positions.get(0).getX());
        assertEquals(2, positions.get(0).getY());
        assertEquals(3, positions.get(0).getZ());
    }

    @Test
    public void extractsPositionsFromDoubleChestIdCorrectly() {
        String chestId = "1,2,3:4,5,6";
        List<BlockPos> positions = Helper.extractPositionsFromChestId(chestId);

        assertEquals(2, positions.size());

        assertEquals(1, positions.get(0).getX());
        assertEquals(2, positions.get(0).getY());
        assertEquals(3, positions.get(0).getZ());

        assertEquals(4, positions.get(1).getX());
        assertEquals(5, positions.get(1).getY());
        assertEquals(6, positions.get(1).getZ());
    }

    @Test
    public void canCountItems() {
        List<Pair<String, Integer>> inventory = Arrays.asList(
                new Pair<>("item1", 5),
                new Pair<>("item1", 5),
                new Pair<>("item2", 20),
                new Pair<>("item3", 30),
                new Pair<>("Air", 40)
        );
        Map<String, Integer> count = Helper.countItems(inventory.stream());
        assertEquals(3, count.size());
        assertEquals(10, (int) count.get("item1"));
        assertEquals(20, (int) count.get("item2"));
        assertEquals(30, (int) count.get("item3"));
    }
}
