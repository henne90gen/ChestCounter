package de.henne90gen.chestcounter;

import java.util.List;

import javax.annotation.Nonnull;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public interface IChestCounter {

	@Nonnull
	List<BlockPos> getChestPositions(PlayerInteractEvent event);

	String getWorldID();

	void logError(Exception e);

	void log(String msg);

	String getChestDBFilename();
}
