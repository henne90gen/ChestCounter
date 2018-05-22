package de.henne90gen.chestcounter;

import java.util.ArrayList;
import java.util.List;

import de.henne90gen.chestcounter.commands.ChestCommand;
import de.henne90gen.chestcounter.commands.ChestLabelCommand;
import de.henne90gen.chestcounter.commands.ChestQueryCommand;
import de.henne90gen.chestcounter.eventhandlers.ChestEventHandler;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = ChestCounter.MODID, name = ChestCounter.NAME, version = ChestCounter.VERSION, useMetadata = true)
public class ChestCounter implements IChestCounter {

	public static final String MODID = "chestcounter";
	public static final String NAME = "Chest Counter";
	public static final String VERSION = "1.0";

	private Logger logger;

	public String label;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();

		// register event handler
		MinecraftForge.EVENT_BUS.register(new ChestEventHandler(this));

		// register commands
		ClientCommandHandler.instance.registerCommand(new ChestCommand(this));
		ClientCommandHandler.instance.registerCommand(new ChestLabelCommand(this));
		ClientCommandHandler.instance.registerCommand(new ChestQueryCommand(this));

		logger.info("Enabled {}", NAME);
	}

	@Nonnull
	@Override
	public List<BlockPos> getChestPositions(World world, BlockPos position) {
		List<BlockPos> chestPositions = new ArrayList<>();
		BlockPos[] positions = {
				position,
				position.north(),
				position.east(),
				position.south(),
				position.west()
		};
		for (BlockPos pos : positions) {
			TileEntity tileEntity = world.getTileEntity(pos);
			if (tileEntity instanceof TileEntityChest) {
				chestPositions.add(pos);
			}
		}
		return chestPositions;
	}

	@Override
	public String getWorldID() {
		int dimension = FMLClientHandler.instance().getClient().player.dimension;

		String worldName;
		ServerData currentServerData = Minecraft.getMinecraft().getCurrentServerData();
		if (currentServerData != null) {
			worldName = currentServerData.serverIP;
		} else {
			worldName = FMLClientHandler.instance().getServer().getWorldName();
		}

		return worldName + ":" + dimension;
	}

	@Override
	public void logError(Exception e) {
		logger.error("Something went wrong!", e);
	}

	@Override
	public void log(String msg) {
		logger.info(msg);
	}

	@Override
	public String getChestDBFilename() {
		return "./chestcount.json";
	}
}
