package me.alpha432.oyvey.features.modules.render;

import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.event.impl.render.Render3DEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.settings.Setting;
import me.alpha432.oyvey.util.render.RenderUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class PlayerEspModule extends Module {
    private final Setting<Boolean> ignoreFriends = bool("IgnoreFriends", false);
    private final Setting<Color> fillTopColor = color("FillTop", 255, 114, 255, 90);
    private final Setting<Color> fillBottomColor = color("FillBottom", 128, 39, 255, 65);
    private final Setting<Color> lineColor = color("LineColor", 255, 255, 255, 220);
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
            renderPseudoFill(event, box);
            RenderUtil.drawBox(event.getMatrix(), box, lineColor.getValue(), lineWidth.getValue());

            if (tracers.getValue()) {
                drawTracerDotted(event, player);
            }
        }
    }

    private void renderPseudoFill(Render3DEvent event, AABB box) {
        double minY = box.minY;
        double maxY = box.maxY;
        for (int i = 0; i < 5; i++) {
            double t = i / 4.0;
            Color step = blend(fillBottomColor.getValue(), fillTopColor.getValue(), t);
            double y = minY + (maxY - minY) * t;
            AABB slice = new AABB(box.minX, y, box.minZ, box.maxX, Math.min(maxY, y + 0.08), box.maxZ);
            RenderUtil.drawBox(event.getMatrix(), slice, step, Math.max(1.0f, lineWidth.getValue() * 0.6f));
        }
    }

    private void drawTracerDotted(Render3DEvent event, Player player) {
        Vec3 from = mc.player.getEyePosition(event.getDelta());
        Vec3 to = player.getBoundingBox().getCenter();

        for (int i = 0; i < 18; i++) {
            double t = i / 17.0;
            Color dotColor = blend(tracerStart.getValue(), tracerEnd.getValue(), t);
            Vec3 dot = from.lerp(to, t);
            AABB marker = new AABB(dot.x - 0.03, dot.y - 0.03, dot.z - 0.03, dot.x + 0.03, dot.y + 0.03, dot.z + 0.03);
            RenderUtil.drawBox(event.getMatrix(), marker, dotColor, 1.0f);
        }
    }

    private boolean canRenderPlayer(Player player) {
        if (player == mc.player || !player.isAlive() || player.isSpectator()) return false;
        return !ignoreFriends.getValue() || !OyVey.friendManager.isFriend(player.getName().getString());
    }

    private Color blend(Color start, Color end, double t) {
        int r = (int) (start.getRed() + (end.getRed() - start.getRed()) * t);
        int g = (int) (start.getGreen() + (end.getGreen() - start.getGreen()) * t);
        int b = (int) (start.getBlue() + (end.getBlue() - start.getBlue()) * t);
        int a = (int) (start.getAlpha() + (end.getAlpha() - start.getAlpha()) * t);
        return new Color(r, g, b, a);
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
