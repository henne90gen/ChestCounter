package de.henne90gen.chestcounter;

import de.henne90gen.chestcounter.commands.ChestCommand;
import de.henne90gen.chestcounter.commands.ChestLabelCommand;
import de.henne90gen.chestcounter.commands.ChestQueryCommand;
import de.henne90gen.chestcounter.db.ChestDB;
import de.henne90gen.chestcounter.db.ChestDBCache;
import de.henne90gen.chestcounter.service.ChestService;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
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

	public ChestService chestService;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();

		chestService = new ChestService(this, new ChestDBCache(new ChestDB(getChestDBFilename())));

		// register event handler
		MinecraftForge.EVENT_BUS.register(new ChestEventHandler(this));

		// register commands
		ClientCommandHandler.instance.registerCommand(new ChestCommand(this));
		ClientCommandHandler.instance.registerCommand(new ChestLabelCommand(this));
		ClientCommandHandler.instance.registerCommand(new ChestQueryCommand(this));

		logger.info("Enabled {}", NAME);
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
