package de.henne90gen.chestcounter;

import java.util.List;

import javax.annotation.Nonnull;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public interface IChestCounter {

	@Nonnull
	List<BlockPos> getChestPositions(World world, BlockPos position);

	String getWorldID();

	void logError(Exception e);

	void log(String msg);

	String getChestDBFilename();
}
