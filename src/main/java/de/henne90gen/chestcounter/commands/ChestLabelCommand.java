package de.henne90gen.chestcounter.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import de.henne90gen.chestcounter.ChestCounter;
import de.henne90gen.chestcounter.Helper;
import de.henne90gen.chestcounter.dtos.Chest;
import javax.annotation.Nullable;
import net.minecraft.client.entity.EntityPlayerSP;
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
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.client.FMLClientHandler;

public class ChestLabelCommand implements ICommand {

	private final ChestCounter mod;

	public ChestLabelCommand(ChestCounter mod) {
		this.mod = mod;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		EntityPlayerSP player = FMLClientHandler.instance().getClient().player;
		Entity commandSenderEntity = sender.getCommandSenderEntity();

		if (args.length > 1 || commandSenderEntity == null) {
			player.sendMessage(new TextComponentString(getUsage(sender)));
			return;
		} else if (args.length == 0) {
			printLabels(player);
			return;
		}

		BlockPos blockPos = findBlockPosLookedAt(commandSenderEntity);
		if (blockPos == null) {
			printLookAtChestMessage(player);
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

			Helper.instance.runInThread(() -> mod.chestDB.updateLabel(chest));
			player.sendMessage(new TextComponentString("Updated label to " + label));
		} else {
			printLookAtChestMessage(player);
		}
	}

	private void printLabels(EntityPlayerSP player) {
		Map<String, List<String>> labels = mod.chestDB.getAllLabels(Helper.instance.getWorldID());
		for (Map.Entry<String, List<String>> entry : labels.entrySet()) {
			String label = entry.getKey();
			if (label.isEmpty()) {
				label = "No Label";
			}

			StringBuilder chestIDs = new StringBuilder();
			for (String chestID : entry.getValue()) {
				chestIDs.append("(").append(chestID).append("), ");
			}
			String chestIDsString = chestIDs.substring(0, chestIDs.length() - 2);

			player.sendMessage(new TextComponentString(label + ": " + chestIDsString));
		}
	}

	private void printLookAtChestMessage(EntityPlayerSP player) {
		player.sendMessage(new TextComponentString("Please look at a chest to update its label"));
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
