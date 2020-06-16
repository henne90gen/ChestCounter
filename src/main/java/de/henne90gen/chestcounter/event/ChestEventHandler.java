package de.henne90gen.chestcounter.event;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.henne90gen.chestcounter.ChestCounter;
import de.henne90gen.chestcounter.Helper;
import de.henne90gen.chestcounter.Renderer;
import de.henne90gen.chestcounter.db.entities.ChestConfig;
import de.henne90gen.chestcounter.db.entities.SearchResultPlacement;
import de.henne90gen.chestcounter.service.dtos.Chest;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.util.List;

import static de.henne90gen.chestcounter.ChestCounter.MOD_NAME;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ChestEventHandler {

    private static final Logger LOGGER = LogManager.getLogger();

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
        ChestConfig config = mod.chestService.getConfig();
        if (!config.enabled) {
            return;
        }

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

                Block block = world.getBlockState(pos).getBlock();
                if (Helper.isContainerBlock(block)) {
                    // it would be nice to update the chests right here, but sadly we only get empty chests from the world
                    continue;
                }

                String chestId = Helper.getChestId(mc.world, pos);
                mod.chestService.delete(chest.worldId, chestId);
            }
        }
    }

    @SubscribeEvent
    public void keyPressed(InputEvent.KeyInputEvent event) {
        if (event.getAction() != GLFW.GLFW_RELEASE) {
            return;
        }

        LOGGER.info("KEY: {}", event.getKey());
        ChestConfig config = mod.chestService.getConfig();

        checkToggleMod(config);
        checkShowSearchResultInInventory(config);
        checkShowSearchResultInGame(config);

        mod.chestService.setConfig(config);
    }

    private void checkShowSearchResultInInventory(ChestConfig config) {
        if (mod.showSearchResultInInventory.isPressed()) {
            config.showSearchResultInInventory = !config.showSearchResultInInventory;
            if (config.showSearchResultInInventory) {
                sendChatMessage("Inventory search result visible");
            } else {
                sendChatMessage("Inventory search result hidden");
            }
        }
    }

    private void checkShowSearchResultInGame(ChestConfig config) {
        if (mod.showSearchResultInGame.isPressed()) {
            config.showSearchResultInGame = !config.showSearchResultInGame;
            if (config.showSearchResultInGame) {
                sendChatMessage("Game search result visible");
            } else {
                sendChatMessage("Game search result hidden");
            }
        }
    }

    private void checkToggleMod(ChestConfig config) {
        if (mod.toggleModEnabled.isPressed()) {
            config.enabled = !config.enabled;
            if (config.enabled) {
                sendChatMessage("Enabled");
            } else {
                sendChatMessage("Disabled");
            }
        }
    }

    private void sendChatMessage(String message) {
        String finalMessage = "[" + MOD_NAME + "] " + message;
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().world != null && Minecraft.getInstance().world.isRemote) {
            Minecraft.getInstance().player.sendChatMessage(finalMessage);
        }
    }

    @SubscribeEvent
    public void blockClicked(PlayerInteractEvent.RightClickBlock event) {
        ChestConfig config = mod.chestService.getConfig();
        if (!config.enabled) {
            return;
        }

        if (!event.getWorld().isRemote()) {
            return;
        }

        BlockPos pos = event.getPos();
        World world = event.getWorld();
        TileEntity tileEntity = world.getTileEntity(pos);
        if (!Helper.isContainerTileEntity(tileEntity)) {
            return;
        }

        String worldId = Helper.getWorldID();
        String chestId = Helper.getChestId(world, pos);
        mod.currentChest = mod.chestService.getChest(worldId, chestId);
        LOGGER.debug("Setting current chest to: " + mod.currentChest.label + "(" + mod.currentChest.id + ")");
    }

    @SubscribeEvent
    public void renderWorld(RenderWorldLastEvent event) {
        ChestConfig config = mod.chestService.getConfig();
        if (!config.enabled) {
            return;
        }

        List<Chest> chests = mod.chestService.getChests(Helper.getWorldID());
        if (chests == null) {
            return;
        }

        float maxDistance = 10.0F;
        float partialTickTime = event.getPartialTicks();
        MatrixStack matrixStack = event.getMatrixStack();
        Renderer.renderChestLabels(chests, mod.lastSearchResult, maxDistance, partialTickTime, matrixStack);
    }

    @SubscribeEvent
    public void renderGameOverlay(RenderGameOverlayEvent.Text event) {
        ChestConfig config = mod.chestService.getConfig();
        if (!config.enabled || !config.showSearchResultInGame) {
            return;
        }

        Renderer.renderSearchResultInGame(config, mod.lastSearchResult);
    }
}
