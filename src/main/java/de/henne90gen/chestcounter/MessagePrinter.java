package de.henne90gen.chestcounter;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.henne90gen.chestcounter.dtos.AmountResult;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;
import java.util.Map;

public class MessagePrinter {

    private static boolean PRINT_TO_EVERYONE = false;

    private final CommandSource sender;

    public MessagePrinter(CommandSource sender) {
        this.sender = sender;
    }

    public static void togglePrintToEveryone() {
        PRINT_TO_EVERYONE = !PRINT_TO_EVERYONE;
    }

    public void printAmountsForLabel(Map<String, Integer> amount) {
        for (Map.Entry<String, Integer> entry : amount.entrySet()) {
            float numberOfStacks = entry.getValue() / 64.0f;
            String msg = "    " + entry.getKey() + ": " + entry.getValue() + " (" + numberOfStacks + ")";
            print(msg);
        }
    }

    public void printAmounts(Map<String, AmountResult> amountResultMap) {
        for (Map.Entry<String, AmountResult> entry : amountResultMap.entrySet()) {
            int amount = entry.getValue().amount;
            float numberOfStacks = amount / 64.0f;
            String labels = "";
            if (!entry.getValue().labels.isEmpty()) {
                labels = " (" + String.join(", ", entry.getValue().labels) + ")";
            }
            String msg = "  " + entry.getKey() + ": " + amount + " -> " + numberOfStacks + labels;
            print(msg);
        }
    }

    public void printLabels(Map<String, List<String>> labels) {
        for (Map.Entry<String, List<String>> entry : labels.entrySet()) {
            String label = entry.getKey();
            if (label.isEmpty()) {
                label = "No Label";
            }

            StringBuilder chestIDs = new StringBuilder();
            for (String chestID : entry.getValue()) {
                chestIDs.append("(").append(chestID).append("), ");
            }
            String chestIDsString = chestIDs.substring(0, chestIDs.length() - 2);

            print(label + ": " + chestIDsString);
        }
    }

    public void printUpdatedLabel(String label) {
        print("Updated label to " + label);
    }

    public void printNoData() {
        print("No data available");
    }

    public void printSomeError() {
        print("Something went wrong! Please try again.");
    }

    public void printToggle() {
        print("Toggled message sending");
    }

    public void printLookAtChestMessage() {
        print("Please look at a chest to update its label");
    }

    public void print(String msg) {
        try {
            // TODO differentiate between sending to everyone and sending to the current player only
            sender.asPlayer().sendMessage(new StringTextComponent(msg));
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            // TODO porper error handling
        }
    }
}
