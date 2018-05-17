package de.henne90gen.chestcounter;

import java.util.LinkedHashMap;
import java.util.Map;

public class ChestContainer {
    public Map<String, Chest> chests;

    public ChestContainer() {
        chests = new LinkedHashMap<>();
    }
}
