package de.henne90gen.chestcounter.service.dtos;

import java.util.LinkedHashMap;
import java.util.Map;

public class Chest {
	public String worldId;
	public String id;
	public String label;
	public Map<String, Integer> items = new LinkedHashMap<>();
}
