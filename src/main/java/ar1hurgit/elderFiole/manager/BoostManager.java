package ar1hurgit.elderFiole.manager;

import ar1hurgit.elderFiole.ElderFiole;
import ar1hurgit.elderFiole.data.ActiveBoost;
import com.gamingmesh.jobs.container.Job;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BoostManager {
    private final ElderFiole plugin;
    private final Map<UUID, List<ActiveBoost>> activeBoosts;

    public BoostManager(ElderFiole plugin) {
        this.plugin = plugin;
        this.activeBoosts = new ConcurrentHashMap<>();
        startCleanupTask();
    }

    public boolean activateBoost(Player player, String jobName, double multiplier, int durationMinutes) {
        UUID playerId = player.getUniqueId();

        // Check if boost already active for this job
        List<ActiveBoost> currentBoosts = activeBoosts.get(playerId);
        if (currentBoosts != null) {
            for (ActiveBoost boost : currentBoosts) {
                if (!boost.isExpired() && boost.getJobName().equalsIgnoreCase(jobName)) {
                    return false; // Boost already active
                }
            }
        }

        long expirationTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000L);
        ActiveBoost boost = new ActiveBoost(jobName, multiplier, expirationTime);

        activeBoosts.computeIfAbsent(playerId, k -> new ArrayList<>()).add(boost);
        return true;
    }

    public void saveBoosts() {
        FileConfiguration data = plugin.getCooldownManager().getDataConfig();

        // Always clear old boosts to ensure clean state
        data.set("active-boosts", null);

        if (activeBoosts.isEmpty()) {
            plugin.getCooldownManager().saveData();
            return;
        }

        int savedCount = 0;
        for (Map.Entry<UUID, List<ActiveBoost>> entry : activeBoosts.entrySet()) {
            String uuidInfo = entry.getKey().toString();
            List<String> boostsData = new ArrayList<>();

            for (ActiveBoost boost : entry.getValue()) {
                if (!boost.isExpired()) {
                    // Format: JobName;Multiplier;ExpirationTime
                    String serialized = boost.getJobName() + ";" + boost.getMultiplier() + ";"
                            + boost.getExpirationTime();
                    boostsData.add(serialized);
                    savedCount++;
                }
            }

            if (!boostsData.isEmpty()) {
                data.set("active-boosts." + uuidInfo, boostsData);
            }
        }

        plugin.getCooldownManager().saveData();
        plugin.getLogger().info("[ElderFiole] Sauvegarde de " + savedCount + " boosts actifs.");
    }

    public void loadBoosts() {
        FileConfiguration data = plugin.getCooldownManager().getDataConfig();
        if (!data.contains("active-boosts")) {
            plugin.getLogger().info("[ElderFiole] Aucun boost actif à restaurer.");
            return;
        }

        ConfigurationSection boostsSection = data.getConfigurationSection("active-boosts");
        if (boostsSection == null)
            return;

        int loadedCount = 0;
        for (String uuidStr : boostsSection.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                List<String> boostsList = data.getStringList("active-boosts." + uuidStr);
                List<ActiveBoost> loadedBoosts = new ArrayList<>();

                for (String boostStr : boostsList) {
                    try {
                        String[] parts = boostStr.split(";");
                        if (parts.length == 3) {
                            String jobName = parts[0];
                            double multiplier = Double.parseDouble(parts[1]);
                            long expiration = Long.parseLong(parts[2]);

                            if (expiration > System.currentTimeMillis()) {
                                loadedBoosts.add(new ActiveBoost(jobName, multiplier, expiration));
                                loadedCount++;
                            }
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("[ElderFiole] Erreur format boost: " + boostStr);
                    }
                }

                if (!loadedBoosts.isEmpty()) {
                    activeBoosts.put(uuid, loadedBoosts);
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("[ElderFiole] UUID invalide: " + uuidStr);
            }
        }
        plugin.getLogger().info("[ElderFiole] Restauration de " + loadedCount + " boosts actifs.");
    }

    public double getTotalMultiplier(Player player, Job job) {
        UUID playerId = player.getUniqueId();
        List<ActiveBoost> boosts = activeBoosts.get(playerId);

        if (boosts == null || boosts.isEmpty()) {
            return 1.0;
        }

        double totalMultiplier = 1.0;
        String jobName = job.getName();

        for (ActiveBoost boost : boosts) {
            if (!boost.isExpired() && boost.getJobName().equalsIgnoreCase(jobName)) {
                totalMultiplier *= boost.getMultiplier();
            }
        }

        return totalMultiplier;
    }

    public List<ActiveBoost> getActiveBoosts(Player player, String jobName) {
        UUID playerId = player.getUniqueId();
        List<ActiveBoost> boosts = activeBoosts.get(playerId);

        if (boosts == null) {
            return new ArrayList<>();
        }

        List<ActiveBoost> activeJobBoosts = new ArrayList<>();
        for (ActiveBoost boost : boosts) {
            if (!boost.isExpired() && boost.getJobName().equalsIgnoreCase(jobName)) {
                activeJobBoosts.add(boost);
            }
        }

        return activeJobBoosts;
    }

    public List<ActiveBoost> getPlayerBoosts(Player player) {
        return activeBoosts.getOrDefault(player.getUniqueId(), new ArrayList<>());
    }

    public void clearPlayerBoosts(UUID playerId) {
        activeBoosts.remove(playerId);
    }

    private void startCleanupTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Map.Entry<UUID, List<ActiveBoost>> entry : activeBoosts.entrySet()) {
                entry.getValue().removeIf(ActiveBoost::isExpired);

                if (entry.getValue().isEmpty()) {
                    activeBoosts.remove(entry.getKey());
                }
            }
        }, 20L * 60L, 20L * 60L); // Run every minute
    }

    public void shutdown() {
        activeBoosts.clear();
    }
}
