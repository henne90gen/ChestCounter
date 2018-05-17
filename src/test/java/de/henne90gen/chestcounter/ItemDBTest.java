package de.henne90gen.chestcounter;

import net.minecraft.util.math.BlockPos;
import org.junit.Test;
import static org.junit.Assert.*;
import scala.actors.threadpool.Arrays;

import java.util.Comparator;
import java.util.List;

public class ItemDBTest {

    @Test
    public void canSortBlockPositions() {
        Comparator<BlockPos> blockPosComparator = ItemDB.getBlockPosComparator();
        BlockPos blockPos1 = new BlockPos(1, 2, 3);
        BlockPos blockPos2 = new BlockPos(2, 2, 3);
        List blockPos = Arrays.asList(new BlockPos[]{blockPos2, blockPos1});
        blockPos.sort(blockPosComparator);

        assertEquals(blockPos1, blockPos.get(0));
        assertEquals(blockPos2, blockPos.get(1));
    }

    @Test
    public void canCreateID() {
        BlockPos blockPos1 = new BlockPos(1, 2, 3);
        BlockPos blockPos2 = new BlockPos(2, 2, 3);
        List blockPos = Arrays.asList(new BlockPos[]{blockPos2, blockPos1});


    }
}
