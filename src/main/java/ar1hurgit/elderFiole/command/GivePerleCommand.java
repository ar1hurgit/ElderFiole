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

public class GivePerleCommand implements CommandExecutor, TabCompleter {
    private final ElderFiole plugin;

    public GivePerleCommand(ElderFiole plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("elderfiole.give")) {
            String message = plugin.getConfig().getString("messages.no-permission", "&cPas de permission.");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Utilisation: /giveperle <joueur> <métier> <multiplicateur> [durée]");
            return true;
        }

        // 1. Joueur
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Joueur introuvable: " + args[0]);
            return true;
        }

        // 2. Métier
        String jobName = args[1];
        Job job = Jobs.getJob(jobName);
        if (job == null) {
            sender.sendMessage(ChatColor.RED + "Métier invalide: " + jobName);
            return true;
        }

        // 3. Multiplicateur
        double multiplier;
        try {
            multiplier = Double.parseDouble(args[2].replace("x", ""));
            if (multiplier <= 0)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Multiplicateur invalide: " + args[2]);
            return true;
        }

        // 4. Durée (optionnel, utilise la durée par défaut de la config si non spécifiée)
        int durationMinutes = 0; // 0 = utiliser la durée par défaut de la config
        if (args.length >= 4) {
            try {
                durationMinutes = Integer.parseInt(args[3]);
                if (durationMinutes < 0)
                    throw new NumberFormatException();
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Durée invalide: " + args[3]);
                return true;
            }
        }

        // Donner l'amulette (perle) avec le nouveau système
        plugin.getJobBoostItemManager().giveJobBoostItem(target, job.getName(), multiplier, durationMinutes);

        String prefix = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.prefix", "&8[&6ElderFiole&8] &r"));
        int actualDuration = durationMinutes > 0 ? durationMinutes : plugin.getConfig().getInt("daily-vial.duration", 120);
        String durationType = "§b" + actualDuration + " minutes";
        
        sender.sendMessage(prefix + ChatColor.GREEN + "Amulette de §e" + job.getName() + " §a(§6" + multiplier + "x§a, " + durationType
                + "§a) donnée à §e" + target.getName() + " §a!");
        target.sendMessage(prefix + ChatColor.GREEN + "Vous avez reçu une amulette de §e" + job.getName() + " §a(§6" + multiplier + "x§a, " + durationType + "§a) !");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (!sender.hasPermission("elderfiole.give"))
            return completions;

        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        } else if (args.length == 2) {
            for (Job job : Jobs.getJobs())
                completions.add(job.getName());
        } else if (args.length == 3) {
            completions.add("1.5");
            completions.add("2.0");
            completions.add("2.5");
            completions.add("3.0");
        } else if (args.length == 4) {
            completions.add("30");
            completions.add("60");
            completions.add("120");
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}
