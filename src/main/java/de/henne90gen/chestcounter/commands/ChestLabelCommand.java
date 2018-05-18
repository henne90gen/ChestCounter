package de.henne90gen.chestcounter.commands;

import java.util.Collections;
import java.util.List;

import de.henne90gen.chestcounter.ChestCounter;
import javax.annotation.Nullable;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class ChestLabelCommand implements ICommand {

	private final ChestCounter mod;

	public ChestLabelCommand(ChestCounter mod) {
		this.mod = mod;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length != 1) {
			return;
		}
		mod.label = args[0];
	}

	@Override
	public String getName() {
		return "chestlabel";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "Usage: /chestlabel";
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
		return Collections.singletonList("/chestlabel");
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
