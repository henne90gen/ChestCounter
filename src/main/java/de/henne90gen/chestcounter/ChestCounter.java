package de.henne90gen.chestcounter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.server.FMLServerHandler;
import org.apache.logging.log4j.Logger;

@Mod(modid = ChestCounter.MODID, name = ChestCounter.NAME, version = ChestCounter.VERSION, useMetadata = true)
public class ChestCounter {

	public static final String MODID = "chestcounter";
	public static final String NAME = "ChestContent Counter";
	public static final String VERSION = "1.0";

	public static Logger logger;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
		MinecraftForge.EVENT_BUS.register(new ChestEventHandler());
		logger.info("Enabled");
		ClientCommandHandler.instance.registerCommand(new ChestCommand());
	}

	public static String getWorldID() {
		int dimension = FMLClientHandler.instance().getClient().player.dimension;

		String worldName;
		ServerData currentServerData = Minecraft.getMinecraft().getCurrentServerData();
		if (currentServerData != null) {
			worldName = currentServerData.serverIP;
		} else {
			// FIXME find a better world name
			worldName = FMLClientHandler.instance().getWorldClient().getWorldInfo().getWorldName();
		}

		logger.info("WorldName: " + worldName);

		return worldName + ":" + dimension;
	}

	public static void logError(Exception e) {
		logger.error("Something went wrong!", e);
	}
}
