package me.alpha432.oyvey.features.modules.render;

import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.settings.Setting;
import net.minecraft.world.entity.player.Player;

import java.util.HashSet;
import java.util.Set;

public class PlayerEspModule extends Module {
    private final Setting<Boolean> ignoreFriends = bool("IgnoreFriends", false);

    private final Set<Integer> glowingPlayers = new HashSet<>();

    public PlayerEspModule() {
        super("Player ESP", "Highlights players at all times, including through walls.", Category.RENDER);
    }

    @Override
    public void onTick() {
        if (nullCheck()) return;

        Set<Integer> currentTick = new HashSet<>();

        for (Player player : mc.level.players()) {
            if (player == mc.player || !player.isAlive() || player.isSpectator()) continue;
            if (ignoreFriends.getValue() && OyVey.friendManager.isFriend(player.getName().getString())) continue;

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
