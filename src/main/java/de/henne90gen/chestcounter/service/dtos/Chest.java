package de.henne90gen.chestcounter.service.dtos;

import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Chest {

    private static final Logger LOGGER = LogManager.getLogger();

    public String worldId;
    public String id;
    public String label;
    public Map<String, Integer> items = new LinkedHashMap<>();

    public List<BlockPos> getBlockPositions() {
        ArrayList<BlockPos> positions = new ArrayList<>();
        String[] parts = id.split(":");
        for (String part : parts) {
            try {
                String[] split = part.split(",");
                int x = Integer.parseInt(split[0]);
                int y = Integer.parseInt(split[1]);
                int z = Integer.parseInt(split[2]);
                positions.add(new BlockPos(x, y, z));
            } catch (NumberFormatException e) {
                LOGGER.warn("Could not create position for " + part + "(" + this + ")");
            }
        }
        return positions;
    }

    @Override
    public String toString() {
        return "Chest{" +
                "worldId='" + worldId + '\'' +
                ", id='" + id + '\'' +
                ", label='" + label + '\'' +
                ", items=" + items +
                '}';
    }
}
