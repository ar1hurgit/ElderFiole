package ar1hurgit.elderFiole.command;

import ar1hurgit.elderFiole.ElderFiole;
import ar1hurgit.elderFiole.item.VialItem;
import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.container.Job;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;

public class DailyFioleCommand implements CommandExecutor {
    private final ElderFiole plugin;
    private final Random random;

    public DailyFioleCommand(ElderFiole plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {return true;}

        Player player = (Player) sender;

        if (!player.hasPermission("elderfiole.dailyfiole")) {
            String message = plugin.getConfig().getString("messages.no-permission", "&cPas de permission.");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return true;
        }

        // Check cooldown
        if (!plugin.getCooldownManager().canUseDaily(player.getUniqueId())) {
            String message = plugin.getConfig().getString("messages.daily-cooldown",
                    "&cVous devez attendre encore {time}.");
            String timeRemaining = plugin.getCooldownManager().getTimeRemaining(player.getUniqueId());
            message = message.replace("{time}", timeRemaining);
            String prefix = plugin.getConfig().getString("messages.prefix", "&8[&6ElderFiole&8] &r");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));
            return true;
        }

        // Check inventory space
        if (player.getInventory().firstEmpty() == -1) {
            String message = plugin.getConfig().getString("messages.inventory-full",
                    "&cVotre inventaire est plein !");
            String prefix = plugin.getConfig().getString("messages.prefix", "&8[&6ElderFiole&8] &r");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));
            return true;
        }

        // Get all loaded jobs
        List<Job> allJobs = Jobs.getJobs();
        if (allJobs == null || allJobs.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Aucun métier chargé par Jobs Reborn !");
            return true;
        }

        // Select random job
        Job randomJob = allJobs.get(random.nextInt(allJobs.size()));
        String jobName = randomJob.getName();

        // Get configurable values
        double multiplier = plugin.getConfig().getDouble("daily-vial.multiplier", 1.10);
        int duration = plugin.getConfig().getInt("daily-vial.duration", 120);

        // Create vial
        ItemStack vial = VialItem.createVial(plugin, jobName, multiplier, duration);
        player.getInventory().addItem(vial);

        // Update cooldown
        plugin.getCooldownManager().setLastUse(player.getUniqueId());

        // Send message
        String message = plugin.getConfig().getString("messages.daily-received",
                "&aVous avez reçu votre fiole quotidienne !");
        String prefix = plugin.getConfig().getString("messages.prefix", "&8[&6ElderFiole&8] &r");
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));

        return true;
    }
}
