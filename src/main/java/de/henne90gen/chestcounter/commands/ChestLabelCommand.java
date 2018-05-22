package de.henne90gen.chestcounter.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.henne90gen.chestcounter.ChestCounter;
import de.henne90gen.chestcounter.ChestDB;
import de.henne90gen.chestcounter.dtos.Chest;
import javax.annotation.Nullable;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
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
	private final ChestDB chestDB;

	public ChestLabelCommand(ChestCounter mod) {
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

		float blockReachDistance = 5;
		Vec3d vec3d = sender.getCommandSenderEntity().getPositionEyes(1);
		Vec3d vec3d1 = sender.getCommandSenderEntity().getLook(1);
		Vec3d vec3d2 = vec3d.addVector(vec3d1.x * blockReachDistance,
				vec3d1.y * blockReachDistance,
				vec3d1.z * blockReachDistance);
		RayTraceResult rayTraceResult = sender.getCommandSenderEntity().world.rayTraceBlocks(vec3d,
				vec3d2,
				true,
				true,
				true);
		TileEntity tileEntity = sender.getCommandSenderEntity().world.getTileEntity(rayTraceResult.getBlockPos());
		if (tileEntity instanceof TileEntityChest) {
			String label = args[0];
			List<BlockPos> chestPositions = mod.getChestPositions(sender.getCommandSenderEntity().world,
					rayTraceResult.getBlockPos());

			Chest chest = new Chest();
			chest.id = chestDB.createChestID(chestPositions);
			chest.worldID = mod.getWorldID();
			chest.chestContent.label = label;

			chestDB.updateLabel(chest);
			player.sendMessage(new TextComponentString("Updated label to " + label));
		} else {
			player.sendMessage(new TextComponentString("Please look at a chest to update its label"));
		}
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
