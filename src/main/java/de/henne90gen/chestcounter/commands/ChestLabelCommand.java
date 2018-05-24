package de.henne90gen.chestcounter.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import de.henne90gen.chestcounter.ChestCounter;
import de.henne90gen.chestcounter.Helper;
import de.henne90gen.chestcounter.MessagePrinter;
import de.henne90gen.chestcounter.dtos.Chest;
import javax.annotation.Nullable;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public class ChestLabelCommand implements ICommand {

	private final ChestCounter mod;

	public ChestLabelCommand(ChestCounter mod) {
		this.mod = mod;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		MessagePrinter printer = new MessagePrinter(sender);
		Entity commandSenderEntity = sender.getCommandSenderEntity();

		if (args.length > 1 || commandSenderEntity == null) {
			printer.print(getUsage(sender));
			return;
		} else if (args.length == 0) {
			Map<String, List<String>> labels = mod.chestService.getAllLabels(Helper.instance.getWorldID());
			printer.printLabels(labels);
			return;
		}

		BlockPos blockPos = findBlockPosLookedAt(commandSenderEntity);
		if (blockPos == null) {
			printer.printLookAtChestMessage();
			return;
		}

		TileEntity tileEntity = commandSenderEntity.world.getTileEntity(blockPos);
		if (tileEntity instanceof TileEntityChest) {
			String label = args[0];
			List<BlockPos> chestPositions = Helper.instance.getChestPositions(commandSenderEntity.world,
					blockPos);

			Chest chest = new Chest();
			chest.id = Helper.instance.createChestID(chestPositions);
			chest.worldID = Helper.instance.getWorldID();
			chest.chestContent.label = label;

			Helper.instance.runInThread(() -> mod.chestService.updateLabel(chest));
			printer.printUpdatedLabel(label);
		} else {
			printer.printLookAtChestMessage();
		}
	}

	private BlockPos findBlockPosLookedAt(Entity entity) {
		float blockReachDistance = 5;
		Vec3d vec3d = entity.getPositionEyes(1);
		Vec3d vec3d1 = entity.getLook(1);
		Vec3d vec3d2 = vec3d.addVector(vec3d1.x * blockReachDistance,
				vec3d1.y * blockReachDistance,
				vec3d1.z * blockReachDistance);

		RayTraceResult result = entity.world.rayTraceBlocks(vec3d,
				vec3d2,
				true,
				true,
				true);

		if (result == null) {
			return null;
		}
		return result.getBlockPos();
	}

	@Override
	public String getName() {
		return "chestlabel";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "Usage: /chestlabel [label]";
	}

	@Override
	public List<String> getAliases() {
		String[] aliases = { "cl", "chestl" };
		return new ArrayList<>(Arrays.asList(aliases));
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
