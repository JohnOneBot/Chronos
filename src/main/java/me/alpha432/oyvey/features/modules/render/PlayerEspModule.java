package me.alpha432.oyvey.features.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.alpha432.oyvey.event.impl.render.Render3DEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.settings.Setting;
import me.alpha432.oyvey.util.render.RenderUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.awt.*;

public class PlayerEspModule extends Module {
    private final Setting<Color> fillColor = color("FillColor", 168, 85, 247, 45);
    private final Setting<Color> lineColor = color("LineColor", 236, 72, 153, 220);
    private final Setting<Float> lineWidth = num("LineWidth", 1.8f, 0.5f, 5.0f);
    private final Setting<Boolean> ignoreFriends = bool("IgnoreFriends", false);

    public PlayerEspModule() {
        super("Player ESP", "Draws player boxes through walls at all times.", Category.RENDER);
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (nullCheck()) return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();

        for (Player player : mc.level.players()) {
            if (player == mc.player || !player.isAlive() || player.isSpectator()) continue;
            if (ignoreFriends.getValue() && me.alpha432.oyvey.OyVey.friendManager.isFriend(player.getName().getString())) continue;

            AABB box = player.getBoundingBox().inflate(0.05);
            RenderUtil.drawBoxFilled(event.getMatrix(), box, fillColor.getValue());
            RenderUtil.drawBox(event.getMatrix(), box, lineColor.getValue(), lineWidth.getValue());
        }

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
}
