package ar1hurgit.elderFiole.manager;

import ar1hurgit.elderFiole.ElderFiole;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DailyCooldownManager {
    private final ElderFiole plugin;
    private final Map<UUID, Long> cooldowns;

    public DailyCooldownManager(ElderFiole plugin) {
        this.plugin = plugin;
        this.cooldowns = new HashMap<>();
        loadCooldowns();
    }

    private void loadCooldowns() {
        Map<UUID, Long> loadedCooldowns = plugin.getDatabaseManager().loadAllDailyCooldowns();
        cooldowns.putAll(loadedCooldowns);
    }

    private long getCooldownDuration() {
        int hours = plugin.getConfig().getInt("daily-vial.cooldown", 24);
        return TimeUnit.HOURS.toMillis(hours);
    }

    public boolean canUseDaily(UUID playerId) {
        Long lastUse = cooldowns.get(playerId);

        if (lastUse == null) {
            return true;
        }

        long currentTime = System.currentTimeMillis();
        return (currentTime - lastUse) >= getCooldownDuration();
    }

    public void setLastUse(UUID playerId) {
        long currentTime = System.currentTimeMillis();
        cooldowns.put(playerId, currentTime);
        plugin.getDatabaseManager().saveDailyCooldown(playerId, currentTime);
    }

    public String getTimeRemaining(UUID playerId) {
        Long lastUse = cooldowns.get(playerId);

        if (lastUse == null) {
            return "0h 0m";
        }

        long currentTime = System.currentTimeMillis();
        long remainingTime = getCooldownDuration() - (currentTime - lastUse);

        if (remainingTime <= 0) {
            return "0h 0m";
        }

        long hours = TimeUnit.MILLISECONDS.toHours(remainingTime);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(remainingTime) % 60;

        return hours + "h " + minutes + "m";
    }

    /**
     * Import cooldown data from a map (used for migration from YAML)
     */
    public void importCooldowns(Map<UUID, Long> cooldownData) {
        for (Map.Entry<UUID, Long> entry : cooldownData.entrySet()) {
            cooldowns.put(entry.getKey(), entry.getValue());
            plugin.getDatabaseManager().saveDailyCooldown(entry.getKey(), entry.getValue());
        }
    }
}
