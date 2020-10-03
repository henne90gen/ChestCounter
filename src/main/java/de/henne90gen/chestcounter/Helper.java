package de.henne90gen.chestcounter;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.state.properties.ChestType;
import net.minecraft.tileentity.BarrelTileEntity;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.IWorld;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Helper {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final int INVENTORY_SIZE = 36;

    public static String formatPositionsToChestId(List<BlockPos> positions) {
        // copy and sort incoming list
        positions = new ArrayList<>(positions);
        positions.sort(getBlockPosComparator());

        List<String> positionStrings = positions.stream()
                .map(blockPos -> blockPos.getX() + "," + blockPos.getY() + "," + blockPos.getZ())
                .collect(Collectors.toList());
        return String.join(":", positionStrings);
    }

    public static List<BlockPos> extractPositionsFromChestId(String chestId) {
        if (chestId == null) {
            LOGGER.warn("Could not get positions from chest id. (null)");
            return Collections.emptyList();
        }

        String[] parts = chestId.split(":");
        List<BlockPos> positions = new ArrayList<>();
        for (String part : parts) {
            String[] coords = part.split(",");
            if (coords.length != 3) {
                LOGGER.warn("Could not parse positions from chest id. ({}}", chestId);
                continue;
            }

            int x = Integer.parseInt(coords[0]);
            int y = Integer.parseInt(coords[1]);
            int z = Integer.parseInt(coords[2]);
            BlockPos position = new BlockPos(x, y, z);
            positions.add(position);
        }

        return positions;
    }

    public static Comparator<BlockPos> getBlockPosComparator() {
        return (block, other) -> {
            if (block.getX() < other.getX()) {
                return -1;
            } else if (block.getX() == other.getX()) {
                if (block.getY() < other.getY()) {
                    return -1;
                } else if (block.getY() == other.getY()) {
                    if (block.getZ() < other.getZ()) {
                        return -1;
                    } else if (block.getZ() == other.getZ()) {
                        return 0;
                    }
                }
            }
            return 1;
        };
    }

    @Nonnull
    public static String getWorldID() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.world == null) {
            return "";
        }

        DimensionType dimension = mc.world.getDimensionType();
        String dimensionStr = "unknown";
        try {
            Field dimensionIdField = DimensionType.class.
                    getDeclaredField("field_236010_o_");
            dimensionIdField.setAccessible(true);
            long dimensionId = ((OptionalLong) dimensionIdField.get(dimension)).orElseGet(() -> 0L);
            if (dimensionId == 0) {
                dimensionStr = "DimensionType{minecraft:overworld}";
            } else if (dimensionId == 18000L) {
                dimensionStr = "DimensionType{minecraft:the_nether}";
            } else if (dimensionId == 6000L) {
                dimensionStr = "DimensionType{minecraft:the_end}";
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LOGGER.error("Could not find out which dimension the player is in.");
        }

        String worldName;
        ServerData currentServerData = mc.getCurrentServerData();
        if (currentServerData != null) {
            worldName = currentServerData.serverIP;
        } else {
            if (mc.world != null) {
                MinecraftServer server = mc.world.getServer();
                if (server != null) {
                    worldName = server.getName();
                } else {
                    worldName = "default";
                }
            } else {
                worldName = "default";
            }
        }

        return worldName + ":" + dimensionStr;
    }

    public static String createDefaultLabel(String chestId) {
        if (chestId == null || chestId.isEmpty()) {
            return "";
        }
        return chestId.replace(",", " ");
    }

    public static String getChestId(IWorld world, BlockPos position) {
        BlockState blockState = world.getBlockState(position);
        Block block = blockState.getBlock();
        if (!(block instanceof ChestBlock)) {
            // barrels, ...
            return formatPositionsToChestId(Collections.singletonList(position));
        }

        ChestType chestType = blockState.get(ChestBlock.TYPE);
        if (chestType == ChestType.SINGLE) {
            return formatPositionsToChestId(Collections.singletonList(position));
        }

        List<BlockPos> chestPositions = new ArrayList<>();
        chestPositions.add(position);

        BlockPos[] positions = {
                position.north(),
                position.east(),
                position.south(),
                position.west()
        };
        Direction direction = blockState.get(ChestBlock.FACING);
        for (BlockPos pos : positions) {
            BlockState otherBlockState = world.getBlockState(pos);
            if (!(otherBlockState.getBlock() instanceof ChestBlock)) {
                continue;
            }

            ChestType otherChestType = otherBlockState.get(ChestBlock.TYPE);
            Direction otherDirection = otherBlockState.get(ChestBlock.FACING);
            if (direction == otherDirection && otherChestType == chestType.opposite()) {
                chestPositions.add(pos);
            }
        }
        return formatPositionsToChestId(chestPositions);
    }

    public static boolean isContainerTileEntity(TileEntity tileEntity) {
        return tileEntity instanceof ChestTileEntity || tileEntity instanceof BarrelTileEntity;
    }

    public static boolean isContainerBlock(Block block) {
        return block instanceof ChestBlock || block instanceof BarrelBlock;
    }

    public static Stream<Pair<String, Integer>> inventoryIterator(Container container) {
        Iterator<Pair<String, Integer>> iterator = new Iterator<Pair<String, Integer>>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < container.inventorySlots.size() - INVENTORY_SIZE;
            }

            @Override
            public Pair<String, Integer> next() {
                ItemStack itemStack = container.inventorySlots.get(i++).getStack();
                return new Pair<>(itemStack.getDisplayName().getString(), itemStack.getCount());
            }
        };
        Spliterator<Pair<String, Integer>> spliterator = Spliterators.spliteratorUnknownSize(iterator, 0);
        return StreamSupport.stream(spliterator, false);
    }

    public static Map<String, Integer> countItems(Stream<Pair<String, Integer>> inventory) {
        return inventory
                .filter(is -> !"Air".equals(is.getFirst()))
                .collect(Collectors.toMap(
                        Pair::getFirst,
                        Pair::getSecond,
                        Integer::sum
                ));
    }

    public static List<BlockPos> getCubeAroundPosition(BlockPos pos) {
        List<BlockPos> positions = new ArrayList<>();

        positions.add(pos);

        positions.add(pos.up());
        positions.add(pos.down());
        positions.add(pos.south());
        positions.add(pos.north());
        positions.add(pos.east());
        positions.add(pos.west());
        positions.add(pos.south().west());
        positions.add(pos.south().east());
        positions.add(pos.north().west());
        positions.add(pos.north().east());

        positions.add(pos.up().south());
        positions.add(pos.up().north());
        positions.add(pos.up().east());
        positions.add(pos.up().west());
        positions.add(pos.up().south().west());
        positions.add(pos.up().south().east());
        positions.add(pos.up().north().west());
        positions.add(pos.up().north().east());

        positions.add(pos.down().south());
        positions.add(pos.down().north());
        positions.add(pos.down().east());
        positions.add(pos.down().west());
        positions.add(pos.down().south().west());
        positions.add(pos.down().south().east());
        positions.add(pos.down().north().west());
        positions.add(pos.down().north().east());

        return positions;
    }

    @Nonnull
    public static BlockPos getClosestPositionToPlayer(List<BlockPos> positions, float partialTickTime) {
        // TODO test this method
        if (positions.isEmpty()) {
            return new BlockPos(0, 0, 0);
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return positions.get(0);
        }

        float playerX = (float) (mc.player.lastTickPosX + (mc.player.getPosX() - mc.player.lastTickPosX) * partialTickTime);
        float playerY = (float) (mc.player.lastTickPosY + (mc.player.getPosY() - mc.player.lastTickPosY) * partialTickTime);
        float playerZ = (float) (mc.player.lastTickPosZ + (mc.player.getPosZ() - mc.player.lastTickPosZ) * partialTickTime);

        BlockPos closestPos = positions.get(0);
        float closestDistance = Float.MAX_VALUE;
        for (BlockPos pos : positions) {
            float dx = (pos.getX() - playerX) + 0.5F;
            float dy = (pos.getY() - playerY) + 0.5F;
            float dz = (pos.getZ() - playerZ) + 0.5F;
            float distance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance < closestDistance) {
                closestPos = pos;
                closestDistance = distance;
            }
        }
        return closestPos;
    }
}
