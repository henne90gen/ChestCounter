package de.henne90gen.chestcounter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.text.TextComponentString;

public class CommandHandler {
	public static void query(String message) {
		EntityPlayerSP player = Minecraft.getMinecraft().player;

		String[] parts = message.split(" ");
		if (parts.length != 2) {
			player.sendMessage(new TextComponentString("Usage: /chest [item name]"));
			return;
		}
		try {
			ChestContainer chestContainer = ItemDB.loadChestContainer();
			if (chestContainer == null) {
				player.sendMessage(new TextComponentString("No data available"));
				return;
			}

			String queryString = parts[1];
			Map<String, Integer> amount = gatherAmounts(chestContainer, queryString);

			printAmounts(player, amount);
		} catch (IOException e) {
			ChestCounter.logError(e);
		}
	}

	private static void printAmounts(EntityPlayerSP player, Map<String, Integer> amount) {
		player.sendMessage(new TextComponentString("Amounts:"));
		for (Map.Entry<String, Integer> entry : amount.entrySet()) {
			player.sendMessage(new TextComponentString("    " + entry.getKey() + ": " + entry.getValue()));
		}
	}

	private static Map<String, Integer> gatherAmounts(ChestContainer chestContainer, String queryString) {
		Map<String, Integer> amount = new LinkedHashMap<>();
		for (Map.Entry<String, Chest> chestEntry : chestContainer.chests.entrySet()) {
			for (Map.Entry<String, Integer> itemEntry : chestEntry.getValue().items.entrySet()) {
				if (itemEntry.getKey().toLowerCase().contains(queryString.toLowerCase())) {
					Integer itemAmount = amount.get(itemEntry.getKey());
					if (itemAmount == null) {
						itemAmount = 0;
					}
					itemAmount += itemEntry.getValue();
					amount.put(itemEntry.getKey(), itemAmount);
				}
			}
		}
		return amount;
	}
}
