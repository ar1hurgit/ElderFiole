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

public class GiveFioleCommand implements CommandExecutor, TabCompleter {
    private final ElderFiole plugin;

    public GiveFioleCommand(ElderFiole plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("elderfiole.fiole.give")) {
            String message = plugin.getConfig().getString("messages.no-permission", "&cPas de permission.");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return true;
        }

        if (args.length < 4) {
            sender.sendMessage(
                    ChatColor.RED + "Utilisation: /givefiole <joueur> <métier> <multiplicateur> <durée> [quantité]");
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

        // 4. Durée
        int duration;
        try {
            duration = Integer.parseInt(args[3]);
            if (duration <= 0)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Durée invalide: " + args[3]);
            return true;
        }

        // 5. Quantité (optionnel)
        int amount = 1;
        if (args.length >= 5) {
            try {
                amount = Integer.parseInt(args[4]);
                if (amount < 1 || amount > 64)
                    throw new NumberFormatException();
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Quantité invalide: " + args[4]);
                return true;
            }
        }

        // Donner la fiole
        ItemStack vial = VialItem.createVial(plugin, job.getName(), multiplier, duration);
        vial.setAmount(amount);
        target.getInventory().addItem(vial);

        String prefix = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.prefix", "&8[&6ElderFiole&8] &r"));
        sender.sendMessage(prefix + ChatColor.GREEN + "Fiole donnée: " + amount + "x " + job.getName() + " ("
                + multiplier + "x, " + duration + "min) à " + target.getName());
        target.sendMessage(
                prefix + ChatColor.GREEN + "Vous avez reçu " + amount + "x Fiole d'XP " + job.getName() + " !");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (!sender.hasPermission("elderfiole.fiole.give"))
            return completions;

        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        } else if (args.length == 2) {
            for (Job job : Jobs.getJobs())
                completions.add(job.getName());
        } else if (args.length == 3) {
            completions.add("1.5");
            completions.add("2.0");
            completions.add("3.0");
        } else if (args.length == 4) {
            completions.add("30");
            completions.add("60");
            completions.add("120");
        } else if (args.length == 5) {
            completions.add("1");
            completions.add("5");
            completions.add("10");
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}
