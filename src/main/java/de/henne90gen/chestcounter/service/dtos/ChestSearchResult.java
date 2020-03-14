package de.henne90gen.chestcounter.service.dtos;

import java.util.LinkedHashMap;
import java.util.Map;

public class ChestSearchResult {
    public String search;
    public Map<String, Map<String, Integer>> byLabel = new LinkedHashMap<>();
    public Map<String, Map<String, Integer>> byId = new LinkedHashMap<>();
}
