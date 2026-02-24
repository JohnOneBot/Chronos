package me.alpha432.oyvey.util.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import me.alpha432.oyvey.util.traits.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.awt.*;

public class RenderUtil implements Util {
    public static void rect(GuiGraphics context, float x1, float y1, float x2, float y2, int color) {
        context.fill(Math.round(x1), Math.round(y1), Math.round(x2), Math.round(y2), color);
    }

    public static void rect(GuiGraphics context, float x1, float y1, float x2, float y2, int color, float width) {
        int w = Math.max(1, Math.round(width));
        context.fill(Math.round(x1), Math.round(y1), Math.round(x2), Math.round(y1) + w, color);
        context.fill(Math.round(x2) - w, Math.round(y1), Math.round(x2), Math.round(y2), color);
        context.fill(Math.round(x1), Math.round(y2) - w, Math.round(x2), Math.round(y2), color);
        context.fill(Math.round(x1), Math.round(y1), Math.round(x1) + w, Math.round(y2), color);
    }

    public static void horizontalGradient(GuiGraphics context, float x1, float y1, float x2, float y2, Color left, Color right) {
        int start = Math.round(Math.min(x1, x2));
        int end = Math.round(Math.max(x1, x2));
        int yStart = Math.round(Math.min(y1, y2));
        int yEnd = Math.round(Math.max(y1, y2));
        int width = Math.max(1, end - start);

        for (int x = 0; x < width; x++) {
            float t = width <= 1 ? 0f : (float) x / (float) (width - 1);
            int r = (int) (left.getRed() + (right.getRed() - left.getRed()) * t);
            int g = (int) (left.getGreen() + (right.getGreen() - left.getGreen()) * t);
            int b = (int) (left.getBlue() + (right.getBlue() - left.getBlue()) * t);
            int a = (int) (left.getAlpha() + (right.getAlpha() - left.getAlpha()) * t);
            context.fill(start + x, yStart, start + x + 1, yEnd, new Color(r, g, b, a).getRGB());
        }
    }

    public static void verticalGradient(GuiGraphics context, float x1, float y1, float x2, float y2, Color top, Color bottom) {
        context.fillGradient(Math.round(x1), Math.round(y1), Math.round(x2), Math.round(y2), top.getRGB(), bottom.getRGB());
    }

    public static void gradient(GuiGraphics graphics, int x1, int y1, int x2, int y2, int topLeft, int bottomLeft, int bottomRight, int topRight) {
        horizontalGradient(graphics, x1, y1, x2, y2,
                new Color(topLeft, true), new Color(topRight, true));
    }

    public static void rect(PoseStack stack, float x1, float y1, float x2, float y2, int color) {
        rectFilled(stack, x1, y1, x2, y2, color);
    }

    public static void rect(PoseStack stack, float x1, float y1, float x2, float y2, int color, float width) {
        drawHorizontalLine(stack, x1, x2, y1, color, width);
        drawVerticalLine(stack, x2, y1, y2, color, width);
        drawHorizontalLine(stack, x1, x2, y2, color, width);
        drawVerticalLine(stack, x1, y1, y2, color, width);
    }

    protected static void drawHorizontalLine(PoseStack matrices, float x1, float x2, float y, int color, float width) {
        if (x2 < x1) {
            float i = x1;
            x1 = x2;
            x2 = i;
        }
        rectFilled(matrices, x1, y, x2 + width, y + width, color);
    }

    protected static void drawVerticalLine(PoseStack matrices, float x, float y1, float y2, int color, float width) {
        if (y2 < y1) {
            float i = y1;
            y1 = y2;
            y2 = i;
        }
        rectFilled(matrices, x, y1 + width, x + width, y2, color);
    }

    public static void rectFilled(PoseStack matrix, float x1, float y1, float x2, float y2, int color) {
        if (x1 < x2) {
            float i = x1;
            x1 = x2;
            x2 = i;
        }

        if (y1 < y2) {
            float i = y1;
            y1 = y2;
            y2 = i;
        }

        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;

        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.addVertex(matrix.last().pose(), x1, y2, 0.0F).setColor(r, g, b, a);
        bufferBuilder.addVertex(matrix.last().pose(), x2, y2, 0.0F).setColor(r, g, b, a);
        bufferBuilder.addVertex(matrix.last().pose(), x2, y1, 0.0F).setColor(r, g, b, a);
        bufferBuilder.addVertex(matrix.last().pose(), x1, y1, 0.0F).setColor(r, g, b, a);
        draw(bufferBuilder);
    }

