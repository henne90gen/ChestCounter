package de.henne90gen.chestcounter.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.henne90gen.chestcounter.ChestCounter;
import de.henne90gen.chestcounter.Helper;
import de.henne90gen.chestcounter.dtos.AmountResult;
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

	public ChestCommand(ChestCounter mod) {
		this.mod = mod;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		EntityPlayerSP player = FMLClientHandler.instance().getClient().player;

		if (args.length != 1) {
			player.sendMessage(new TextComponentString(getUsage(sender)));
			return;
		}
		String queryString = args[0];
		Map<String, AmountResult> amount = mod.chestDB.getItemCounts(Helper.instance.getWorldID(), queryString);

		if (amount.entrySet().size() == 0) {
			printNoData(player);
			return;
		}

		printAmounts(player, amount);
	}

	private void printNoData(EntityPlayerSP player) {
		player.sendMessage(new TextComponentString("No data available"));
	}

	private void printAmounts(EntityPlayerSP player, Map<String, AmountResult> amountResultMap) {
		for (Map.Entry<String, AmountResult> entry : amountResultMap.entrySet()) {
			int amount = entry.getValue().amount;
			float numberOfStacks = amount / 64.0f;
			String msg = entry.getKey() + ": " + amount + " -> " + numberOfStacks + " (" + String.join(", ",
					entry.getValue().labels) + ")";
			player.sendMessage(new TextComponentString(msg));
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
