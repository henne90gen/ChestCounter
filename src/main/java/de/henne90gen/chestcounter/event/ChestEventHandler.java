package de.henne90gen.chestcounter.event;

import de.henne90gen.chestcounter.ChestCounter;
import de.henne90gen.chestcounter.Helper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ChestEventHandler {

	private static final Logger LOGGER = LogManager.getLogger();

	private final ChestCounter mod;

	public ChestEventHandler(ChestCounter mod) {
		this.mod = mod;
	}

	@SubscribeEvent
	public void player(PlayerInteractEvent.LeftClickBlock event) {
		if (event.getPlayer() == null) {
			return;
		}
		if (!event.getWorld().isRemote()) {
			return;
		}

		String worldID = Helper.getWorldID();

		List<BlockPos> positions = Helper.getCubeAroundPosition(event.getPos());
		for (BlockPos pos : positions) {
			BlockState blockState = event.getPlayer().getEntityWorld().getBlockState(pos);
			Block block = blockState.getBlock();

			if (block instanceof ChestBlock) {
				continue;
			}

			String chestId = Helper.getChestId(Collections.singletonList(pos));
			mod.chestService.delete(worldID, chestId);
		}
	}
}
