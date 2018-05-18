package de.henne90gen.chestcounter;

import de.henne90gen.chestcounter.commands.ChestCommand;
import de.henne90gen.chestcounter.commands.ChestLabelCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = ChestCounter.MODID, name = ChestCounter.NAME, version = ChestCounter.VERSION, useMetadata = true)
public class ChestCounter {

	public static final String MODID = "chestcounter";
	public static final String NAME = "Chest Counter";
	public static final String VERSION = "1.0";

	public static Logger logger;

	public String label;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();

		// register event handler
		MinecraftForge.EVENT_BUS.register(new ChestEventHandler(this));

		// register commands
		ClientCommandHandler.instance.registerCommand(new ChestCommand(this));
		ClientCommandHandler.instance.registerCommand(new ChestLabelCommand(this));

		logger.info("Enabled");
	}

	public static String getWorldID() {
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

	public static void logError(Exception e) {
		logger.error("Something went wrong!", e);
	}
}
