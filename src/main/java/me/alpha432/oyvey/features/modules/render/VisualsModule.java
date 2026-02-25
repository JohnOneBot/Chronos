package me.alpha432.oyvey.features.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.event.impl.render.Render3DEvent;
import me.alpha432.oyvey.features.Feature;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.settings.Setting;
import net.minecraft.client.ParticleStatus;
import net.minecraft.core.particles.DustParticleOptions;
import org.joml.Vector3f;

import java.awt.*;

public class VisualsModule extends Module {
    public final Setting<Boolean> customFog = bool("CustomFog", true);
    public final Setting<Color> fogColor = color("FogColor", 130, 85, 255, 255);
    public final Setting<Float> fogStart = num("FogStart", 6.0f, 0.0f, 128.0f);
    public final Setting<Float> fogEnd = num("FogEnd", 34.0f, 4.0f, 512.0f);

    public final Setting<ParticleMode> particleMode = mode("ParticleMode", ParticleMode.ALL);
    public final Setting<Boolean> ambientParticles = bool("AmbientParticles", true);
    public final Setting<Integer> particleAmount = num("ParticleAmount", 8, 0, 40);
    public final Setting<Float> particleSpread = num("ParticleSpread", 1.8f, 0.2f, 6.0f);
    public final Setting<Float> particleSpeed = num("ParticleSpeed", 0.035f, 0.0f, 0.2f);
    public final Setting<Float> particleSize = num("ParticleSize", 1.25f, 0.2f, 4.0f);
    public final Setting<Color> particleColor = color("ParticleColor", 184, 120, 255, 255);

    private ParticleStatus previousStatus;

    public VisualsModule() {
        super("Visuals", "Adds a customizable and vivid visual environment", Category.RENDER);
    }

    public static VisualsModule getInstance() {
        return OyVey.moduleManager != null ? OyVey.moduleManager.getModuleByClass(VisualsModule.class) : null;
    }

    @Override
    public void onEnable() {
        if (Feature.nullCheck()) return;
        previousStatus = mc.options.particles().get();
        applyParticleMode();
    }

    @Override
    public void onDisable() {
        if (Feature.nullCheck()) return;
        if (previousStatus != null) {
            mc.options.particles().set(previousStatus);
            previousStatus = null;
        }
    }

    @Override
    public void onTick() {
        if (Feature.nullCheck()) return;

        applyParticleMode();

        if (!ambientParticles.getValue() || particleAmount.getValue() <= 0) return;

        Color color = particleColor.getValue();
        Vector3f dustColor = new Vector3f(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f);
        DustParticleOptions options = new DustParticleOptions(dustColor, particleSize.getValue());

        double px = mc.player.getX();
        double py = mc.player.getY() + 1.0;
        double pz = mc.player.getZ();

        for (int i = 0; i < particleAmount.getValue(); i++) {
            double ox = (mc.level.random.nextDouble() * 2.0 - 1.0) * particleSpread.getValue();
            double oy = (mc.level.random.nextDouble() * 2.0 - 1.0) * (particleSpread.getValue() * 0.5);
            double oz = (mc.level.random.nextDouble() * 2.0 - 1.0) * particleSpread.getValue();

            double motionX = (mc.level.random.nextDouble() * 2.0 - 1.0) * particleSpeed.getValue();
            double motionY = (mc.level.random.nextDouble() * 2.0 - 1.0) * particleSpeed.getValue();
            double motionZ = (mc.level.random.nextDouble() * 2.0 - 1.0) * particleSpeed.getValue();

            mc.level.addParticle(options, px + ox, py + oy, pz + oz, motionX, motionY, motionZ);
        }
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (!customFog.getValue()) return;

        float start = fogStart.getValue();
        float end = Math.max(fogEnd.getValue(), start + 0.5f);

        RenderSystem.setShaderFogStart(start);
        RenderSystem.setShaderFogEnd(end);
    }

    private void applyParticleMode() {
        ParticleStatus target = switch (particleMode.getValue()) {
            case ALL -> ParticleStatus.ALL;
            case DECREASED -> ParticleStatus.DECREASED;
            case MINIMAL -> ParticleStatus.MINIMAL;
        };

        if (mc.options.particles().get() != target) {
            mc.options.particles().set(target);
        }
    }

    public boolean useCustomFog() {
        return isEnabled() && customFog.getValue();
    }

    public Color getActiveFogColor() {
        return fogColor.getValue();
    }

    public enum ParticleMode {
        ALL,
        DECREASED,
        MINIMAL
    }
}
