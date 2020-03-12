package de.henne90gen.chestcounter.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.henne90gen.chestcounter.ChestCounter;
import de.henne90gen.chestcounter.Helper;
import de.henne90gen.chestcounter.MessagePrinter;
import de.henne90gen.chestcounter.service.dtos.ChestSearchResult;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.stream.Collectors;

public class ChestCommand {

	private static final Logger LOGGER = LogManager.getLogger();

	public static void register(ChestCounter mod, CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
				Commands.literal("chest")
						.then(Commands.argument("item", StringArgumentType.string()))
						.executes(context -> {
							MessagePrinter printer = new MessagePrinter(context.getSource());
							String itemName = StringArgumentType.getString(context, "item");
							if (itemName.isEmpty()) {
								amountOfAllItems(mod, printer);
							} else {
								amountOfItems(mod, printer, itemName);
							}
//        else if (args.length == 2) {
//            amountOfItemsForLabel(printer, args);
//        } else {
//            printer.print(getUsage(sender));
//        }
							return 0;
						}));
	}

	private static void amountOfItemsForLabel(ChestCounter mod, MessagePrinter printer, String[] args) {
		String label = args[0];
		Map<String, Integer> itemCounts = mod.chestService.getItemCountsForLabel(Helper.instance.getWorldID(),
				label);
		if (itemCounts == null) {
			printer.printSomeError();
			return;
		}

		LOGGER.info("Query results: " + itemCounts);

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

	private static void amountOfAllItems(ChestCounter mod, MessagePrinter printer) {
		amountOfItems(mod, printer, "");
	}

	private static void amountOfItems(ChestCounter mod, MessagePrinter printer, String queryString) {
		ChestSearchResult amount = mod.chestService.getItemCounts(Helper.instance.getWorldID(), queryString);

		if (!queryString.isEmpty()) {
			printer.print("Search result for '" + queryString + "':");
		} else {
			printer.print("All available items:");
		}

		if (amount.entrySet().size() == 0) {
			printer.printNoData();
			return;
		}

		printer.printAmounts(amount);
	}

// TODO clean this up
//    @Override
//    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
//        return true;
//    }
}
