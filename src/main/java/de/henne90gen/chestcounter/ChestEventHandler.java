package de.henne90gen.chestcounter;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.henne90gen.chestcounter.service.dtos.Chest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ChestEventHandler {

	private static final Logger LOGGER = LogManager.getLogger();

	private final ChestCounter mod;
	private Chest chest;

	public ChestEventHandler(ChestCounter mod) {
		this.mod = mod;
	}

	@SubscribeEvent
	public void worldLoaded(WorldEvent.Load event) {
		// FIXME commands do not work. Remove this as soon as we don't need commands any more
//		IntegratedServer integratedServer = Minecraft.getInstance().getIntegratedServer();
//		mod.registerCommands(integratedServer);
	}

	@SubscribeEvent
	public void close(GuiOpenEvent event) {
		if (event.getGui() == null && chest != null) {
			Helper.instance.runInThread(() -> {
				mod.chestService.save(chest);
				chest = null;
			});
		}
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
			chest.id = Helper.instance.getChestId(event.getWorld(), event.getPos());
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
		MatrixStack matrixStackIn = event.getMatrixStack();

		for (Chest chest : chests) {
			String text = chest.label;
			if (text == null) {
				continue;
			}
			BlockPos pos = Helper.instance.getBlockPosFromChestID(chest.id);
			renderText(text, pos.getX(), pos.getY(), pos.getZ(), maxDistance, color, matrixStackIn, partialTickTime);
		}
	}

	private void renderText(String text, float x, float y, float z, float maxDistance, int color, MatrixStack matrixStackIn, float partialTickTime) {
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

		matrixStackIn.push();

		matrixStackIn.translate(dx, dy - 0.9F, dz);
		matrixStackIn.rotate(mc.getRenderManager().getCameraOrientation());
		float scale = 0.025F;
		matrixStackIn.scale(-scale, -scale, scale);
		Matrix4f matrix4f = matrixStackIn.getLast().getMatrix();

		float textBackgroundOpacity = mc.gameSettings.getTextBackgroundOpacity(0.25F);
		int colorBackground = (int) (textBackgroundOpacity * 255.0F) << 24;
		FontRenderer fontrenderer = mc.getRenderManager().getFontRenderer();
		float textX = (float) (-fontrenderer.getStringWidth(text) / 2);
		float textY = 0.0F;
		IRenderTypeBuffer bufferIn = mc.getRenderTypeBuffers().getBufferSource();
		int packedLightIn = mc.getRenderManager().getPackedLight(mc.player, partialTickTime);
		fontrenderer.renderString(text, textX, textY, -1, false, matrix4f, bufferIn, false, colorBackground, packedLightIn);

		matrixStackIn.pop();
	}
}
