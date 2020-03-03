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

public class ChestClearCommand implements ICommand {

    private final ChestCounter mod;

    public ChestClearCommand(ChestCounter mod) {
        this.mod = mod;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        MessagePrinter printer = new MessagePrinter(sender);

        if (args.length != 0) {
            printer.print(getUsage(sender));
        } else {
            mod.chestService.deleteWorld(Helper.instance.getWorldID());
        }
    }

    @Override
    public String getName() {
        return "chestclear";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "Usage: /chestclear";
    }

    @Override
    public List<String> getAliases() {
        return new ArrayList<>(Collections.singletonList("cc"));
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
