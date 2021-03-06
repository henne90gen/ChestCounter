package de.henne90gen.chestcounter;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import de.henne90gen.chestcounter.db.entities.ChestConfig;
import de.henne90gen.chestcounter.db.entities.SearchResultPlacement;
import de.henne90gen.chestcounter.service.dtos.Chest;
import de.henne90gen.chestcounter.service.dtos.ChestSearchResult;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.util.math.vector.Vector3d;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import static net.minecraft.client.gui.AbstractGui.fill;

public class Renderer {

    public static final float CHEST_NAME_FONT_SIZE = 0.4F;
    public static final float SEARCH_RESULT_FONT_SIZE = CHEST_NAME_FONT_SIZE / 2.0F;
    public static final int MARGIN = 5;
    public static final int OFFSET_FROM_TOP = 17;
    public static final DecimalFormat TWO_DECIMAL_PLACES_FORMAT = new DecimalFormat("0.00");

    public static void renderChestLabels(List<Chest> chests, ChestSearchResult searchResult, float maxDistance, float partialTickTime, MatrixStack matrixStack) {
        for (Chest chest : chests) {
            List<BlockPos> positions = chest.getBlockPositions();
            boolean chestHasItemFromSearch = searchResult != null && searchResult.byId.containsKey(ChestSearchResult.keyId(chest.id)) && !searchResult.search.isEmpty();
            BlockPos pos = Helper.getClosestPositionToPlayer(positions, partialTickTime);

            String text = chest.label;
            boolean hasNoLabel = text == null || text.isEmpty();
            if (hasNoLabel && !chestHasItemFromSearch) {
                continue;
            } else if (hasNoLabel) {
                text = chest.id;
            }

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
            for (Map.Entry<String, Integer> entry : searchResult.byId.get(ChestSearchResult.keyId(chest.id)).items.entrySet()) {
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

    private static List<String> getSearchResultText(ChestSearchResult searchResult, boolean byId) {
        boolean searching = searchResult.search != null && !searchResult.search.isEmpty();
        Map<ChestSearchResult.Key, ChestSearchResult.Value> resultMap = byId ? searchResult.byId : searchResult.byLabel;

        List<Pair<Map.Entry<ChestSearchResult.Key, ChestSearchResult.Value>, Double>> results = resultMap
                .entrySet()
                .stream()
                .map(e -> new Pair<>(e, getDistanceToClosestPosition(e.getValue().positions)))
                .sorted(Comparator.comparing(Pair::getSecond))
                .collect(Collectors.toList());

        List<String> result = new ArrayList<>();
        for (Pair<Map.Entry<ChestSearchResult.Key, ChestSearchResult.Value>, Double> pair : results) {
            Map.Entry<ChestSearchResult.Key, ChestSearchResult.Value> entry = pair.getFirst();
            ChestSearchResult.Key key = entry.getKey();
            ChestSearchResult.Value value = entry.getValue();
            double distanceToChest = pair.getSecond();
            String formattedDistance = TWO_DECIMAL_PLACES_FORMAT.format(distanceToChest);

            String label = formattedDistance + "m" + " -> ";
            if (searching) {
                label += key.key;
            } else {
                if (!key.isId) {
                    label += key.key + " -> ";
                }

                ArrayList<Map.Entry<String, Integer>> entries = new ArrayList<>(value.items.entrySet());
                label += entries.stream()
                        .sorted(Map.Entry.comparingByValue())
                        .map(e -> e.getValue() + "x " + e.getKey())
                        .collect(Collectors.joining(", "));
            }

            result.add(label);

            if (searching) {
                for (Map.Entry<String, Integer> amountEntry : value.items.entrySet()) {
                    String amountString = "    " + amountEntry.getValue() + "x " + amountEntry.getKey();
                    result.add(amountString);
                }
                result.add("");
            }
        }
        return result;
    }

    public static void renderSearchResultInMenu(ContainerScreen<?> screen, ChestConfig config, ChestSearchResult searchResult, boolean byId) {
        boolean placeToTheRightOfInventory = config.searchResultPlacement == SearchResultPlacement.RIGHT_OF_INVENTORY;
        int baseX;
        if (placeToTheRightOfInventory) {
            int guiLeft = screen.getGuiLeft();
            int xSize = screen.getXSize();
            baseX = guiLeft + xSize + Renderer.MARGIN;
        } else {
            baseX = Renderer.MARGIN;
        }

        int backgroundWidth = 0;
        Renderer.renderSearchResult(baseX, OFFSET_FROM_TOP, searchResult, byId, backgroundWidth);
    }

    public static void renderSearchResultInGame(ChestConfig config, ChestSearchResult searchResult) {
        Minecraft mc = Minecraft.getInstance();
        MainWindow mainWindow = mc.getMainWindow();
        boolean placeToTheRightOfInventory = config.searchResultPlacement == SearchResultPlacement.RIGHT_OF_INVENTORY;

        int baseX;
        if (placeToTheRightOfInventory) {
            baseX = (int) (mainWindow.getScaledWidth() * 0.7F);
        } else {
            baseX = 1;
        }

        int backgroundWidth = (int) (mainWindow.getScaledWidth() * 0.3F);
        Renderer.renderSearchResult(baseX, 1, searchResult, false, backgroundWidth);
    }

    public static void renderSearchResult(int left, int top, ChestSearchResult searchResult, boolean byId, int backgroundWidth) {
        List<String> lines = getSearchResultText(searchResult, byId);

        Minecraft mc = Minecraft.getInstance();
        FontRenderer fontRenderer = mc.fontRenderer;

        MatrixStack matrixStack = new MatrixStack();
        matrixStack.push();
        matrixStack.translate(left, top, 0.0F);

        float scale = 0.65F;
        matrixStack.scale(1.0F, scale, 1.0F);

        int height = 9;
        if (backgroundWidth > 0) {
            Optional<Integer> backgroundHeightOpt = lines.stream()
                    .filter(Objects::nonNull)
                    .map(line -> line.isEmpty() ? height / 2 : height)
                    .reduce(Integer::sum);
            if (backgroundHeightOpt.isPresent()) {
                int x1 = -1;
                int x2 = -1 + backgroundWidth + 2;
                int y1 = -1;
                int y2 = -1 + backgroundHeightOpt.get();
                fill(matrixStack, x1, y1, x2, y2, -1873784752);
            }
        }

        matrixStack.scale(scale, 1.0F, 1.0F);

        int currentY = 0;
        for (String line : lines) {
            if (line == null) {
                continue;
            }

            if (line.isEmpty()) {
                currentY += height / 2;
                continue;
            }

            fontRenderer.drawString(matrixStack, line, 0, currentY, 14737632);
            currentY += height;
        }

        matrixStack.pop();
    }

    private static double getDistanceToClosestPosition(List<Vector3d> positions) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return 0.0;
        }

        double playerX = mc.player.getPosX();
        double playerY = mc.player.getPosY();
        double playerZ = mc.player.getPosZ();
        Vector3d playerPos = new Vector3d(playerX, playerY, playerZ);

        double closestDistance = Double.MAX_VALUE;
        for (Vector3d pos : positions) {
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
