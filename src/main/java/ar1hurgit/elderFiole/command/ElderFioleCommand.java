package ar1hurgit.elderFiole.command;

import ar1hurgit.elderFiole.ElderFiole;
import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.container.Job;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class    ElderFioleCommand implements CommandExecutor, TabCompleter {
    private final ElderFiole plugin;

    public ElderFioleCommand(ElderFiole plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = plugin.getConfig().getString("messages.prefix", "§8[§6ElderFiole§8] §r");

        // /elderfiole give [métier] [multiplicateur] [temps optionnel]
        if (args.length >= 3 && args[0].equalsIgnoreCase("give")) {
            if (!sender.hasPermission("elderfiole.give")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        prefix + plugin.getConfig().getString("messages.no-permission", "§cPas de permission.")));
                return true;
            }

            // Récupérer le joueur cible
            Player target;
            if (args.length >= 4) {
                target = Bukkit.getPlayer(args[3]);
                if (target == null) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            prefix + "§cJoueur introuvable: " + args[3]));
                    return true;
                }
            } else if (sender instanceof Player) {
                target = (Player) sender;
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        prefix + "§cVous devez spécifier un joueur depuis la console."));
                return true;
            }

            // Valider le métier
            String jobName = args[1];
            Job job = Jobs.getJob(jobName);
            if (job == null) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        prefix + "§cMétier invalide: " + jobName));
                return true;
            }

            // Parser le multiplicateur
            double multiplier;
            try {
                String multStr = args[2].replace("x", "");
                multiplier = Double.parseDouble(multStr);
                if (multiplier <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        prefix + "§cMultiplicateur invalide: " + args[2]));
                return true;
            }

            // Parser le temps (optionnel, utilise la durée par défaut de la config si non spécifiée)
            int durationMinutes = 0; // 0 = utiliser la durée par défaut de la config
            if (args.length >= 5) {
                try {
                    durationMinutes = Integer.parseInt(args[4]);
                    if (durationMinutes < 0) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            prefix + "§cDurée invalide: " + args[4]));
                    return true;
                }
            }

            // Donner l'item
            plugin.getJobBoostItemManager().giveJobBoostItem(target, job.getName(), multiplier, durationMinutes);

            int actualDuration = durationMinutes > 0 ? durationMinutes : plugin.getConfig().getInt("daily-vial.duration", 120);
            String durationType = "§b" + actualDuration + " minutes";
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    prefix + "§aAmulette de §e" + job.getName() + " §a(§6" + multiplier + "x§a, " + durationType
                            + "§a) donnée à §e" + target.getName() + " §a!"));

            target.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    prefix + "§aVous avez reçu une amulette de §e" + job.getName() + " §a(§6" + multiplier + "x§a, " + durationType + "§a) !"));

            return true;
        }

        // Usage
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                prefix + "§cUtilisation: /elderfiole give <métier> <multiplicateur> [joueur] [temps en minutes]"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                "§7Exemple: §e/elderfiole give Miner 1.5x"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                "§7Exemple: §e/elderfiole give Miner 2.0x Player123 60"));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!sender.hasPermission("elderfiole.give")) {
            return completions;
        }

        if (args.length == 1) {
            completions.add("give");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            // Métiers
            for (Job job : Jobs.getJobs()) {
                completions.add(job.getName());
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            // Multiplicateurs
            completions.add("1.5x");
            completions.add("2.0x");
            completions.add("2.5x");
            completions.add("3.0x");
        } else if (args.length == 4 && args[0].equalsIgnoreCase("give")) {
            // Joueurs
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
        } else if (args.length == 5 && args[0].equalsIgnoreCase("give")) {
            // Temps
            completions.add("30");
            completions.add("60");
            completions.add("120");
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}
