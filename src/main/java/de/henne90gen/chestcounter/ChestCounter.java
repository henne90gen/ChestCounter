package de.henne90gen.chestcounter;

import com.mojang.brigadier.CommandDispatcher;
import de.henne90gen.chestcounter.commands.ChestClearCommand;
import de.henne90gen.chestcounter.commands.ChestCommand;
import de.henne90gen.chestcounter.commands.ChestLabelCommand;
import de.henne90gen.chestcounter.commands.ChestToggleCommand;
import de.henne90gen.chestcounter.db.ChestDB;
import de.henne90gen.chestcounter.db.ChestDBCache;
import de.henne90gen.chestcounter.db.IChestDB;
import de.henne90gen.chestcounter.service.ChestService;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ChestCounter.MOD_ID)
public class ChestCounter implements IChestCounter {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "chestcounter";
    public static final String NAME = "Chest Counter";
    public static final String VERSION = "@VERSION@";

    public final Object fileLock = new Object();
    public ChestService chestService;

    public ChestCounter() {
        FMLJavaModLoadingContext fmlJavaModLoadingContext = FMLJavaModLoadingContext.get();
        if (fmlJavaModLoadingContext == null) {
            LOGGER.warn("Could not get mod loading context.");
            return;
        }

        IEventBus modEventBus = fmlJavaModLoadingContext.getModEventBus();
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::setupClient);
        modEventBus.addListener(this::setupServer);
        modEventBus.addListener(this::setupWorld);

        LOGGER.info("Added listeners to event bus.");
    }

    public void registerCommands(IntegratedServer integratedServer) {
        if (integratedServer == null) {
            LOGGER.warn("Could not register commands.");
            return;
        }

        CommandDispatcher<CommandSource> dispatcher = integratedServer.getCommandManager().getDispatcher();
        ChestCommand.register(this, dispatcher);
        ChestLabelCommand.register(this, dispatcher);
        ChestToggleCommand.register(dispatcher);
        ChestClearCommand.register(this, dispatcher);
    }

    public void setup(final FMLCommonSetupEvent event) {
        IChestDB chestDB = new ChestDBCache(new ChestDB(this, getChestDBFilename()));
        chestService = new ChestService(this, chestDB);

        // register event handler
        MinecraftForge.EVENT_BUS.register(new ChestEventHandler(this));

        LOGGER.info("Enabled {}", NAME);
    }

    public void setupServer(final FMLServerAboutToStartEvent event) {
        LOGGER.info("Setup server side of {}", NAME);
    }

    public void setupClient(final FMLClientSetupEvent event) {
        IntegratedServer integratedServer = event.getMinecraftSupplier().get().getIntegratedServer();
        registerCommands(integratedServer);

        LOGGER.info("Setup client side of {}", NAME);
    }

    public void setupWorld(final FMLServerStartedEvent event) {
        IntegratedServer integratedServer = Minecraft.getInstance().getIntegratedServer();
        registerCommands(integratedServer);

        LOGGER.info("Setup after world load of {}", NAME);
    }

    @Override
    public void logError(Exception e) {
        LOGGER.error("Something went wrong!", e);
    }

    @Override
    public void log(String msg) {
        LOGGER.info(msg);
    }

    @Override
    public String getChestDBFilename() {
        return "./chestcount.json";
    }
}
