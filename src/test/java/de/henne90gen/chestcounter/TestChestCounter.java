package de.henne90gen.chestcounter;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class TestChestCounter implements IChestCounter {

	private final String filename;

	public TestChestCounter(String filename) {
		this.filename = filename;
	}

	@Nonnull
	@Override
	public List<BlockPos> getChestPositions(World world, BlockPos position) {
		return Collections.emptyList();
	}

	@Override
	public String getWorldID() {
		return "test-world";
	}

	@Override
	public void logError(Exception e) {

	}

	@Override
	public void log(String msg) {

	}

	@Override
	public String getChestDBFilename() {
		return filename;
	}
}
