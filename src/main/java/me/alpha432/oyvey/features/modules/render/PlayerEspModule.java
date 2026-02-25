package me.alpha432.oyvey.features.modules.render;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.event.impl.render.Render3DEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.settings.Setting;
import me.alpha432.oyvey.util.render.RenderUtil;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

import static com.mojang.blaze3d.systems.RenderSystem.setShader;

public class PlayerEspModule extends Module {
    private final Setting<Boolean> ignoreFriends = bool("IgnoreFriends", false);
    private final Setting<Color> fillTopColor = color("FillTop", 255, 114, 255, 60);
    private final Setting<Color> fillBottomColor = color("FillBottom", 128, 39, 255, 40);
    private final Setting<Color> lineColor = color("LineColor", 255, 255, 255, 200);
    private final Setting<Float> lineWidth = num("LineWidth", 1.6f, 0.5f, 4.0f);
    private final Setting<Boolean> tracers = bool("Tracers", true);
    private final Setting<Color> tracerStart = color("TracerStart", 255, 70, 170, 255);
    private final Setting<Color> tracerEnd = color("TracerEnd", 95, 180, 255, 255);

    private final Set<Integer> glowingPlayers = new HashSet<>();

    public PlayerEspModule() {
        super("Player ESP", "Highlights players at all times and draws tracer/box ESP.", Category.RENDER);
    }

    @Override
    public void onTick() {
        if (nullCheck()) return;

        Set<Integer> currentTick = new HashSet<>();

        for (Player player : mc.level.players()) {
            if (!canRenderPlayer(player)) continue;
            player.setGlowingTag(true);
            currentTick.add(player.getId());
            glowingPlayers.add(player.getId());
        }

        glowingPlayers.removeIf(id -> {
            if (currentTick.contains(id)) return false;
            var entity = mc.level.getEntity(id);
            if (entity instanceof Player player && player != mc.player) {
                player.setGlowingTag(false);
            }
            return true;
        });
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (nullCheck()) return;

        for (Player player : mc.level.players()) {
            if (!canRenderPlayer(player)) continue;

            AABB box = player.getBoundingBox().inflate(0.05);
            double midY = (box.minY + box.maxY) * 0.5;
            RenderUtil.drawBoxFilled(event.getMatrix(), new AABB(box.minX, box.minY, box.minZ, box.maxX, midY, box.maxZ), fillBottomColor.getValue());
            RenderUtil.drawBoxFilled(event.getMatrix(), new AABB(box.minX, midY, box.minZ, box.maxX, box.maxY, box.maxZ), fillTopColor.getValue());
            RenderUtil.drawBox(event.getMatrix(), box, lineColor.getValue(), lineWidth.getValue());

            if (tracers.getValue()) {
                drawTracer(event, player);
            }
        }
    }

    private boolean canRenderPlayer(Player player) {
        if (player == mc.player || !player.isAlive() || player.isSpectator()) return false;
        return !ignoreFriends.getValue() || !OyVey.friendManager.isFriend(player.getName().getString());
    }

    private void drawTracer(Render3DEvent event, Player player) {
        Vec3 cam = mc.getEntityRenderDispatcher().camera.getPosition();
        Vec3 from = mc.player.getEyePosition(event.getDelta()).subtract(cam);
        Vec3 to = player.getBoundingBox().getCenter().subtract(cam);

        setShader(GameRenderer::getPositionColorShader);
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
        builder.addVertex(event.getMatrix().last().pose(), (float) from.x, (float) from.y, (float) from.z)
                .setColor(tracerStart.getValue().getRed(), tracerStart.getValue().getGreen(), tracerStart.getValue().getBlue(), tracerStart.getValue().getAlpha());
        builder.addVertex(event.getMatrix().last().pose(), (float) to.x, (float) to.y, (float) to.z)
                .setColor(tracerEnd.getValue().getRed(), tracerEnd.getValue().getGreen(), tracerEnd.getValue().getBlue(), tracerEnd.getValue().getAlpha());
        BufferUploader.drawWithShader(builder.buildOrThrow());
    }

    @Override
    public void onDisable() {
        if (mc.level != null) {
            for (Integer id : glowingPlayers) {
                var entity = mc.level.getEntity(id);
                if (entity instanceof Player player && player != mc.player) {
                    player.setGlowingTag(false);
                }
            }
        }
        glowingPlayers.clear();
    }
}
