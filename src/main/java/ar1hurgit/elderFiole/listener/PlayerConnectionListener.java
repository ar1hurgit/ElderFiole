package ar1hurgit.elderFiole.listener;

import ar1hurgit.elderFiole.ElderFiole;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {
    private final ElderFiole plugin;

    public PlayerConnectionListener(ElderFiole plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Charger les boosts du joueur depuis la base de données
        plugin.getBoostManager().loadPlayerBoosts(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Sauvegarder et décharger les boosts du joueur
        plugin.getBoostManager().saveBoosts();

        plugin.getBoostManager().clearPlayerBoosts(event.getPlayer().getUniqueId());
    }
}
