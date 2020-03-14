package de.henne90gen.chestcounter;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.henne90gen.chestcounter.service.dtos.Chest;
import de.henne90gen.chestcounter.service.dtos.ChestSearchResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Map;

public class Renderer {

    public static final int MARGIN = 5;

    public static void renderChestLabels(List<Chest> chests, int color, float maxDistance, float partialTickTime, MatrixStack matrixStack) {
        for (Chest chest : chests) {
            String text = chest.label;
            if (text == null) {
                continue;
            }
            BlockPos pos = Helper.getBlockPosFromChestID(chest.id);
            renderText(text, pos.getX(), pos.getY(), pos.getZ(), maxDistance, color, matrixStack, partialTickTime);
        }
    }

    private static void renderText(String text, float x, float y, float z, float maxDistance, int color, MatrixStack matrixStackIn, float partialTickTime) {
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

    public static void renderSearchResult(ChestSearchResult searchResult, ContainerScreen<?> screen) {
        int guiLeft = screen.getGuiLeft();
        int guiTop = screen.getGuiTop();
        int xSize = screen.getXSize();

        int RESULT_MARGIN = 25;
        int currentY = guiTop + RESULT_MARGIN;
        for (Map.Entry<String, Map<String, Integer>> entry : searchResult.entrySet()) {
            Minecraft.getInstance().fontRenderer.drawString(entry.getKey(), guiLeft + xSize + MARGIN, currentY, 0xffffff);
            currentY += MARGIN * 2;
            Map<String, Integer> value = entry.getValue();
            for (Map.Entry<String, Integer> amountEntry : value.entrySet()) {
                String amountString = amountEntry.getValue() + "x " + amountEntry.getKey();
                int xOffset = MARGIN * 2;
                Minecraft.getInstance().fontRenderer.drawString(amountString, guiLeft + xSize + MARGIN + xOffset, currentY, 0xffffff);
                currentY += MARGIN * 2;
            }
            currentY += MARGIN;
        }
    }
}
