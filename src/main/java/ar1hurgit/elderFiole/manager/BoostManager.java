package ar1hurgit.elderFiole.manager;

import ar1hurgit.elderFiole.ElderFiole;
import ar1hurgit.elderFiole.data.ActiveBoost;
import com.gamingmesh.jobs.container.Job;
import org.bukkit.Bukkit;
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
        // Nettoyer les boosts expirés
        plugin.getDatabaseManager().clearExpiredBoosts();

        // Sauvegarder tous les boosts actifs
        int savedCount = 0;
        for (Map.Entry<UUID, List<ActiveBoost>> entry : activeBoosts.entrySet()) {
            // D'abord supprimer les anciens boosts du joueur
            plugin.getDatabaseManager().clearPlayerBoosts(entry.getKey());

            // Puis sauvegarder les nouveaux
            for (ActiveBoost boost : entry.getValue()) {
                if (!boost.isExpired()) {
                    plugin.getDatabaseManager().saveActiveBoost(entry.getKey(), boost);
                    savedCount++;
                }
            }
        }

        plugin.getLogger().info("[ElderFiole] Sauvegarde de " + savedCount + " boosts actifs dans SQLite.");
    }

    public void loadBoosts() {
        // Charger uniquement les boosts des joueurs en ligne
        Map<UUID, List<ActiveBoost>> loadedBoosts = plugin.getDatabaseManager().loadActiveBoosts();

        int loadedCount = 0;
        for (Map.Entry<UUID, List<ActiveBoost>> entry : loadedBoosts.entrySet()) {
            // Ne charger que si le joueur est en ligne
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null && player.isOnline()) {
                activeBoosts.put(entry.getKey(), entry.getValue());
                loadedCount += entry.getValue().size();
            }
        }

        plugin.getLogger().info("[ElderFiole] Restauration de " + loadedCount + " boosts actifs depuis SQLite.");
    }

    public void loadPlayerBoosts(UUID playerUuid) {
        // Charger les boosts d'un joueur spécifique lors de sa connexion
        Map<UUID, List<ActiveBoost>> loadedBoosts = plugin.getDatabaseManager().loadActiveBoosts();
        List<ActiveBoost> playerBoosts = loadedBoosts.get(playerUuid);

        if (playerBoosts != null && !playerBoosts.isEmpty()) {
            activeBoosts.put(playerUuid, playerBoosts);
            plugin.getLogger().info("[ElderFiole] Chargement de " + playerBoosts.size() + " boosts pour " + playerUuid);
        }
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
