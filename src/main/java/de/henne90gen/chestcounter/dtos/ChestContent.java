package de.henne90gen.chestcounter.dtos;

import java.util.LinkedHashMap;
import java.util.Map;

public class ChestContent {
	public Map<String, Integer> items;

	public ChestContent() {
		items = new LinkedHashMap<>();
	}
}
