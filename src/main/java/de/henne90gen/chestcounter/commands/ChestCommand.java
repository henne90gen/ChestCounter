package de.henne90gen.chestcounter.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.henne90gen.chestcounter.ChestCounter;
import de.henne90gen.chestcounter.Helper;
import de.henne90gen.chestcounter.dtos.AmountResult;
import javax.annotation.Nullable;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public class ChestCommand implements ICommand {

	private final ChestCounter mod;

	public ChestCommand(ChestCounter mod) {
		this.mod = mod;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length == 1) {
			amountOfItems(sender, args);
		} else if (args.length == 2) {
			amountOfItemsForLabel(sender, args);
		} else {
			printUsage(sender);
		}
	}

	private void amountOfItemsForLabel(ICommandSender sender, String[] args) {
		String label = args[0];
		Map<String, Integer> itemCounts = mod.chestService.getItemCountsForLabel(Helper.instance.getWorldID(),
				label);
		if (itemCounts == null) {
			sender.sendMessage(new TextComponentString("Something went wrong! Please try again."));
			return;
		}

		mod.log("Query results: " + itemCounts);

		String itemName = args[1].toLowerCase();
		Map<String, Integer> amount = itemCounts.entrySet()
				.stream()
				.filter(entry -> entry.getKey().toLowerCase().contains(itemName)
						|| itemName.contains(entry.getKey().toLowerCase()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		if (amount.size() == 0) {
			printNoData(sender);
			return;
		} else {
			sender.sendMessage(new TextComponentString(label + ":"));
		}

		printAmountsForLabel(sender, amount);
	}

	private void printAmountsForLabel(ICommandSender sender, Map<String, Integer> amount) {
		for (Map.Entry<String, Integer> entry : amount.entrySet()) {
			float numberOfStacks = entry.getValue() / 64.0f;
			String msg = "    " + entry.getKey() + ": " + entry.getValue() + " (" + numberOfStacks + ")";
			sender.sendMessage(new TextComponentString(msg));
		}
	}

	private void amountOfItems(ICommandSender sender, String[] args) {
		String queryString = args[0];
		Map<String, AmountResult> amount = mod.chestService.getItemCounts(Helper.instance.getWorldID(), queryString);

		if (amount.entrySet().size() == 0) {
			printNoData(sender);
			return;
		}

		printAmounts(sender, amount);
	}

	private void printUsage(ICommandSender sender) {
		sender.sendMessage(new TextComponentString(getUsage(sender)));
	}

	private void printNoData(ICommandSender sender) {
		sender.sendMessage(new TextComponentString("No data available"));
	}

	private void printAmounts(ICommandSender sender, Map<String, AmountResult> amountResultMap) {
		for (Map.Entry<String, AmountResult> entry : amountResultMap.entrySet()) {
			int amount = entry.getValue().amount;
			float numberOfStacks = amount / 64.0f;
			String msg = entry.getKey() + ": " + amount + " -> " + numberOfStacks + " (" + String.join(", ",
					entry.getValue().labels) + ")";
			sender.sendMessage(new TextComponentString(msg));
		}
	}

	@Override
	public String getName() {
		return "chest";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "Usage: /chest [item name]";
	}

	@Override
	public List<String> getAliases() {
		return new ArrayList<>(Collections.singletonList("c"));
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return true;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
			@Nullable BlockPos targetPos)
	{
		return new ArrayList<>();
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return false;
	}

	@Override
	public int compareTo(ICommand iCommand) {
		return 0;
	}
}
