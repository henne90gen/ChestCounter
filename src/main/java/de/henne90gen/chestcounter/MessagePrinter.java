package de.henne90gen.chestcounter;

import java.util.Map;

import de.henne90gen.chestcounter.dtos.AmountResult;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentString;

public class MessagePrinter {

	private static boolean PRINT_TO_EVERYONE = false;

	private final ICommandSender sender;

	public MessagePrinter(ICommandSender sender) {
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
			String msg = entry.getKey() + ": " + amount + " -> " + numberOfStacks + " (" + String.join(", ",
					entry.getValue().labels) + ")";
			print(msg);
		}
	}

	public void print(String msg) {
		if (PRINT_TO_EVERYONE && sender instanceof EntityPlayerSP) {
			((EntityPlayerSP) sender).sendChatMessage(msg);
		} else {
			sender.sendMessage(new TextComponentString(msg));
		}
	}

	public void printNoData() {
		print("No data available");
	}

}
