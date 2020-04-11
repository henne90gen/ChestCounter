package de.henne90gen.chestcounter.event;

import de.henne90gen.chestcounter.ChestCounter;
import de.henne90gen.chestcounter.Helper;
import de.henne90gen.chestcounter.service.dtos.Chest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ChestEventHandler {

    // TODO make this configurable
    private static final int CHECK_FOR_CHESTS_EVERY_X_TICKS = 20;
    private static final double MAX_DISTANCE_SQ = 50.0 * 50.0;

    private final ChestCounter mod;
    private int tickCount = 0;

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

        tickCount++;
        if (tickCount % CHECK_FOR_CHESTS_EVERY_X_TICKS != 0) {
            return;
        }

        if (mc.player == null) {
            return;
        }

        double playerX = mc.player.lastTickPosX + (mc.player.getPosX() - mc.player.lastTickPosX);
        double playerY = mc.player.lastTickPosY + (mc.player.getPosY() - mc.player.lastTickPosY);
        double playerZ = mc.player.lastTickPosZ + (mc.player.getPosZ() - mc.player.lastTickPosZ);

        // TODO this might become a performance bottleneck
        //  we might be able to do it faster by striping the list and only checking one stripe per tick
        List<Chest> chests = mod.chestService.getChests(Helper.getWorldID());
        for (Chest chest : chests) {
            List<BlockPos> positions = chest.getBlockPositions();
            for (BlockPos pos : positions) {
                double distanceSq = pos.distanceSq(playerX, playerY, playerZ, true);
				if (distanceSq > MAX_DISTANCE_SQ) {
                    continue;
                }

                TileEntity tileEntity = world.getTileEntity(pos);
                if (Helper.isContainerTileEntity(tileEntity)) {
                    // it would be nice to update the chests right here, but sadly we only get empty chests from the world
                    continue;
                }

                mod.chestService.delete(chest.worldId, chest.id);
            }
        }
    }
}
