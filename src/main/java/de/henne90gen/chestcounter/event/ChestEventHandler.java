package de.henne90gen.chestcounter.event;

import de.henne90gen.chestcounter.ChestCounter;
import de.henne90gen.chestcounter.Helper;
import de.henne90gen.chestcounter.service.dtos.Chest;
import net.minecraft.client.gui.widget.ToggleWidget;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.client.event.PlayerSPPushOutOfBlocksEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Objects;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ChestEventHandler {

	private static final Logger LOGGER = LogManager.getLogger();

	private final ChestCounter mod;
	private Chest chest;

	private BlockPos lastClickedPos;
	private int clickCounter = 0;

	public ChestEventHandler(ChestCounter mod) {
		this.mod = mod;
	}

	@SubscribeEvent
	public void close(GuiOpenEvent event) {
		if (event.getGui() == null && chest != null) {
			Helper.runInThread(() -> {
				mod.chestService.save(chest);
				chest = null;
			});
		}
	}

	@SubscribeEvent
	public void harvestBlock(BlockEvent.BreakEvent event) {
		// TODO this event is not being fired on the client
		if (!event.getWorld().isRemote()) {
			return;
		}
		LOGGER.info("BreakEvent: " + event.getClass());

		TileEntity tileEntity = event.getWorld().getTileEntity(event.getPos());
		if (tileEntity instanceof ChestTileEntity) {
			String worldId = Helper.getWorldID();
			String chestId = Helper.getChestId(Collections.singletonList(event.getPos()));

			LOGGER.debug("Destroyed chest " + chestId);
			mod.chestService.delete(worldId, chestId);

			BlockPos[] positions = {
					event.getPos().north(),
					event.getPos().east(),
					event.getPos().south(),
					event.getPos().west()
			};
			for (BlockPos position : positions) {
				TileEntity entity = event.getWorld().getTileEntity(position);
				if (entity instanceof ChestTileEntity) {
					Chest chest = new Chest();
					chest.worldId = Helper.getWorldID();
					chest.id = Helper.getChestId(Collections.singletonList(position));
					Helper.runInThread(() -> mod.chestService.save(chest));
					break;
				}
			}
		}
	}

	@SubscribeEvent
	public void block(BlockEvent event) {
		if (event.getWorld().isRemote()) {
			LOGGER.debug("BlockEvent client: " + event.getClass());
		}
	}

	@SubscribeEvent
	public void network(NetworkEvent event) {
		if (Objects.requireNonNull(event.getSource().get().getSender()).world.isRemote()) {
			LOGGER.debug("NetworkEvent client: " + event.getClass());
		} else {
			LOGGER.debug("NetworkEvent server: " + event.getClass());
		}
	}

	@SubscribeEvent
	public void playerInteract(PlayerInteractEvent event) {
//		if (event.getWorld().isRemote()) {
//			LOGGER.debug("PlayerInteractEvent client: " + event.getClass());
//		}
	}

	@SubscribeEvent
	public void player(PlayerEvent event) {
		if (event instanceof InputUpdateEvent ||
				event instanceof PlayerSPPushOutOfBlocksEvent) {
			return;
		}
		if (event.getPlayer() == null) {
			return;
		}
		if (event.getPlayer().getEntityWorld().isRemote()) {
//			LOGGER.info("PlayerEvent client: " + event.getClass());
			if (event instanceof PlayerInteractEvent.LeftClickBlock) {
//				PlayerInteractEvent.LeftClickBlock leftClick = (PlayerInteractEvent.LeftClickBlock) event;
//				BlockPos pos = leftClick.getPos();
//				if (lastClickedPos == null) {
//					lastClickedPos = pos;
//					clickCounter = 0;
//				}
//				if (lastClickedPos.getX() == pos.getX() &&
//						lastClickedPos.getY() == pos.getY() &&
//						lastClickedPos.getZ() == pos.getZ()) {
//					clickCounter++;
//				} else {
//					lastClickedPos = pos;
//					clickCounter = 0;
//				}
//
//				LOGGER.info("ClickCounter: " + clickCounter);
//				if (clickCounter >= 15) {
//					LOGGER.info("Broke block at (" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")");
//					clickCounter = 0;
//					lastClickedPos = null;
//				}
			}
		}
	}

	@SubscribeEvent
	public void place(BlockEvent.EntityPlaceEvent event) {
		// TODO this event is not being fired on the client
		if (!event.getWorld().isRemote()) {
			return;
		}

		TileEntity tileEntity = event.getWorld().getTileEntity(event.getPos());
		if (tileEntity instanceof ChestTileEntity) {
			Chest chest = new Chest();
			chest.worldId = Helper.getWorldID();
			chest.id = Helper.getChestId(event.getWorld(), event.getPos());
			LOGGER.debug("Placed chest " + chest.id);
			Helper.runInThread(() -> mod.chestService.save(chest));
		}
	}
}
