package de.henne90gen.chestcounter.commands;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.henne90gen.chestcounter.ChestCounter;
import de.henne90gen.chestcounter.ChestDB;
import de.henne90gen.chestcounter.dtos.ChestContent;
import de.henne90gen.chestcounter.dtos.ChestWorlds;
import de.henne90gen.chestcounter.dtos.Chests;

import javax.annotation.Nullable;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.client.FMLClientHandler;

public class ChestCommand implements ICommand {

	private final ChestCounter mod;
	private final ChestDB chestDB;

	public ChestCommand(ChestCounter mod) {
		this.mod = mod;
		this.chestDB = new ChestDB(mod);
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		EntityPlayerSP player = FMLClientHandler.instance().getClient().player;

		if (args.length != 1) {
			player.sendMessage(new TextComponentString(getUsage(sender)));
			return;
		}
		try {
			ChestWorlds worlds = chestDB.readChestWorlds();
			if (worlds == null) {
				printNoData(player);
				return;
			}

			Chests world = worlds.get(mod.getWorldID());
			if (world == null) {
				printNoData(player);
				return;
			}

			String queryString = args[0];
			Map<String, Integer> amount = gatherAmounts(world, queryString);

			if (amount.entrySet().size() == 0) {
				printNoData(player);
				return;
			}

			printAmounts(player, amount);
		} catch (IOException e) {
			mod.logError(e);
		}
	}

	private void printNoData(EntityPlayerSP player) {
		player.sendMessage(new TextComponentString("No data available"));
	}

	private void printAmounts(EntityPlayerSP player, Map<String, Integer> amount) {
		for (Map.Entry<String, Integer> entry : amount.entrySet()) {
			float numberOfStacks = entry.getValue() / 64.0f;
			String msg = entry.getKey() + ": " + entry.getValue() + " (" + numberOfStacks + ")";
			player.sendMessage(new TextComponentString(msg));
		}
	}

	private Map<String, Integer> gatherAmounts(Chests chests, String queryString) {
		Map<String, Integer> amount = new LinkedHashMap<>();
		for (Map.Entry<String, ChestContent> chestEntry : chests.entrySet()) {
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
		return Collections.emptyList();
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return true;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
			@Nullable BlockPos targetPos)
	{
		return Collections.emptyList();
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
