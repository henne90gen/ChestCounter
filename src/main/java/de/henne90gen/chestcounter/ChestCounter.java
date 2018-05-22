package de.henne90gen.chestcounter;

import java.util.ArrayList;
import java.util.List;

import de.henne90gen.chestcounter.commands.ChestCommand;
import de.henne90gen.chestcounter.commands.ChestLabelCommand;
import de.henne90gen.chestcounter.eventhandlers.ChestEventHandler;
import de.henne90gen.chestcounter.eventhandlers.ChestLabelEventHandler;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
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
		MinecraftForge.EVENT_BUS.register(new ChestLabelEventHandler(this));

		// register commands
		ClientCommandHandler.instance.registerCommand(new ChestCommand(this));
		ClientCommandHandler.instance.registerCommand(new ChestLabelCommand(this));

		logger.info("Enabled {}", NAME);
	}

	@Nonnull
	@Override
	public List<BlockPos> getChestPositions(PlayerInteractEvent event) {
		List<BlockPos> chestPositions = new ArrayList<>();
		BlockPos[] positions = {
				event.getPos(),
				event.getPos().north(),
				event.getPos().east(),
				event.getPos().south(),
				event.getPos().west()
		};
		for (BlockPos position : positions) {
			TileEntity tileEntity = event.getWorld().getTileEntity(position);
			if (tileEntity instanceof TileEntityChest) {
				chestPositions.add(position);
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
