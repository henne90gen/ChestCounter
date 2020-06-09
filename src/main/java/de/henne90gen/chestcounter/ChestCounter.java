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
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ChestCounter.MOD_ID)
public class ChestCounter {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "chestcounter";
    public static final String MOD_NAME = "Chest Counter";

    public static final String KEYBIND_CATEGORY = "key." + ChestCounter.MOD_ID + ".category";

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

        LOGGER.info("Enabled {}", MOD_NAME);
    }

    public void setup(final FMLCommonSetupEvent event) {
        ChestDB chestDB = new CacheChestDB(new FileChestDB(getChestDBFilename()));
        chestService = new ChestService(chestDB);

        MinecraftForge.EVENT_BUS.register(new ChestEventHandler(this));
        MinecraftForge.EVENT_BUS.register(new GuiSearchEventHandler(this));
        MinecraftForge.EVENT_BUS.register(new GuiLabelEventHandler(this));

        registerKeybindings();

        LOGGER.info("Setup {}", MOD_NAME);
    }

    private void registerKeybindings() {
        {
            // TODO change to F8
            int keyCode = 67; // C
            String description = "toggle_mod_enabled";
            toggleModEnabled = new KeyBinding(description, KeyConflictContext.UNIVERSAL, KeyModifier.CONTROL, InputMappings.Type.KEYSYM, keyCode, KEYBIND_CATEGORY);
            ClientRegistry.registerKeyBinding(toggleModEnabled);
        }
        {
            // TODO change to CTRL+F8
            int keyCode = 83; // S
            String description = "show_search_result_in_inventory";
            showSearchResultInInventory = new KeyBinding(description, KeyConflictContext.GUI, KeyModifier.CONTROL, InputMappings.Type.KEYSYM, keyCode, KEYBIND_CATEGORY);
            ClientRegistry.registerKeyBinding(showSearchResultInInventory);
        }
    }

    public String getChestDBFilename() {
        return "./chestcount.json";
    }

    public void search() {
        search(null);
    }

    public void search(String query) {
        if (query == null) {
            // use last search result
            query = lastSearchResult.search;
        }
        lastSearchResult = chestService.getItemCounts(Helper.getWorldID(), query);
    }
}
