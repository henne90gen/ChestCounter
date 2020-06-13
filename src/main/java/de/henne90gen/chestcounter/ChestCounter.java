package de.henne90gen.chestcounter;

import de.henne90gen.chestcounter.db.CacheChestDB;
import de.henne90gen.chestcounter.db.ChestDB;
import de.henne90gen.chestcounter.db.FileChestDB;
import de.henne90gen.chestcounter.event.ChestEventHandler;
import de.henne90gen.chestcounter.event.GuiLabelEventHandler;
import de.henne90gen.chestcounter.event.GuiSearchEventHandler;
import de.henne90gen.chestcounter.service.ChestService;
import de.henne90gen.chestcounter.service.dtos.Chest;
import de.henne90gen.chestcounter.service.dtos.ChestSearchResult;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ChestCounter.MOD_ID)
public class ChestCounter {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "chestcounter";
    public static final String MOD_NAME = "Chest Counter";

    public ChestService chestService;

    public Chest currentChest = null;
    public ChestSearchResult lastSearchResult = new ChestSearchResult();

    public KeyBinding toggleModEnabled;
    public KeyBinding showSearchResultInInventory;

    public ChestCounter() {
        FMLJavaModLoadingContext fmlJavaModLoadingContext = FMLJavaModLoadingContext.get();
        if (fmlJavaModLoadingContext == null) {
            LOGGER.warn("Could not get mod loading context.");
            return;
        }

        IEventBus modEventBus = fmlJavaModLoadingContext.getModEventBus();
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::clientSetup);

        LOGGER.info("Enabled {}", MOD_NAME);
    }

    public void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("[{}] Common Setup...", MOD_NAME);

        ChestDB chestDB = new CacheChestDB(new FileChestDB(getChestDBFilename()));
        chestService = new ChestService(chestDB);

        MinecraftForge.EVENT_BUS.register(new ChestEventHandler(this));
        MinecraftForge.EVENT_BUS.register(new GuiSearchEventHandler(this));
        MinecraftForge.EVENT_BUS.register(new GuiLabelEventHandler(this));

        LOGGER.info("[{}] Common Setup Done.", MOD_NAME);
    }

    public void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("[{}] Client Setup...", MOD_NAME);

        registerKeybindings();

        LOGGER.info("[{}] Client Setup Done.", MOD_NAME);
    }

    private void registerKeybindings() {
        int keyCode = 297; // F8

        String toggleDescription = "Toggle mod enabled";
        toggleModEnabled = new KeyBinding(toggleDescription, KeyConflictContext.UNIVERSAL, KeyModifier.NONE, InputMappings.Type.KEYSYM, keyCode, MOD_NAME);
        ClientRegistry.registerKeyBinding(toggleModEnabled);

        String searchResultDescription = "Show search results in inventory";
        showSearchResultInInventory = new KeyBinding(searchResultDescription, KeyConflictContext.UNIVERSAL, KeyModifier.CONTROL, InputMappings.Type.KEYSYM, keyCode, MOD_NAME);
        ClientRegistry.registerKeyBinding(showSearchResultInInventory);
    }

    public String getChestDBFilename() {
        return "./chestcount.json";
    }

    public void search() {
        search(null);
    }

    public void search(String query) {
        if (query == null) {
            // use last search query
            query = lastSearchResult.search;
        }
        lastSearchResult = chestService.getItemCounts(Helper.getWorldID(), query);
    }
}
