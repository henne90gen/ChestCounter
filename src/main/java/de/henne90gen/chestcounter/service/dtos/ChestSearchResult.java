package de.henne90gen.chestcounter.service.dtos;

import net.minecraft.util.math.vector.Vector3d;

import java.util.*;

public class ChestSearchResult {

    /**
     * The search text.
     */
    public String search = "";

    /**
     * Map of chest labels to the items they contain.
     */
    public Map<Key, Value> byLabel = new LinkedHashMap<>();

    /**
     * Map of chest ids to the items they contain.
     */
    public Map<Key, Value> byId = new LinkedHashMap<>();

    public static class Value {
        public List<Vector3d> positions = new ArrayList<>();
        public Map<String, Integer> items = new LinkedHashMap<>();
    }

    public static Key keyId(String id) {
        return new Key(id, true);
    }

    public static Key keyLabel(String label) {
        return new Key(label, false);
    }

    public static class Key {
        public final String key;
        public final boolean isId;

        public Key(String key, boolean isId) {
            this.key = key;
            this.isId = isId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key key1 = (Key) o;
            return key.equals(key1.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key);
        }
    }
}
