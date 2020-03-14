package de.henne90gen.chestcounter;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.henne90gen.chestcounter.service.dtos.Chest;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.BlockEvent;
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
    private Chest chest;

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
        if (!event.getWorld().isRemote()) {
            return;
        }

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
    public void place(BlockEvent.EntityPlaceEvent event) {
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
