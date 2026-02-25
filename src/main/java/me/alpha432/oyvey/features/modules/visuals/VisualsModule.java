package me.alpha432.oyvey.features.modules.visuals;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.settings.Setting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;

import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

public class VisualsModule extends Module {
    private final Setting<Boolean> customFog = bool("CustomFog", true);
    private final Setting<Color> fogColor = color("FogColor", 166, 110, 255, 255);

    private final Setting<Boolean> skyParticles = bool("SkyParticles", true);
    private final Setting<ParticleStyle> particleStyle = mode("ParticleStyle", ParticleStyle.END_ROD);
    private final Setting<Integer> particleCount = num("ParticleCount", 8, 1, 40);
    private final Setting<Float> particleHeight = num("ParticleHeight", 28.0f, 4.0f, 80.0f);
    private final Setting<Float> particleSpread = num("ParticleSpread", 26.0f, 4.0f, 80.0f);
    private final Setting<Float> particleSpeed = num("ParticleSpeed", 0.04f, 0.01f, 0.30f);
    private final Setting<Integer> particleDelay = num("ParticleDelay", 2, 1, 20);

    public VisualsModule() {
        super("Visuals", "Stunning ambience tuning for fog and sky particles.", Category.VISUALS);
    }

    @Override
    public void onTick() {
        if (nullCheck() || !skyParticles.getValue()) return;
        if (mc.player.tickCount % particleDelay.getValue() != 0) return;

        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < particleCount.getValue(); i++) {
            double x = mc.player.getX() + random.nextDouble(-particleSpread.getValue(), particleSpread.getValue());
            double y = mc.player.getY() + particleHeight.getValue() + random.nextDouble(-5.0, 5.0);
            double z = mc.player.getZ() + random.nextDouble(-particleSpread.getValue(), particleSpread.getValue());

            double vx = random.nextDouble(-particleSpeed.getValue(), particleSpeed.getValue());
            double vy = random.nextDouble(-particleSpeed.getValue(), particleSpeed.getValue());
            double vz = random.nextDouble(-particleSpeed.getValue(), particleSpeed.getValue());
            mc.level.addParticle(particleStyle.getValue().type, x, y, z, vx, vy, vz);
        }
    }

    public boolean shouldOverrideFog() {
        return isEnabled() && customFog.getValue();
    }

    public Color getFogColor() {
        return fogColor.getValue();
    }

    private enum ParticleStyle {
        END_ROD(ParticleTypes.END_ROD),
        WITCH(ParticleTypes.WITCH),
        ENCHANT(ParticleTypes.ENCHANT),
        PORTAL(ParticleTypes.PORTAL);

        private final ParticleOptions type;

        ParticleStyle(ParticleOptions type) {
            this.type = type;
        }
    }
}
