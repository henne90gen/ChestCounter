package de.henne90gen.chestcounter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.minecraft.util.math.BlockPos;
import org.junit.Test;

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

        String chestID = Helper.getChestId(blockPos);
        assertEquals("1,2,3:2,2,3", chestID);
    }

    @Test
    public void createsChestIDForSingleChestCorrectly() {
        String chestID = Helper.getChestId(
                Collections.singletonList(new BlockPos(1, 2, 3))
        );
        assertEquals("1,2,3", chestID);
    }
}