    public static void horizontalGradient(PoseStack matrix, float x1, float y1, float x2, float y2, Color left, Color right) {
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.addVertex(matrix.last().pose(), x1, y1, 0.0F).setColor(left.getRed(), left.getGreen(), left.getBlue(), left.getAlpha());
        bufferBuilder.addVertex(matrix.last().pose(), x1, y2, 0.0F).setColor(left.getRed(), left.getGreen(), left.getBlue(), left.getAlpha());
        bufferBuilder.addVertex(matrix.last().pose(), x2, y2, 0.0F).setColor(right.getRed(), right.getGreen(), right.getBlue(), right.getAlpha());
        bufferBuilder.addVertex(matrix.last().pose(), x2, y1, 0.0F).setColor(right.getRed(), right.getGreen(), right.getBlue(), right.getAlpha());
        draw(bufferBuilder);
    }

    public static void verticalGradient(PoseStack matrix, float x1, float y1, float x2, float y2, Color top, Color bottom) {
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.addVertex(matrix.last().pose(), x1, y1, 0.0F).setColor(top.getRed(), top.getGreen(), top.getBlue(), top.getAlpha());
        bufferBuilder.addVertex(matrix.last().pose(), x1, y2, 0.0F).setColor(bottom.getRed(), bottom.getGreen(), bottom.getBlue(), bottom.getAlpha());
        bufferBuilder.addVertex(matrix.last().pose(), x2, y2, 0.0F).setColor(bottom.getRed(), bottom.getGreen(), bottom.getBlue(), bottom.getAlpha());
        bufferBuilder.addVertex(matrix.last().pose(), x2, y1, 0.0F).setColor(top.getRed(), top.getGreen(), top.getBlue(), top.getAlpha());
        draw(bufferBuilder);
    }

    public static void drawBoxFilled(PoseStack stack, AABB bb, Color c) {
        Vec3 camera = mc.getEntityRenderDispatcher().camera.getPosition();
        float minX = (float) (bb.minX - camera.x());
        float minY = (float) (bb.minY - camera.y());
        float minZ = (float) (bb.minZ - camera.z());
        float maxX = (float) (bb.maxX - camera.x());
        float maxY = (float) (bb.maxY - camera.y());
        float maxZ = (float) (bb.maxZ - camera.z());

        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int rgb = c.getRGB();

        bufferBuilder.addVertex(stack.last().pose(), minX, minY, minZ).setColor(rgb);
        bufferBuilder.addVertex(stack.last().pose(), maxX, minY, minZ).setColor(rgb);
        bufferBuilder.addVertex(stack.last().pose(), maxX, maxY, minZ).setColor(rgb);
        bufferBuilder.addVertex(stack.last().pose(), minX, maxY, minZ).setColor(rgb);

        bufferBuilder.addVertex(stack.last().pose(), minX, minY, maxZ).setColor(rgb);
        bufferBuilder.addVertex(stack.last().pose(), maxX, minY, maxZ).setColor(rgb);
        bufferBuilder.addVertex(stack.last().pose(), maxX, maxY, maxZ).setColor(rgb);
        bufferBuilder.addVertex(stack.last().pose(), minX, maxY, maxZ).setColor(rgb);

        bufferBuilder.addVertex(stack.last().pose(), minX, minY, minZ).setColor(rgb);
        bufferBuilder.addVertex(stack.last().pose(), minX, maxY, minZ).setColor(rgb);
        bufferBuilder.addVertex(stack.last().pose(), maxX, maxY, minZ).setColor(rgb);
        bufferBuilder.addVertex(stack.last().pose(), maxX, minY, minZ).setColor(rgb);

        bufferBuilder.addVertex(stack.last().pose(), maxX, minY, minZ).setColor(rgb);
        bufferBuilder.addVertex(stack.last().pose(), maxX, maxY, minZ).setColor(rgb);
        bufferBuilder.addVertex(stack.last().pose(), maxX, maxY, maxZ).setColor(rgb);
        bufferBuilder.addVertex(stack.last().pose(), maxX, minY, maxZ).setColor(rgb);

        bufferBuilder.addVertex(stack.last().pose(), minX, minY, maxZ).setColor(rgb);
        bufferBuilder.addVertex(stack.last().pose(), maxX, minY, maxZ).setColor(rgb);
        bufferBuilder.addVertex(stack.last().pose(), maxX, maxY, maxZ).setColor(rgb);
        bufferBuilder.addVertex(stack.last().pose(), minX, maxY, maxZ).setColor(rgb);

        bufferBuilder.addVertex(stack.last().pose(), minX, minY, minZ).setColor(rgb);
        bufferBuilder.addVertex(stack.last().pose(), minX, minY, maxZ).setColor(rgb);
        bufferBuilder.addVertex(stack.last().pose(), minX, maxY, maxZ).setColor(rgb);
        bufferBuilder.addVertex(stack.last().pose(), minX, maxY, minZ).setColor(rgb);

        draw(bufferBuilder);
    }

    public static void drawBoxFilled(PoseStack stack, Vec3 vec, Color c) {
        drawBoxFilled(stack, AABB.unitCubeFromLowerCorner(vec), c);
    }

