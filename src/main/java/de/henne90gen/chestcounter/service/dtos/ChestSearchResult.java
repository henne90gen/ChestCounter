package de.henne90gen.chestcounter.service.dtos;

import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ChestSearchResult {
	public static class Entry {
		public List<Vec3d> positions = new ArrayList<>();
		public Map<String, Integer> items = new LinkedHashMap<>();
	}

	/**
     * The search text.
     */
    public String search = "";

	/**
	 * Map of chest labels to the items they contain.
	 */
	public Map<String, Entry> byLabel = new LinkedHashMap<>();

	/**
	 * Map of chest ids to the items they contain.
	 */
	public Map<String, Entry> byId = new LinkedHashMap<>();

}
