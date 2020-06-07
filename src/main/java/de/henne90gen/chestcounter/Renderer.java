package de.henne90gen.chestcounter;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import de.henne90gen.chestcounter.service.dtos.Chest;
import de.henne90gen.chestcounter.service.dtos.ChestSearchResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

public class Renderer {

	public static final float CHEST_NAME_FONT_SIZE = 0.4F;
	public static final float SEARCH_RESULT_FONT_SIZE = CHEST_NAME_FONT_SIZE / 2.0F;
	public static final int MARGIN = 5;
	public static final DecimalFormat TWO_DECIMAL_PLACES_FORMAT = new DecimalFormat("0.00");

	public static void renderChestLabels(List<Chest> chests, ChestSearchResult searchResult, float maxDistance, float partialTickTime, MatrixStack matrixStack) {
		for (Chest chest : chests) {
			String text = chest.label;
			if (text == null || text.isEmpty()) {
				text = chest.id;
			}

			List<BlockPos> positions = chest.getBlockPositions();
			boolean chestHasItemFromSearch = searchResult != null && searchResult.byId.containsKey(chest.id) && !searchResult.search.isEmpty();
			BlockPos pos = Helper.getClosestPositionToPlayer(positions, partialTickTime);

			int color = 0xFFFFFF;
			float finalMaxDistance = maxDistance;
			if (chestHasItemFromSearch) {
				color = 0x00FF00;
				finalMaxDistance = Float.MAX_VALUE;
			}
			renderTextInGame(text, pos.getX(), pos.getY(), pos.getZ(), finalMaxDistance, color, matrixStack, partialTickTime, CHEST_NAME_FONT_SIZE);

			if (!chestHasItemFromSearch) {
				continue;
			}

			color = 0xFFFFFF;
			float offsetY = CHEST_NAME_FONT_SIZE / 3.0F;
			int count = 0;
			for (Map.Entry<String, Integer> entry : searchResult.byId.get(chest.id).items.entrySet()) {
				if (count > 5) {
					break;
				}
				count++;

				String entryText = entry.getValue() + "x " + entry.getKey();
				renderTextInGame(entryText, pos.getX(), pos.getY() - offsetY, pos.getZ(), finalMaxDistance, color, matrixStack, partialTickTime, SEARCH_RESULT_FONT_SIZE);
				offsetY += SEARCH_RESULT_FONT_SIZE / 3.0F;
			}
		}
	}

	private static void renderTextInGame(String text, float x, float y, float z, float maxDistance, int color, MatrixStack matrixStackIn, float partialTickTime, float scale) {
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

		// moves text out of the chest (with the specified radius)
		float radius = 0.75F;
		dx -= (dx * radius) / distance;
		dz -= (dz * radius) / distance;

		matrixStackIn.push();
		matrixStackIn.translate(dx, dy - 0.9F, dz);
		matrixStackIn.rotate(mc.getRenderManager().getCameraOrientation());
		float finalScale = 0.025F * scale;
		matrixStackIn.scale(-finalScale, -finalScale, finalScale);
		Matrix4f matrix4f = matrixStackIn.getLast().getMatrix();

		float textBackgroundOpacity = mc.gameSettings.getTextBackgroundOpacity(0.25F);
		int colorBackground = (int) (textBackgroundOpacity * 255.0F) << 24;
		FontRenderer fontrenderer = mc.getRenderManager().getFontRenderer();
		float textX = (float) (-fontrenderer.getStringWidth(text) / 2);
		float textY = 0.0F;
		IRenderTypeBuffer bufferIn = mc.getRenderTypeBuffers().getBufferSource();
		int packedLightIn = mc.getRenderManager().getPackedLight(mc.player, partialTickTime);
		fontrenderer.renderString(text, textX, textY, color, false, matrix4f, bufferIn, false, colorBackground, packedLightIn);

		matrixStackIn.pop();
	}

	public static void renderSearchResult(ContainerScreen<?> screen, ChestSearchResult searchResult, boolean byId, boolean placeToTheRightOfInventory) {
		int baseX;
		if (placeToTheRightOfInventory) {
			int guiLeft = screen.getGuiLeft();
			int xSize = screen.getXSize();
			baseX = guiLeft + xSize + MARGIN;
		} else {
			baseX = MARGIN;
		}

		int currentY = 17;
		Map<String, ChestSearchResult.Entry> resultMap = byId ? searchResult.byId : searchResult.byLabel;
		for (Map.Entry<String, ChestSearchResult.Entry> entry : resultMap.entrySet()) {
			ChestSearchResult.Entry value = entry.getValue();
			double distanceToChest = getDistanceToClosestPosition(value.positions);
			String formattedDistance = TWO_DECIMAL_PLACES_FORMAT.format(distanceToChest);
			String label = entry.getKey() + " -> " + formattedDistance + "m";
			renderTextInMenu(label, baseX, currentY);

			currentY += MARGIN;
			for (Map.Entry<String, Integer> amountEntry : value.items.entrySet()) {
				String amountString = amountEntry.getValue() + "x " + amountEntry.getKey();
				int xOffset = 7;
				renderTextInMenu(amountString, baseX + xOffset, currentY);
				currentY += MARGIN;
			}
			currentY += 3;
		}
	}

	private static double getDistanceToClosestPosition(List<Vec3d> positions) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) {
			return 0.0;
		}

		double playerX = mc.player.getPosX();
		double playerY = mc.player.getPosY();
		double playerZ = mc.player.getPosZ();
		Vec3d playerPos = new Vec3d(playerX, playerY, playerZ);

		double closestDistance = Double.MAX_VALUE;
		for (Vec3d pos : positions) {
			double distance = pos.distanceTo(playerPos);
			if (distance < closestDistance) {
				closestDistance = distance;
			}
		}

		return closestDistance;
	}

	private static void renderTextInMenu(String text, float x, float y) {
		RenderSystem.enableAlphaTest();
		float scale = 0.5F;

		// constant taken from FontRenderer
		int packedLight = 15728880;
		float scaleInv = 1 / scale;
		IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
		Matrix4f matrix = TransformationMatrix.identity().getMatrix();
		matrix.mul(Matrix4f.makeScale(scale, scale, 1.0F));

		Minecraft.getInstance().fontRenderer.renderString(
				text,
				x * scaleInv, y * scaleInv,
				0xffffff, false,
				matrix,
				buffer,
				true,
				0xffffff,
				packedLight
		);

		buffer.finish();
	}
}
