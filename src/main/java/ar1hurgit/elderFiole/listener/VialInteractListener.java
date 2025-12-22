package ar1hurgit.elderFiole.listener;

import ar1hurgit.elderFiole.ElderFiole;
import ar1hurgit.elderFiole.item.VialItem;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class VialInteractListener implements Listener {
    private final ElderFiole plugin;

    public VialInteractListener(ElderFiole plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (!VialItem.isVial(plugin, item)) {
            return;
        }

        event.setCancelled(true);

        // Extract vial data
        String jobName = VialItem.getJobName(plugin, item);
        Double multiplier = VialItem.getMultiplier(plugin, item);
        Integer duration = VialItem.getDuration(plugin, item);

        if (jobName == null || multiplier == null || duration == null) {
            return;
        }

        // Activate boost
        if (!plugin.getBoostManager().activateBoost(player, jobName, multiplier, duration)) {
            String message = plugin.getConfig().getString("messages.boost-already-active", "&cBoost déjà actif !");
            message = message.replace("{job}", jobName);
            String prefix = plugin.getConfig().getString("messages.prefix", "&8[&6ElderFiole&8] &r");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));
            return;
        }

        // Send message
        String message = plugin.getConfig().getString("messages.vial-activated", "&aBoost activé !");
        message = message.replace("{multiplier}", String.format("%.2f", multiplier))
                .replace("{job}", jobName)
                .replace("{duration}", String.valueOf(duration));
        String prefix = plugin.getConfig().getString("messages.prefix", "&8[&6ElderFiole&8] &r");
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));

        // Consume item
        item.setAmount(item.getAmount() - 1);
    }
}
