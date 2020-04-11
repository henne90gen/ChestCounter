package de.henne90gen.chestcounter;

import de.henne90gen.chestcounter.db.CacheChestDB;
import de.henne90gen.chestcounter.db.ChestDB;
import de.henne90gen.chestcounter.db.FileChestDB;
import de.henne90gen.chestcounter.event.ChestEventHandler;
import de.henne90gen.chestcounter.event.ChestGuiEventHandler;
import de.henne90gen.chestcounter.service.ChestService;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ChestCounter.MOD_ID)
public class ChestCounter {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "chestcounter";
    public static final String NAME = "Chest Counter";

    public ChestService chestService;

    public ChestCounter() {
        FMLJavaModLoadingContext fmlJavaModLoadingContext = FMLJavaModLoadingContext.get();
        if (fmlJavaModLoadingContext == null) {
            LOGGER.warn("Could not get mod loading context.");
            return;
        }

        IEventBus modEventBus = fmlJavaModLoadingContext.getModEventBus();
        modEventBus.addListener(this::setup);
        LOGGER.info("Added listeners to event bus.");
    }

    public void setup(final FMLCommonSetupEvent event) {
        ChestDB chestDB = new CacheChestDB(new FileChestDB(getChestDBFilename()));
        chestService = new ChestService(chestDB);

        // register event handler
        MinecraftForge.EVENT_BUS.register(new ChestEventHandler(this));
        MinecraftForge.EVENT_BUS.register(new ChestGuiEventHandler(this));

        LOGGER.info("Enabled {}", NAME);
    }

    public String getChestDBFilename() {
        return "./chestcount.json";
    }
}
