package de.henne90gen.chestcounter.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.henne90gen.chestcounter.ChestCounter;
import de.henne90gen.chestcounter.Helper;
import de.henne90gen.chestcounter.MessagePrinter;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

public class ChestLabelCommand {

	private static final Logger LOGGER = LogManager.getLogger();

	public static void register(ChestCounter mod, CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
				Commands.literal("chestlabel")
						.then(Commands.argument("label", StringArgumentType.string()))
						.executes(context -> {
							MessagePrinter printer = new MessagePrinter(context.getSource());

							Entity entity = context.getSource().getEntity();
							if (entity == null) {
								LOGGER.warn("Source entity of command was null.");
								return 1;
							}

							String label = StringArgumentType.getString(context, "label");
							if (label.isEmpty()) {
								Map<String, List<String>> labels = mod.chestService.getAllLabels(Helper.instance.getWorldID());
								printer.printLabels(labels);
								return 0;
							}

							BlockPos blockPos = findBlockPosLookedAt(entity);
							if (blockPos == null) {
								printer.printLookAtChestMessage();
								return 1;
							}

							TileEntity tileEntity = entity.world.getTileEntity(blockPos);
							if (tileEntity instanceof ChestTileEntity) {
								List<BlockPos> chestPositions = Helper.instance.getChestPositions(entity.world,
										blockPos);

								String worldId = Helper.instance.getWorldID();
								String chestId = Helper.instance.getChestId(chestPositions);

								Helper.instance.runInThread(() -> mod.chestService.updateLabel(worldId, chestId, label));
								printer.printUpdatedLabel(label);
							} else {
								printer.printLookAtChestMessage();
							}
							return 0;
						}));
	}

	private static BlockPos findBlockPosLookedAt(Entity entity) {
		float blockReachDistance = 5;
		Vec3d vec3d = entity.getEyePosition(1);
		Vec3d vec3d1 = entity.getLook(1);
		Vec3d vec3d2 = vec3d.add(vec3d1.x * blockReachDistance,
				vec3d1.y * blockReachDistance,
				vec3d1.z * blockReachDistance);

		// TODO find out what the last 3 arguments do
		BlockRayTraceResult result = entity.world.rayTraceBlocks(vec3d, vec3d2, null, null, null);

		if (result == null) {
			return null;
		}
		return result.getPos();
	}

// TODO clean this up
//    @Override
//    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
//        return true;
//    }
}
