package de.henne90gen.chestcounter.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.henne90gen.chestcounter.ChestCounter;
import de.henne90gen.chestcounter.Helper;
import de.henne90gen.chestcounter.MessagePrinter;
import de.henne90gen.chestcounter.dtos.AmountResult;
import javax.annotation.Nullable;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class ChestCommand implements ICommand {

	private final ChestCounter mod;

	public ChestCommand(ChestCounter mod) {
		this.mod = mod;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		MessagePrinter printer = new MessagePrinter(sender);

		if (args.length == 1) {
			amountOfItems(printer, args);
		} else if (args.length == 2) {
			amountOfItemsForLabel(printer, args);
		} else {
			printer.print(getUsage(sender));
		}
	}

	private void amountOfItemsForLabel(MessagePrinter printer, String[] args) {
		String label = args[0];
		Map<String, Integer> itemCounts = mod.chestService.getItemCountsForLabel(Helper.instance.getWorldID(),
				label);
		if (itemCounts == null) {
			printer.printSomeError();
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
			printer.printNoData();
			return;
		} else {
			printer.print(label + ":");
		}

		printer.printAmountsForLabel(amount);
	}

	private void amountOfItems(MessagePrinter printer, String[] args) {
		String queryString = args[0];
		Map<String, AmountResult> amount = mod.chestService.getItemCounts(Helper.instance.getWorldID(), queryString);

		if (amount.entrySet().size() == 0) {
			printer.printNoData();
			return;
		}

		printer.printAmounts(amount);
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
