package de.henne90gen.chestcounter;

import java.util.LinkedHashMap;
import java.util.Map;

public class Chest {
    public String id;
    public Map<String, Integer> items;

    public Chest() {
        items = new LinkedHashMap<>();
    }
}
