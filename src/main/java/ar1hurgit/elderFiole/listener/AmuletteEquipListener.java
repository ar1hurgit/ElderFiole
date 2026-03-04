package ar1hurgit.elderFiole.listener;

import ar1hurgit.elderFiole.ElderFiole;
import ar1hurgit.elderFiole.manager.JobBoostItemManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AmuletteEquipListener implements Listener {
    private final ElderFiole plugin;

    // Stocke l'ID de la dernière amulette équipée par joueur
    private final Map<UUID, String> lastEquippedAmulette = new ConcurrentHashMap<>();

    public AmuletteEquipListener(ElderFiole plugin) {
        this.plugin = plugin;
        startOffHandCheckTask();
    }

    private void startOffHandCheckTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    checkOffHandChange(player);
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void checkOffHandChange(Player player) {
        ItemStack offHand = player.getInventory().getItemInOffHand();
        UUID playerId = player.getUniqueId();

        // Récupérer les données de l'amulette actuelle
        String currentAmuletteId = null;
        JobBoostItemManager.AmuletteData currentData = null;
        
        if (offHand != null && offHand.hasItemMeta()) {
            currentData = plugin.getJobBoostItemManager().getAmuletteDataFromStack(offHand);
            if (currentData != null) {
                currentAmuletteId = currentData.id;
            }
        }

        String lastId = lastEquippedAmulette.get(playerId);

        // Si l'amulette a changé
        if (!java.util.Objects.equals(lastId, currentAmuletteId)) {
            // Désactiver l'ancienne amulette si elle existait et était active
            if (lastId != null) {
                // Chercher l'ancienne amulette dans l'inventaire et la désactiver
                for (ItemStack stack : player.getInventory().getContents()) {
                    JobBoostItemManager.AmuletteData data = plugin.getJobBoostItemManager().getAmuletteDataFromStack(stack);
                    if (data != null && data.id.equals(lastId) && data.active) {
                        plugin.getJobBoostItemManager().toggleAmulette(player, stack);
                        player.sendMessage(plugin.getConfig().getString("messages.prefix") +
                                "§cAmulette de §e" + data.jobName + " §cdésactivée (déséquipée).");
                        break;
                    }
                }
            }

            // Mettre à jour le cache
            if (currentAmuletteId != null) {
                lastEquippedAmulette.put(playerId, currentAmuletteId);
            } else {
                lastEquippedAmulette.remove(playerId);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        
        if (offHand != null && offHand.hasItemMeta()) {
            JobBoostItemManager.AmuletteData data = plugin.getJobBoostItemManager().getAmuletteDataFromStack(offHand);
            if (data != null) {
                lastEquippedAmulette.put(player.getUniqueId(), data.id);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Désactiver l'amulette active à la déconnexion
        Player player = event.getPlayer();
        String lastId = lastEquippedAmulette.get(player.getUniqueId());
        
        if (lastId != null) {
            for (ItemStack stack : player.getInventory().getContents()) {
                JobBoostItemManager.AmuletteData data = plugin.getJobBoostItemManager().getAmuletteDataFromStack(stack);
                if (data != null && data.id.equals(lastId) && data.active) {
                    plugin.getJobBoostItemManager().toggleAmulette(player, stack);
                    break;
                }
            }
        }
        
        lastEquippedAmulette.remove(player.getUniqueId());
    }
}
