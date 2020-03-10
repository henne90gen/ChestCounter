package de.henne90gen.chestcounter.commands;

import com.mojang.brigadier.CommandDispatcher;
import de.henne90gen.chestcounter.ChestCounter;
import de.henne90gen.chestcounter.MessagePrinter;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class ChestToggleCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("chesttoggle")
                        .executes(context -> {
                            MessagePrinter.togglePrintToEveryone();
                            new MessagePrinter(context.getSource()).printToggle();
                            return 0;
                        }));
    }


// TODO clean this up
//    @Override
//    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
//        return true;
//    }
}
