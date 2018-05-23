package de.henne90gen.chestcounter.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.henne90gen.chestcounter.ChestCounter;
import de.henne90gen.chestcounter.Helper;
import javax.annotation.Nullable;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.client.FMLClientHandler;

public class ChestQueryCommand implements ICommand {

	private final ChestCounter mod;

	public ChestQueryCommand(ChestCounter mod) {
		this.mod = mod;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		EntityPlayerSP player = FMLClientHandler.instance().getClient().player;

		if (args.length < 1) {
			player.sendMessage(new TextComponentString(getUsage(sender)));
			return;
		}
		try {
			String label = args[0];
			Map<String, Integer> itemCounts = mod.chestDB.getItemCountsForLabel(Helper.instance.getWorldID(), label);
			mod.log("Query results: " + itemCounts);

			if (itemCounts.size() == 0) {
				player.sendMessage(new TextComponentString("No data available"));
				return;
			} else {
				player.sendMessage(new TextComponentString(label + ":"));
			}

			if (args.length == 1) {
				printAmounts(player, itemCounts);
			} else {
				String itemName = args[1];
				Map<String, Integer> amount = itemCounts.entrySet()
						.stream()
						.filter(entry -> entry.getKey().contains(itemName)
								|| itemName.contains(entry.getKey()))
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
				printAmounts(player, amount);
			}
		} catch (IOException e) {
			player.sendMessage(new TextComponentString("Something went wrong! Please try again."));
			mod.logError(new NullPointerException("getItemCountsForLabel returned null"));
		}
	}

	private void printAmounts(EntityPlayerSP player, Map<String, Integer> amount) {
		for (Map.Entry<String, Integer> entry : amount.entrySet()) {
			float numberOfStacks = entry.getValue() / 64.0f;
			String msg = "    " + entry.getKey() + ": " + entry.getValue() + " (" + numberOfStacks + ")";
			player.sendMessage(new TextComponentString(msg));
		}
	}

	@Override
	public String getName() {
		return "chestquery";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "Usage: /chestquery [label] [itemname]";
	}

	@Override
	public List<String> getAliases() {
		String[] aliases = { "cq", "chestq" };
		return Arrays.asList(aliases);
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
