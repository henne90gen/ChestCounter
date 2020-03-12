package de.henne90gen.chestcounter.commands;

import com.mojang.brigadier.CommandDispatcher;
import de.henne90gen.chestcounter.ChestCounter;
import de.henne90gen.chestcounter.Helper;
import de.henne90gen.chestcounter.MessagePrinter;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class ChestClearCommand {

    public static void register(ChestCounter mod, CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("chestclear")
                        .executes(context -> {
                            MessagePrinter printer = new MessagePrinter(context.getSource());
                            printer.print("Cleared chest database for this world.");
//                            mod.chestService.deleteWorld(Helper.instance.getWorldID());
                            return 0;
                        }));
    }

// TODO clean this up
//    @Override
//    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
//        return true;
//    }
//    @Override
//    public List<String> getAliases() {
//        return new ArrayList<>(Collections.singletonList("cc"));
//    }
}
