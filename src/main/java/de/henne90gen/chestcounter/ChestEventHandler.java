package de.henne90gen.chestcounter;

import de.henne90gen.chestcounter.service.dtos.Chest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;

import java.util.*;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ChestEventHandler {

	private static final int INVENTORY_SIZE = 36;

	private final ChestCounter mod;
	private List<BlockPos> chestPositions;
	private Chest chest;

	public ChestEventHandler(ChestCounter mod) {
		this.mod = mod;
		this.chestPositions = new ArrayList<>();
	}

	@SubscribeEvent
	public void worldLoaded(WorldEvent.Load event) {
		// FIXME commands do not work. Remove this as soon as we don't need commands any more
		IntegratedServer integratedServer = Minecraft.getInstance().getIntegratedServer();
		mod.registerCommands(integratedServer);
	}

	@SubscribeEvent
	public void close(GuiOpenEvent event) {
		if (event.getGui() == null && chest != null) {
			Helper.instance.runInThread(() -> {
				mod.chestService.save(chest);
				chest = null;
				chestPositions.clear();
			});
		}
	}

	@SubscribeEvent
	public void guiIsOpen(GuiContainerEvent event) {
		if (shouldNotHandleEvent(event)) {
			return;
		}

		Minecraft mc = event.getGuiContainer().getMinecraft();
		if (mc.currentScreen instanceof ChestScreen) {
			Container currentContainer = ((ChestScreen) mc.currentScreen).getContainer();

			chest = new Chest();
			chest.worldID = Helper.instance.getWorldID();
			chest.id = Helper.instance.getChestId(chestPositions);
			chest.items = countItems(currentContainer);
			mod.chestService.save(chest);
		}
	}

	private boolean shouldNotHandleEvent(GuiContainerEvent event) {
		return chestPositions.isEmpty() || event.getGuiContainer() == null ||
				event.getGuiContainer().getMinecraft().world == null ||
				!event.getGuiContainer().getMinecraft().world.isRemote();
	}

	private Map<String, Integer> countItems(Container currentContainer) {
		Map<String, Integer> counter = new LinkedHashMap<>();
		for (int i = 0; i < currentContainer.inventorySlots.size() - INVENTORY_SIZE; i++) {
			ItemStack stack = currentContainer.inventorySlots.get(i).getStack();
			String itemName = stack.getDisplayName().getString();
			if ("Air".equals(itemName)) {
				continue;
			}
			Integer currentCount = counter.get(itemName);
			if (currentCount == null) {
				currentCount = 0;
			}
			currentCount += stack.getCount();
			counter.put(itemName, currentCount);
		}
		return counter;
	}

	@SubscribeEvent
	public void interact(PlayerInteractEvent event) {
		if (!event.getWorld().isRemote()) {
			return;
		}
		chestPositions = Helper.instance.getChestPositions(event.getWorld(), event.getPos());
	}

	@SubscribeEvent
	public void harvestBlock(BlockEvent.BreakEvent event) {
		if (!event.getWorld().isRemote()) {
			return;
		}

		TileEntity tileEntity = event.getWorld().getTileEntity(event.getPos());
		if (tileEntity instanceof ChestTileEntity) {
			String worldId = Helper.instance.getWorldID();
			String chestId = Helper.instance.getChestId(Collections.singletonList(event.getPos()));

			mod.chestService.delete(worldId, chestId);

			BlockPos[] positions = {
					event.getPos().north(),
					event.getPos().east(),
					event.getPos().south(),
					event.getPos().west()
			};
			for (BlockPos position : positions) {
				TileEntity entity = event.getWorld().getTileEntity(position);
				if (entity instanceof ChestTileEntity) {
					Chest chest = new Chest();
					chest.worldID = Helper.instance.getWorldID();
					chest.id = Helper.instance.getChestId(Collections.singletonList(position));
					Helper.instance.runInThread(() -> mod.chestService.save(chest));
					break;
				}
			}
		}
	}

	@SubscribeEvent
	public void place(BlockEvent.EntityPlaceEvent event) {
		if (!event.getWorld().isRemote()) {
			return;
		}

		TileEntity tileEntity = event.getWorld().getTileEntity(event.getPos());
		if (tileEntity instanceof ChestTileEntity) {
			Chest chest = new Chest();
			chest.worldID = Helper.instance.getWorldID();
			List<BlockPos> chestPositions = Helper.instance.getChestPositions(event.getWorld(), event.getPos());
			chest.id = Helper.instance.getChestId(chestPositions);
			Helper.instance.runInThread(() -> mod.chestService.save(chest));
		}
	}

	@SubscribeEvent
	public void render(RenderWorldLastEvent event) {
		List<Chest> chests = mod.chestService.getChests(Helper.instance.getWorldID());

		if (chests == null) {
			return;
		}

		int color = 0xFFFFFF;
		float partialTickTime = event.getPartialTicks();
		float maxDistance = 10.0F;

		for (Chest chest : chests) {
			String[] text = {chest.label};
			BlockPos pos = Helper.instance.getBlockPosFromChestID(chest.id);
			renderText(text, pos.getX(), pos.getY(), pos.getZ(), maxDistance, color, partialTickTime);
		}
	}

	private void renderText(String[] text, float x, float y, float z, float maxDistance, int color, float partialTickTime) {
		// TODO refactor this into a different class
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) {
			return;
		}

		float playerX = (float) (mc.player.lastTickPosX + (mc.player.getPosX() - mc.player.lastTickPosX) * partialTickTime);
		float playerY = (float) (mc.player.lastTickPosY + (mc.player.getPosY() - mc.player.lastTickPosY) * partialTickTime);
		float playerZ = (float) (mc.player.lastTickPosZ + (mc.player.getPosZ() - mc.player.lastTickPosZ) * partialTickTime);

		float dx = (x - playerX) + 0.5F;
		float dy = (y - playerY) + 0.5F;
		float dz = (z - playerZ) + 0.5F;
		float distance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (distance >= maxDistance) {
			return;
		}

		float scale = 0.03f;
		GL11.glColor4f(1f, 1f, 1f, 0.5f);
		GL11.glPushMatrix();
		GL11.glTranslatef(dx, dy, dz);
		GL11.glRotatef(-mc.player.cameraYaw, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(mc.player.cameraYaw, 1.0F, 0.0F, 0.0F);
		GL11.glScalef(-scale, -scale, scale);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDepthMask(false);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		int textWidth = 0;
		for (String thisMessage : text) {
			int thisMessageWidth = mc.fontRenderer.getStringWidth(thisMessage);

			if (thisMessageWidth > textWidth)
				textWidth = thisMessageWidth;
		}

		int lineHeight = 10;
		int i = 0;
		for (String message : text) {
			mc.fontRenderer.drawString(message, -textWidth / 2, i * lineHeight, color);
			i++;
		}

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glPopMatrix();
	}
}
