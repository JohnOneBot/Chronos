package me.alpha432.oyvey.features.modules.visuals;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.settings.Setting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class VisualsModule extends Module {
    // Environment
    private final Setting<Boolean> customFog = bool("CustomFog", true);
    private final Setting<Color> fogColor = color("FogColor", 166, 110, 255, 255);
    private final Setting<WeatherPreset> forceWeather = mode("ForceWeather", WeatherPreset.OFF);

    // Sky particles
    private final Setting<Boolean> skyParticles = bool("SkyParticles", true);
    private final Setting<ParticleStyle> particleStyle = mode("ParticleStyle", ParticleStyle.END_ROD);
    private final Setting<Integer> particleCount = num("ParticleCount", 8, 1, 60);
    private final Setting<Float> particleHeight = num("ParticleHeight", 28.0f, 4.0f, 120.0f);
    private final Setting<Float> particleSpread = num("ParticleSpread", 26.0f, 4.0f, 100.0f);
    private final Setting<Float> particleSpeed = num("ParticleSpeed", 0.04f, 0.01f, 0.50f);
    private final Setting<Integer> particleDelay = num("ParticleDelay", 2, 1, 20);

    // ESP-like ambience
    private final Setting<Boolean> entityGlow = bool("EntityGlow", false);
    private final Setting<Boolean> playersOnly = bool("GlowPlayersOnly", true);
    private final Setting<Float> glowRange = num("GlowRange", 48.0f, 8.0f, 160.0f);

    private final Set<Integer> glowingEntities = new HashSet<>();

    public VisualsModule() {
        super("Visuals", "Soup-style visuals: environment, particles, and ESP-like ambience.", Category.VISUALS);
    }

    @Override
    public void onTick() {
        if (nullCheck()) return;

        applyTimeAndWeather();
        spawnSkyParticles();
        updateGlowEntities();
    }

    @Override
    public void onDisable() {
        clearGlowingEntities();
    }

    public boolean shouldOverrideFog() {
        return isEnabled() && customFog.getValue();
    }

    public Color getFogColor() {
        return fogColor.getValue();
    }

    private void applyTimeAndWeather() {
        switch (forceWeather.getValue()) {
            case CLEAR -> {
                mc.level.setRainLevel(0.0f);
                mc.level.setThunderLevel(0.0f);
            }
            case RAIN -> {
                mc.level.setRainLevel(1.0f);
                mc.level.setThunderLevel(0.0f);
            }
            case THUNDER -> {
                mc.level.setRainLevel(1.0f);
                mc.level.setThunderLevel(1.0f);
            }
            case OFF -> {
            }
        }
    }

    private void spawnSkyParticles() {
        if (!skyParticles.getValue()) return;
        if (mc.player.tickCount % particleDelay.getValue() != 0) return;

        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < particleCount.getValue(); i++) {
            double x = mc.player.getX() + random.nextDouble(-particleSpread.getValue(), particleSpread.getValue());
            double y = mc.player.getY() + particleHeight.getValue() + random.nextDouble(-8.0, 8.0);
            double z = mc.player.getZ() + random.nextDouble(-particleSpread.getValue(), particleSpread.getValue());

            double vx = random.nextDouble(-particleSpeed.getValue(), particleSpeed.getValue());
            double vy = random.nextDouble(-particleSpeed.getValue(), particleSpeed.getValue());
            double vz = random.nextDouble(-particleSpeed.getValue(), particleSpeed.getValue());
            mc.level.addParticle(particleStyle.getValue().type, x, y, z, vx, vy, vz);
        }
    }

    private void updateGlowEntities() {
        if (!entityGlow.getValue()) {
            clearGlowingEntities();
            return;
        }

        double range = glowRange.getValue();
        Set<Integer> currentTick = new HashSet<>();

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity) || entity == mc.player) continue;
            if (playersOnly.getValue() && !(entity instanceof Player)) continue;
            if (mc.player.distanceToSqr(entity) > range * range) continue;

            entity.setGlowingTag(true);
            currentTick.add(entity.getId());
            glowingEntities.add(entity.getId());
        }

        glowingEntities.removeIf(id -> {
            if (currentTick.contains(id)) return false;
            Entity entity = mc.level.getEntity(id);
            if (entity != null) {
                entity.setGlowingTag(false);
            }
            return true;
        });
    }

    private void clearGlowingEntities() {
        if (mc.level == null) {
            glowingEntities.clear();
            return;
        }

        for (Integer id : glowingEntities) {
            Entity entity = mc.level.getEntity(id);
            if (entity != null) {
                entity.setGlowingTag(false);
            }
        }
        glowingEntities.clear();
    }

    private enum ParticleStyle {
        END_ROD(ParticleTypes.END_ROD),
        WITCH(ParticleTypes.WITCH),
        ENCHANT(ParticleTypes.ENCHANT),
        PORTAL(ParticleTypes.PORTAL),
        SPORE(ParticleTypes.CRIMSON_SPORE),
        WHITE_ASH(ParticleTypes.WHITE_ASH);

        private final ParticleOptions type;

        ParticleStyle(ParticleOptions type) {
            this.type = type;
        }
    }

    private enum WeatherPreset {
        OFF,
        CLEAR,
        RAIN,
        THUNDER
    }
}
