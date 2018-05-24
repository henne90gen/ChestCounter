package de.henne90gen.chestcounter.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.henne90gen.chestcounter.MessagePrinter;
import javax.annotation.Nullable;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class ChestToggleCommand implements ICommand {

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		MessagePrinter.togglePrintToEveryone();
		new MessagePrinter(sender).print("Toggled message sending");
	}

	@Override
	public String getName() {
		return "chesttoggle";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "Usage: /chesttoggle";
	}

	@Override
	public List<String> getAliases() {
		return new ArrayList<>(Collections.singletonList("ct"));
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
