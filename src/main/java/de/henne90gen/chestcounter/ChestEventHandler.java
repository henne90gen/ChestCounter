package de.henne90gen.chestcounter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.IOException;
import java.util.*;

public class ChestEventHandler {

    private final List<BlockPos> chestPositions;

    public ChestEventHandler() {
        this.chestPositions = new ArrayList<>();
    }

    @SubscribeEvent
    public void open(GuiContainerEvent event) {
        if (shouldNotHandleEvent(event)) {
            return;
        }

        Minecraft mc = FMLClientHandler.instance().getClient();
        if (mc.currentScreen instanceof GuiContainer) {
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
    public void command(ClientChatEvent event) {
        if (event.getMessage().startsWith("/chest")) {
            String[] parts = event.getMessage().split(" ");
            if (parts.length != 2) {
                return;
            }
            String itemName = parts[1];
            try {
                ChestContainer chestContainer = ItemDB.loadChestContainer();
                if (chestContainer == null) {
                    return;
                }
                Map<String, Integer> amount = new LinkedHashMap<>();
                for (Map.Entry<String, Chest> chestEntry : chestContainer.chests.entrySet()) {
                    for (Map.Entry<String, Integer> itemEntry : chestEntry.getValue().items.entrySet()) {
                        if (itemEntry.getKey().toLowerCase().contains(itemName.toLowerCase())) {
                            Integer itemAmount = amount.get(itemEntry.getKey());
                            if (itemAmount == null) {
                                itemAmount = 0;
                            }
                            itemAmount += itemEntry.getValue();
                            amount.put(itemEntry.getKey(), itemAmount);
                        }
                    }
                }
                EntityPlayerSP player = Minecraft.getMinecraft().player;
                player.sendMessage(new TextComponentString("Amounts:"));
                for (Map.Entry<String, Integer> entry : amount.entrySet()) {
                    player.sendMessage(new TextComponentString("    " + entry.getKey() + ": " + entry.getValue()));
                }
                event.setCanceled(true);
            } catch (IOException e) {
                ChestCounter.logError(e);
            }
        }
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

    private boolean shouldNotHandleEvent(GuiContainerEvent event) {
        return event.getGuiContainer() == null ||
                event.getGuiContainer().mc == null ||
                event.getGuiContainer().mc.world == null ||
                !event.getGuiContainer().mc.world.isRemote ||
                chestPositions.isEmpty();
    }
}
