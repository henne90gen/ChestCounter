package de.henne90gen.chestcounter;

import de.henne90gen.chestcounter.service.dtos.Chest;
import net.minecraft.util.math.BlockPos;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ChestTest {

    @Test
    public void canConvertSingleChestIDToBlockPosCorrectly() {
        Chest chest = new Chest();
        chest.id = "10,20,30";

        List<BlockPos> positions = chest.getBlockPositions();
        assertEquals(1, positions.size());

        BlockPos pos = positions.get(0);
        assertEquals(10, pos.getX());
        assertEquals(20, pos.getY());
        assertEquals(30, pos.getZ());
    }

    @Test
    public void canConvertDoubleChestIDToBlockPosCorrectly() {
        Chest chest = new Chest();
        chest.id = "10,20,30:40,50,60";

        List<BlockPos> positions = chest.getBlockPositions();
        assertEquals(2, positions.size());

        BlockPos pos = positions.get(0);
        assertEquals(10, pos.getX());
        assertEquals(20, pos.getY());
        assertEquals(30, pos.getZ());

        pos = positions.get(1);
        assertEquals(40, pos.getX());
        assertEquals(50, pos.getY());
        assertEquals(60, pos.getZ());
    }

    @Test
    public void canHandleBrokenChestId() {
        Chest chest = new Chest();
        chest.id = "";

        List<BlockPos> positions = chest.getBlockPositions();
        assertEquals(0, positions.size());
    }
}
