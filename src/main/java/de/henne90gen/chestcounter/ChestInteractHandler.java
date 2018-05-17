package de.henne90gen.chestcounter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class ChestInteractHandler {

    private final Logger logger;

    private final List<BlockPos> chestPositions;

    public ChestInteractHandler(Logger logger) {
        this.logger = logger;
        this.chestPositions = new ArrayList<>();
    }

    @SubscribeEvent
    public void open(GuiContainerEvent event) {
        if (event.getGuiContainer() == null) {
            return;
        }
        if (event.getGuiContainer().mc == null) {
            return;
        }
        if (event.getGuiContainer().mc.world == null) {
            return;
        }
        if (!event.getGuiContainer().mc.world.isRemote) {
            return;
        }
        if (chestPositions.isEmpty()) {
            return;
        }

        Minecraft mc = FMLClientHandler.instance().getClient();
        if (mc.currentScreen != null && mc.currentScreen instanceof GuiContainer) {
            Container currentContainer = ((GuiContainer) mc.currentScreen).inventorySlots;
            Map<String, Integer> counter = new LinkedHashMap<>();
            for (int i = 0; i < currentContainer.inventorySlots.size() - 36; i++) {
                ItemStack stack = currentContainer.inventorySlots.get(i).getStack();
                int count = stack.getCount();
                String itemName = stack.getDisplayName();
                if ("Air".equals(itemName)) {
                    continue;
                }
                Integer currentCount = counter.get(itemName);
                if (currentCount == null) {
                    currentCount = 0;
                }
                currentCount += count;
                counter.put(itemName, currentCount);
            }

            Chest chest = new Chest();
            chest.id = ItemDB.buildID(chestPositions);
            chest.items = counter;
            ItemDB.save(chest);
        }

        chestPositions.clear();
    }

    @SubscribeEvent
    public void interact(PlayerInteractEvent event) {
        if (!event.getWorld().isRemote) {
            return;
        }

        chestPositions.clear();
        addChestPosition(event, event.getPos());
        addChestPosition(event, event.getPos().north());
        addChestPosition(event, event.getPos().east());
        addChestPosition(event, event.getPos().south());
        addChestPosition(event, event.getPos().west());
    }

    @SubscribeEvent
    public void harvestBlock(BlockEvent.BreakEvent event) {
        TileEntity tileEntity = event.getWorld().getTileEntity(event.getPos());
        if (tileEntity instanceof TileEntityChest) {
            ItemDB.delete(ItemDB.buildID(Collections.singletonList(event.getPos())));
        }
    }

    private void addChestPosition(PlayerInteractEvent event, BlockPos position) {
        TileEntity tileEntity = event.getWorld().getTileEntity(position);
        if (tileEntity instanceof TileEntityChest) {
            chestPositions.add(position);
        }
    }
}
