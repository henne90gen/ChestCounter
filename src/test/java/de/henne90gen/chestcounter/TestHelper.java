package de.henne90gen.chestcounter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TestHelper {
    public static void writeTestFile(File file, String worldID, String chestID, String chestLabel, String itemName, int itemAmount)
            throws IOException {
        FileWriter writer = new FileWriter(file);

        writer.write("{" +
                "\"version\": 1," +
                "\"worlds\": {\""
                + worldID
                + "\":{\""
                + chestID
                + "\":{\"items\":{\"" + itemName + "\":" + itemAmount + "},\"label\":\"" + chestLabel + "\"}}}}");
        writer.close();
    }
}
