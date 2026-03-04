package ar1hurgit.elderFiole.manager;

import ar1hurgit.elderFiole.ElderFiole;
import com.gamingmesh.jobs.container.Job;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class JobBoostItemManager {
    private final ElderFiole plugin;
    private final NamespacedKey itemIdKey;
    private final NamespacedKey jobNameKey;
    private final NamespacedKey multiplierKey;
    private final NamespacedKey durationKey;
    private final NamespacedKey remainingTimeKey;
    private final NamespacedKey activeKey;

    // Stocke les IDs des amulettes actives par joueur
    private final Map<UUID, Set<String>> activeAmulettes = new ConcurrentHashMap<>();

    public JobBoostItemManager(ElderFiole plugin) {
        this.plugin = plugin;
        this.itemIdKey = new NamespacedKey(plugin, "amulette_id");
        this.jobNameKey = new NamespacedKey(plugin, "job_name");
        this.multiplierKey = new NamespacedKey(plugin, "multiplier");
        this.durationKey = new NamespacedKey(plugin, "duration");
        this.remainingTimeKey = new NamespacedKey(plugin, "remaining_time");
        this.activeKey = new NamespacedKey(plugin, "active");
        startTimerTask();
    }

    public void giveJobBoostItem(Player player, String jobName, double multiplier, int durationMinutes) {
        if (durationMinutes <= 0) {
            durationMinutes = plugin.getConfig().getInt("daily-vial.duration", 120);
        }

        // Créer l'item avec un ID unique
        String uniqueId = UUID.randomUUID().toString();
        ItemStack itemStack = createJobBoostItemStack(uniqueId, jobName, multiplier, durationMinutes, false);
        player.getInventory().addItem(itemStack);
    }

    public ItemStack createJobBoostItemStack(String uniqueId, String jobName, double multiplier, int durationMinutes, boolean active) {
        String materialName = plugin.getConfig().getString("job-boost-items." + jobName + ".material", "GHAST_TEAR");
        Material material;
        try {
            material = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            material = Material.GHAST_TEAR;
        }

        ItemStack itemStack = new ItemStack(material);
        ItemMeta meta = itemStack.getItemMeta();

        if (meta != null) {
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(itemIdKey, PersistentDataType.STRING, uniqueId);
            container.set(jobNameKey, PersistentDataType.STRING, jobName);
            container.set(multiplierKey, PersistentDataType.DOUBLE, multiplier);
            container.set(durationKey, PersistentDataType.INTEGER, durationMinutes);
            container.set(remainingTimeKey, PersistentDataType.INTEGER, durationMinutes * 60);
            container.set(activeKey, PersistentDataType.BYTE, (byte) (active ? 1 : 0));

            String displayName = plugin.getConfig().getString("job-boost-items." + jobName + ".name",
                    "§6§lPerle de " + jobName);
            meta.setDisplayName(displayName);

            int remainingSeconds = durationMinutes * 60;
            List<String> lore = new ArrayList<>();
            lore.add("§7Métier: §e" + jobName);
            lore.add("§7Multiplicateur: §a" + multiplier + "x");
            lore.add("§7Durée: §b" + durationMinutes + " minutes");
            lore.add("§7Temps restant: §e" + formatTime(remainingSeconds));
            lore.add("");
            lore.add(active ? "§a✓ Activé" : "§c✗ Désactivé");
            lore.add("");
            lore.add("§eMaintenez en main secondaire pour activer");
            lore.add("§e/amulette toggle pour activer/désactiver");

            meta.setLore(lore);

            int customModelData = plugin.getConfig().getInt("job-boost-items." + jobName + ".custom-model-data", 0);
            if (customModelData > 0) {
                meta.setCustomModelData(customModelData);
            }

            if (plugin.getConfig().getBoolean("job-boost-items." + jobName + ".glow", true)) {
                meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            }

            itemStack.setItemMeta(meta);
        }

        return itemStack;
    }

    private String formatTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;

        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, secs);
        } else {
            return String.format("%ds", secs);
        }
    }

    /**
     * Données d'une amulette extraite d'un ItemStack
     */
    public static class AmuletteData {
        public final String id;
        public final String jobName;
        public final double multiplier;
        public final int durationMinutes;
        public final int remainingTime;
        public final boolean active;

        public AmuletteData(String id, String jobName, double multiplier, int durationMinutes, int remainingTime, boolean active) {
            this.id = id;
            this.jobName = jobName;
            this.multiplier = multiplier;
            this.durationMinutes = durationMinutes;
            this.remainingTime = remainingTime;
            this.active = active;
        }
    }

    public AmuletteData getAmuletteDataFromStack(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return null;
        }

        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return null;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();

        String id = container.get(itemIdKey, PersistentDataType.STRING);
        String jobName = container.get(jobNameKey, PersistentDataType.STRING);
        Double multiplier = container.get(multiplierKey, PersistentDataType.DOUBLE);
        Integer duration = container.get(durationKey, PersistentDataType.INTEGER);
        Integer remainingTime = container.get(remainingTimeKey, PersistentDataType.INTEGER);
        Byte activeByte = container.get(activeKey, PersistentDataType.BYTE);

        if (id == null || jobName == null || multiplier == null) {
            return null;
        }

        return new AmuletteData(
            id,
            jobName,
            multiplier,
            duration != null ? duration : 0,
            remainingTime != null ? remainingTime : 0,
            activeByte != null && activeByte == 1
        );
    }

    /**
     * Active ou désactive une amulette spécifique
     */
    public void toggleAmulette(Player player, ItemStack itemStack) {
        AmuletteData data = getAmuletteDataFromStack(itemStack);
        if (data == null) {
            return;
        }

        boolean newActive = !data.active;
        updateAmuletteState(itemStack, data, newActive);

        // Mettre à jour la liste des amulettes actives
        UUID playerId = player.getUniqueId();
        if (newActive) {
            activeAmulettes.computeIfAbsent(playerId, k -> ConcurrentHashMap.newKeySet()).add(data.id);
        } else {
            Set<String> active = activeAmulettes.get(playerId);
            if (active != null) {
                active.remove(data.id);
            }
        }
    }

    /**
     * Met à jour l'état d'une amulette dans l'ItemStack
     */
    private void updateAmuletteState(ItemStack itemStack, AmuletteData data, boolean active) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(activeKey, PersistentDataType.BYTE, (byte) (active ? 1 : 0));

        // Mettre à jour le lore
        List<String> lore = new ArrayList<>();
        lore.add("§7Métier: §e" + data.jobName);
        lore.add("§7Multiplicateur: §a" + data.multiplier + "x");
        lore.add("§7Durée: §b" + data.durationMinutes + " minutes");
        lore.add("§7Temps restant: §e" + formatTime(data.remainingTime));
        lore.add("");
        lore.add(active ? "§a✓ Activé" : "§c✗ Désactivé");
        lore.add("");
        lore.add("§eMaintenez en main secondaire pour activer");
        lore.add("§e/amulette toggle pour activer/désactiver");

        meta.setLore(lore);
        itemStack.setItemMeta(meta);
    }

    /**
     * Met à jour le temps restant d'une amulette dans l'ItemStack
     */
    private void updateAmuletteTime(ItemStack itemStack, AmuletteData data, int newRemainingTime) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(remainingTimeKey, PersistentDataType.INTEGER, newRemainingTime);

        // Mettre à jour le lore
        List<String> lore = new ArrayList<>();
        lore.add("§7Métier: §e" + data.jobName);
        lore.add("§7Multiplicateur: §a" + data.multiplier + "x");
        lore.add("§7Durée: §b" + data.durationMinutes + " minutes");
        lore.add("§7Temps restant: §e" + formatTime(newRemainingTime));
        lore.add("");
        lore.add(data.active ? "§a✓ Activé" : "§c✗ Désactivé");
        lore.add("");
        lore.add("§eMaintenez en main secondaire pour activer");
        lore.add("§e/amulette toggle pour activer/désactiver");

        meta.setLore(lore);
        itemStack.setItemMeta(meta);
    }

    /**
     * Désactive une amulette par son ID
     */
    public void deactivateAmuletteById(Player player, String amuletteId) {
        // Chercher l'item dans l'inventaire du joueur
        for (ItemStack stack : player.getInventory().getContents()) {
            AmuletteData data = getAmuletteDataFromStack(stack);
            if (data != null && data.id.equals(amuletteId)) {
                updateAmuletteState(stack, data, false);
                Set<String> active = activeAmulettes.get(player.getUniqueId());
                if (active != null) {
                    active.remove(amuletteId);
                }
                return;
            }
        }
    }

    public double getJobBoostMultiplier(Player player, Job job) {
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (offHand == null || !offHand.hasItemMeta()) {
            return 1.0;
        }

        AmuletteData data = getAmuletteDataFromStack(offHand);
        if (data == null || !data.active || data.remainingTime <= 0) {
            return 1.0;
        }

        if (data.jobName.equalsIgnoreCase(job.getName())) {
            return data.multiplier;
        }

        return 1.0;
    }

    public AmuletteData getOffHandAmuletteData(Player player) {
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (offHand == null || !offHand.hasItemMeta()) {
            return null;
        }
        return getAmuletteDataFromStack(offHand);
    }

    private void startTimerTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                processAmulettes(player);
            }
        }, 20L, 20L);
    }

    private void processAmulettes(Player player) {
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (offHand == null || !offHand.hasItemMeta()) {
            return;
        }

        AmuletteData data = getAmuletteDataFromStack(offHand);
        if (data == null || !data.active) {
            return;
        }

        int newRemainingTime = data.remainingTime - 1;
        
        if (newRemainingTime <= 0) {
            // Amulette expirée
            player.getInventory().setItemInOffHand(null);
            player.sendMessage(plugin.getConfig().getString("messages.prefix") +
                    "§cVotre amulette de §e" + data.jobName + " §ca expiré !");
            return;
        }

        // Mettre à jour le temps restant
        updateAmuletteTime(offHand, data, newRemainingTime);
    }

    public void loadItems() {
        // Les amulettes sont maintenant stockées dans les items eux-mêmes
        // Pas besoin de charger depuis la BDD
        plugin.getLogger().info("[ElderFiole] Système d'amulettes chargé (données dans items).");
    }

    public void saveAllItems() {
        // Les données sont sauvegardées dans les items
    }

    public void shutdown() {
        activeAmulettes.clear();
    }
}
