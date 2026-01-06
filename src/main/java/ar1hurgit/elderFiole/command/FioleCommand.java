package ar1hurgit.elderFiole.command;

import ar1hurgit.elderFiole.ElderFiole;
import ar1hurgit.elderFiole.item.VialItem;
import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.container.Job;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FioleCommand implements CommandExecutor, TabCompleter {
    private final ElderFiole plugin;

    public FioleCommand(ElderFiole plugin) {
        this.plugin = plugin;
    }

        return true;
    }

    private void handleTimeLeft(Player player) {
        List<ar1hurgit.elderFiole.data.ActiveBoost> boosts = plugin.getBoostManager().getPlayerBoosts(player);
        
        // Filter out expired one just in case cleanup hasn't run yet
        List<ar1hurgit.elderFiole.data.ActiveBoost> active = boosts.stream()
                .filter(b -> !b.isExpired())
                .collect(Collectors.toList());

        String prefix = plugin.getConfig().getString("messages.prefix", "&8[&6ElderFiole&8] &r");

        if (active.isEmpty()) {
            String message = plugin.getConfig().getString("messages.no-active-boosts", "&cVous n'avez aucun boost actif.");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));
            return;
        }

        String header = plugin.getConfig().getString("messages.boost-time-header", "&eVos boosts actifs:");
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + header));

        String format = plugin.getConfig().getString("messages.boost-time-format", "&7- &6{job} &7: &e{time}");

        for (ar1hurgit.elderFiole.data.ActiveBoost boost : active) {
            long remaining = boost.getExpirationTime() - System.currentTimeMillis();
            long minutes = remaining / 60000;
            long seconds = (remaining % 60000) / 1000;

            String timeStr = String.format("%02dm %02ds", minutes, seconds);
            String line = format.replace("{job}", boost.getJobName())
                                .replace("{time}", timeStr)
                                .replace("{multiplier}", String.valueOf(boost.getMultiplier()));
            
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("elderfiole.reload")) {
                String message = plugin.getConfig().getString("messages.no-permission", "&cPas de permission.");
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                return true;
            }

            plugin.reloadConfig();
            
            // Reload boosts just in case of weird state, though not strictly necessary for config reload
            // plugin.getBoostManager().loadBoosts(); 

            String message = plugin.getConfig().getString("messages.reload-success",
                    "&aConfiguration rechargée !");
            String prefix = plugin.getConfig().getString("messages.prefix", "&8[&6ElderFiole&8] &r");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("timeleft")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Seul un joueur peut utiliser cette commande.");
                return true;
            }
            if (!sender.hasPermission("elderfiole.fiole.timeleft")) { // New permission
                 String message = plugin.getConfig().getString("messages.no-permission", "&cPas de permission.");
                 sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                 return true;
            }
            handleTimeLeft((Player) sender);
            return true;
        }

        if (!sender.hasPermission("elderfiole.fiole.give")) {
            String message = plugin.getConfig().getString("messages.no-permission", "&cPas de permission.");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return true;
        }

        if (args.length != 6 || !args[0].equalsIgnoreCase("give")) {
            String usage = plugin.getConfig().getString("messages.usage-fiole",
                    "&cUtilisation: /fiole <give|reload|timeleft> ...");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', usage));
            return true;
        }

        // Parse multiplier
        double multiplier;
        try {
            multiplier = Double.parseDouble(args[1]);
            if (multiplier <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            String message = plugin.getConfig().getString("messages.invalid-multiplier",
                    "&cMultiplicateur invalide.");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return true;
        }

        // Validate job
        String jobName = args[2];
        Job job = Jobs.getJob(jobName);
        if (job == null) {
            String message = plugin.getConfig().getString("messages.invalid-job", "&cMétier invalide.");
            message = message.replace("{job}", jobName);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return true;
        }

        // Parse duration
        int duration;
        try {
            duration = Integer.parseInt(args[3]);
            if (duration <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            String message = plugin.getConfig().getString("messages.invalid-duration",
                    "&cDurée invalide.");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return true;
        }

        // Get target player
        Player target = Bukkit.getPlayer(args[4]);
        if (target == null) {
            String message = plugin.getConfig().getString("messages.invalid-player",
                    "&cJoueur introuvable.");
            message = message.replace("{player}", args[4]);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return true;
        }

        // Parse amount
        int amount;
        try {
            amount = Integer.parseInt(args[5]);
            if (amount < 1 || amount > 64) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            String message = plugin.getConfig().getString("messages.invalid-number",
                    "&cNombre invalide.");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return true;
        }

        // Create and give vials
        ItemStack vial = VialItem.createVial(plugin, job.getName(), multiplier, duration);
        vial.setAmount(amount);
        target.getInventory().addItem(vial);

        // Send messages
        String targetMessage = plugin.getConfig().getString("messages.vial-received", "&aFiole reçue !");
        targetMessage = targetMessage.replace("{job}", job.getName());
        String prefix = plugin.getConfig().getString("messages.prefix", "&8[&6ElderFiole&8] &r");
        target.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + targetMessage));

        String senderMessage = plugin.getConfig().getString("messages.vial-given", "&aFiole donnée !");
        senderMessage = senderMessage.replace("{amount}", String.valueOf(amount))
                .replace("{player}", target.getName());
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + senderMessage));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission("elderfiole.fiole.give"))
                completions.add("give");
            if (sender.hasPermission("elderfiole.reload"))
                completions.add("reload");
            if (sender.hasPermission("elderfiole.fiole.timeleft"))
                completions.add("timeleft");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("give")) {
                completions.add("1.5");
                completions.add("2.0");
                completions.add("3.0");
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            for (Job job : Jobs.getJobs()) {
                completions.add(job.getName());
            }
        } else if (args.length == 4 && args[0].equalsIgnoreCase("give")) {
            completions.add("30");
            completions.add("60");
            completions.add("120");
        } else if (args.length == 5 && args[0].equalsIgnoreCase("give")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
        } else if (args.length == 6 && args[0].equalsIgnoreCase("give")) {
            completions.add("1");
            completions.add("5");
            completions.add("10");
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}