    public static void drawBoxFilled(PoseStack stack, BlockPos bp, Color c) {
        drawBoxFilled(stack, new AABB(bp), c);
    }

    public static void drawBox(PoseStack stack, AABB box, Color c, float lineWidth) {
        Vec3 camera = mc.getEntityRenderDispatcher().camera.getPosition();
        float minX = (float) (box.minX - camera.x());
        float minY = (float) (box.minY - camera.y());
        float minZ = (float) (box.minZ - camera.z());
        float maxX = (float) (box.maxX - camera.x());
        float maxY = (float) (box.maxY - camera.y());
        float maxZ = (float) (box.maxZ - camera.z());

        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        PoseStack.Pose pose = stack.last();
        int color = c.getRGB();

        bufferBuilder.addVertex(pose, minX, minY, minZ).setColor(color).setNormal(1, 0, 0);
        bufferBuilder.addVertex(pose, maxX, minY, minZ).setColor(color).setNormal(1, 0, 0);
        bufferBuilder.addVertex(pose, minX, minY, minZ).setColor(color).setNormal(0, 1, 0);
        bufferBuilder.addVertex(pose, minX, maxY, minZ).setColor(color).setNormal(0, 1, 0);
        bufferBuilder.addVertex(pose, minX, minY, minZ).setColor(color).setNormal(0, 0, 1);
        bufferBuilder.addVertex(pose, minX, minY, maxZ).setColor(color).setNormal(0, 0, 1);

        bufferBuilder.addVertex(pose, maxX, minY, minZ).setColor(color).setNormal(0, 1, 0);
        bufferBuilder.addVertex(pose, maxX, maxY, minZ).setColor(color).setNormal(0, 1, 0);
        bufferBuilder.addVertex(pose, maxX, maxY, minZ).setColor(color).setNormal(-1, 0, 0);
        bufferBuilder.addVertex(pose, minX, maxY, minZ).setColor(color).setNormal(-1, 0, 0);
        bufferBuilder.addVertex(pose, minX, maxY, minZ).setColor(color).setNormal(0, 0, 1);
        bufferBuilder.addVertex(pose, minX, maxY, maxZ).setColor(color).setNormal(0, 0, 1);

        bufferBuilder.addVertex(pose, minX, maxY, maxZ).setColor(color).setNormal(0, -1, 0);
        bufferBuilder.addVertex(pose, minX, minY, maxZ).setColor(color).setNormal(0, -1, 0);
        bufferBuilder.addVertex(pose, minX, minY, maxZ).setColor(color).setNormal(1, 0, 0);
        bufferBuilder.addVertex(pose, maxX, minY, maxZ).setColor(color).setNormal(1, 0, 0);
        bufferBuilder.addVertex(pose, maxX, minY, maxZ).setColor(color).setNormal(0, 0, -1);
        bufferBuilder.addVertex(pose, maxX, minY, minZ).setColor(color).setNormal(0, 0, -1);

        bufferBuilder.addVertex(pose, minX, maxY, maxZ).setColor(color).setNormal(1, 0, 0);
        bufferBuilder.addVertex(pose, maxX, maxY, maxZ).setColor(color).setNormal(1, 0, 0);
        bufferBuilder.addVertex(pose, maxX, minY, maxZ).setColor(color).setNormal(0, 1, 0);
        bufferBuilder.addVertex(pose, maxX, maxY, maxZ).setColor(color).setNormal(0, 1, 0);
        bufferBuilder.addVertex(pose, maxX, maxY, minZ).setColor(color).setNormal(0, 0, 1);
        bufferBuilder.addVertex(pose, maxX, maxY, maxZ).setColor(color).setNormal(0, 0, 1);

        RenderSystem.lineWidth(Math.max(1.0f, lineWidth));
        draw(bufferBuilder);
        RenderSystem.lineWidth(1.0f);
    }

    public static void drawBox(PoseStack stack, Vec3 vec, Color c, float lineWidth) {
        drawBox(stack, AABB.unitCubeFromLowerCorner(vec), c, lineWidth);
    }

    public static void drawBox(PoseStack stack, BlockPos bp, Color c, float lineWidth) {
        drawBox(stack, new AABB(bp), c, lineWidth);
    }

    public static PoseStack matrixFrom(Vec3 pos) {
        PoseStack matrices = new PoseStack();
        Camera camera = mc.gameRenderer.getMainCamera();
        matrices.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
        matrices.mulPose(Axis.YP.rotationDegrees(camera.getYRot() + 180.0F));
        matrices.translate(pos.x() - camera.getPosition().x, pos.y() - camera.getPosition().y, pos.z() - camera.getPosition().z);
        return matrices;
    }

    private static void draw(BufferBuilder builder) {
        builder.buildOrThrow();
    }
}
