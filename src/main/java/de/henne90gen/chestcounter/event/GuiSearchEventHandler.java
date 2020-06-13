package de.henne90gen.chestcounter.event;

import de.henne90gen.chestcounter.ChestCounter;
import de.henne90gen.chestcounter.Renderer;
import de.henne90gen.chestcounter.db.entities.ChestConfig;
import de.henne90gen.chestcounter.db.entities.SearchResultPlacement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ToggleWidget;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class GuiSearchEventHandler extends GuiEventHandler {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final int SEARCH_FIELD_WIDTH = 100;

    private final ChestCounter mod;

    private TextFieldWidget searchField = null;
    private ToggleWidget searchToggle = null;

    public GuiSearchEventHandler(ChestCounter mod) {
        this.mod = mod;
    }

    @SubscribeEvent
    public void initGui(GuiScreenEvent.InitGuiEvent.Post event) {
        ChestConfig config = mod.chestService.getConfig();
        if (!(event.getGui() instanceof ContainerScreen)
                || event.getGui() instanceof CreativeScreen) {
            return;
        }
        LOGGER.debug("Screen opened: " + event.getGui().getClass());

        addUiComponents(event, config);

        mod.search();
    }

    @SubscribeEvent
    public void keyPressed(GuiScreenEvent.KeyboardKeyPressedEvent.Pre event) {
        ChestConfig config = mod.chestService.getConfig();
        if (shouldNotHandleGuiEvent(event, config)) {
            return;
        }

        if (!config.showSearchResultInInventory) {
            return;
        }

        keyPressedOnTextField(event, searchField);
        if (event.isCanceled()) {
            mod.search(searchField.getText());
        }
    }

    @SubscribeEvent
    public void charTyped(GuiScreenEvent.KeyboardCharTypedEvent.Pre event) {
        ChestConfig config = mod.chestService.getConfig();
        if (shouldNotHandleGuiEvent(event, config)) {
            return;
        }

        if (!config.showSearchResultInInventory) {
            return;
        }

        if (searchField != null) {
            event.setCanceled(searchField.charTyped(event.getCodePoint(), event.getModifiers()));
            mod.search(searchField.getText());
        }
    }

    @SubscribeEvent
    public void mouseClicked(GuiScreenEvent.MouseClickedEvent.Pre event) {
        ChestConfig config = mod.chestService.getConfig();
        if (shouldNotHandleGuiEvent(event, config)) {
            return;
        }

        if (!config.showSearchResultInInventory) {
            return;
        }

        if (searchField != null) {
            searchField.mouseClicked(event.getMouseX(), event.getMouseY(), event.getButton());
        }
        if (searchToggle != null) {
            searchToggle.mouseClicked(event.getMouseX(), event.getMouseY(), event.getButton());
        }
    }

    @SubscribeEvent
    public void mouseReleased(GuiScreenEvent.MouseReleasedEvent.Pre event) {
        ChestConfig config = mod.chestService.getConfig();
        if (shouldNotHandleGuiEvent(event, config)) {
            return;
        }

        if (config.showSearchResultInInventory) {
            if (searchField != null) {
                searchField.mouseReleased(event.getMouseX(), event.getMouseY(), event.getButton());
            }
            if (searchToggle != null) {
                if (searchToggle.mouseReleased(event.getMouseX(), event.getMouseY(), event.getButton()) &&
                        searchToggle.isMouseOver(event.getMouseX(), event.getMouseY())) {
                    boolean triggered = !searchToggle.isStateTriggered();
                    searchToggle.setStateTriggered(triggered);
                }
            }
        }

        // this only works for picking up items, not for dropping them back into the inventory
        // TODO investigate GuiScreenEvent.ActionPerformedEvent, maybe that works better for getting the current chest content
        saveCurrentChest(mod, event);
        if (searchField != null) {
            mod.search(searchField.getText());
        }
    }

    @SubscribeEvent
    public void renderGui(GuiScreenEvent.DrawScreenEvent.Pre event) {
        ChestConfig config = mod.chestService.getConfig();
        boolean visible = config.enabled && config.showSearchResultInInventory;
        if (searchField != null) {
            searchField.visible = visible;
        }
        if (searchToggle != null) {
            searchToggle.visible = visible;
        }
    }

    @SubscribeEvent
    public void renderSearchResult(GuiScreenEvent.DrawScreenEvent.Post event) {
        ChestConfig config = mod.chestService.getConfig();
        if (shouldNotHandleGuiEvent(event, config)) {
            return;
        }

        if (!config.showSearchResultInInventory) {
            return;
        }

        if (mod.lastSearchResult == null) {
            return;
        }

        boolean byId = false;
        if (searchToggle != null) {
            byId = !searchToggle.isStateTriggered();
        }
        ContainerScreen<?> screen = (ContainerScreen<?>) event.getGui();
        boolean placeToTheRightOfInventory = config.searchResultPlacement == SearchResultPlacement.RIGHT_OF_INVENTORY;
        Renderer.renderSearchResult(screen, mod.lastSearchResult, byId, placeToTheRightOfInventory);
    }

    private void addUiComponents(GuiScreenEvent.InitGuiEvent.Post event, ChestConfig config) {
        ContainerScreen<?> screen = (ContainerScreen<?>) event.getGui();
        boolean placeToTheRightOfInventory = config.searchResultPlacement == SearchResultPlacement.RIGHT_OF_INVENTORY;
        boolean visible = config.showSearchResultInInventory;
        addSearchFieldToScreen(event, screen, placeToTheRightOfInventory, visible);
        addSearchToggleToScreen(event, screen, placeToTheRightOfInventory, visible);
    }

    private void addSearchToggleToScreen(GuiScreenEvent.InitGuiEvent.Post event, ContainerScreen<?> screen, boolean placeToTheRightOfInventory, boolean visible) {
        int x;
        if (placeToTheRightOfInventory) {
            int guiLeft = screen.getGuiLeft();
            int xSize = screen.getXSize();
            x = guiLeft + xSize + Renderer.MARGIN;
        } else {
            x = 2 * Renderer.MARGIN + SEARCH_FIELD_WIDTH + 20;
        }

        searchToggle = new ToggleWidget(
                x - 20, 2,
                12, 15,
                false
        );
        searchToggle.visible = visible;
        final ResourceLocation RECIPE_BOOK = new ResourceLocation("textures/gui/recipe_book.png");
        searchToggle.initTextureValues(152, 41, 28, 18, RECIPE_BOOK);
        searchToggle.setStateTriggered(true);
        event.addWidget(searchToggle);
    }

    private void addSearchFieldToScreen(GuiScreenEvent.InitGuiEvent.Post event, ContainerScreen<?> screen, boolean placeToTheRightOfInventory, boolean visible) {
        int x;
        if (placeToTheRightOfInventory) {
            int guiLeft = screen.getGuiLeft();
            int xSize = screen.getXSize();
            x = guiLeft + xSize + Renderer.MARGIN;
        } else {
            x = Renderer.MARGIN;
        }

        searchField = new TextFieldWidget(
                Minecraft.getInstance().fontRenderer,
                x, 2,
                SEARCH_FIELD_WIDTH, 10,
                "Search"
        );
        searchField.visible = visible;
        if (mod.lastSearchResult != null) {
            searchField.setText(mod.lastSearchResult.search);
        }
        event.addWidget(searchField);
    }
}
