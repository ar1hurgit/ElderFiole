package ar1hurgit.elderFiole.command;

import ar1hurgit.elderFiole.ElderFiole;
import ar1hurgit.elderFiole.manager.JobBoostItemManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AmuletteCommand implements CommandExecutor {
    private final ElderFiole plugin;

    public AmuletteCommand(ElderFiole plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cCette commande ne peut être exécutée que par un joueur.");
            return true;
        }

        if (args.length == 0 || !args[0].equalsIgnoreCase("toggle")) {
            player.sendMessage(plugin.getConfig().getString("messages.prefix") + "§cUtilisation: /amulette toggle");
            return true;
        }

        // Vérifier l'item en main secondaire
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (offHand == null || !offHand.hasItemMeta()) {
            player.sendMessage(plugin.getConfig().getString("messages.prefix") +
                    "§cVous devez tenir une amulette de métier en main secondaire !");
            return true;
        }

        // Récupérer les données de l'amulette
        JobBoostItemManager.AmuletteData data = plugin.getJobBoostItemManager().getAmuletteDataFromStack(offHand);
        if (data == null) {
            player.sendMessage(plugin.getConfig().getString("messages.prefix") +
                    "§cCet item n'est pas une amulette de métier !");
            return true;
        }

        // Toggle l'amulette directement dans l'item
        plugin.getJobBoostItemManager().toggleAmulette(player, offHand);

        // Récupérer les nouvelles données après le toggle
        JobBoostItemManager.AmuletteData newData = plugin.getJobBoostItemManager().getAmuletteDataFromStack(offHand);
        String status = newData.active ? "§a§lACTIVÉ" : "§c§lDÉSACTIVÉ";
        player.sendMessage(plugin.getConfig().getString("messages.prefix") +
                "§eAmulette de §6" + data.jobName + " §e: " + status);

        return true;
    }
}
