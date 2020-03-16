package de.henne90gen.chestcounter.event;

import de.henne90gen.chestcounter.ChestCounter;
import de.henne90gen.chestcounter.Helper;
import de.henne90gen.chestcounter.service.dtos.Chest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ChestEventHandler {

	private static final Logger LOGGER = LogManager.getLogger();

	private final ChestCounter mod;

	public ChestEventHandler(ChestCounter mod) {
		this.mod = mod;
	}

	@SubscribeEvent
	public void tick(TickEvent.ClientTickEvent event) {
		Minecraft mc = Minecraft.getInstance();
		ClientWorld world = mc.world;
		if (world == null || !world.isRemote()) {
			return;
		}

		// TODO this might become a performance bottleneck
		//  we might be able to do it faster by striping the list and only checking one stripe per tick
		List<Chest> chests = mod.chestService.getChests(Helper.getWorldID());
		for (Chest chest : chests) {
			List<BlockPos> positions = chest.getBlockPositions();
			for (BlockPos pos : positions) {
				TileEntity tileEntity = world.getTileEntity(pos);
				if (tileEntity instanceof ChestTileEntity) {
					// it would be nice to update the chests right here, but sadly we only get empty chests from the world
					continue;
				}

				mod.chestService.delete(chest.worldId, chest.id);
			}
		}
	}
}
