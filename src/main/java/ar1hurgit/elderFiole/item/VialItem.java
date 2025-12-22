package ar1hurgit.elderFiole.item;

import ar1hurgit.elderFiole.ElderFiole;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class VialItem {
    private static final String VIAL_KEY = "elderfiole_vial";
    private static final String JOB_KEY = "vial_job";
    private static final String MULTIPLIER_KEY = "vial_multiplier";
    private static final String DURATION_KEY = "vial_duration";

    public static ItemStack createVial(ElderFiole plugin, String jobName, double multiplier, int durationMinutes) {
        ItemStack vial = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta meta = vial.getItemMeta();

        if (meta == null) {
            return vial;
        }

        // Set display name
        String name = plugin.getConfig().getString("messages.vial-name", "&6&lFiole d'XP Métier");
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

        // Create lore
        List<String> loreTemplate = plugin.getConfig().getStringList("messages.vial-lore");
        List<String> lore = new ArrayList<>();

        for (String line : loreTemplate) {
            line = line.replace("{job}", jobName)
                    .replace("{multiplier}", String.format("%.2f", multiplier))
                    .replace("{duration}", String.valueOf(durationMinutes));
            lore.add(ChatColor.translateAlternateColorCodes('&', line));
        }

        meta.setLore(lore);

        // Add enchantment glow
        if (plugin.getConfig().getBoolean("item.glow", true)) {
            meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        // Store data in PersistentDataContainer
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey vialKey = new NamespacedKey(plugin, VIAL_KEY);
        NamespacedKey jobKey = new NamespacedKey(plugin, JOB_KEY);
        NamespacedKey multiplierKey = new NamespacedKey(plugin, MULTIPLIER_KEY);
        NamespacedKey durationKey = new NamespacedKey(plugin, DURATION_KEY);

        container.set(vialKey, PersistentDataType.STRING, "true");
        container.set(jobKey, PersistentDataType.STRING, jobName);
        container.set(multiplierKey, PersistentDataType.DOUBLE, multiplier);
        container.set(durationKey, PersistentDataType.INTEGER, durationMinutes);

        vial.setItemMeta(meta);
        return vial;
    }

    public static boolean isVial(ElderFiole plugin, ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey vialKey = new NamespacedKey(plugin, VIAL_KEY);

        return container.has(vialKey, PersistentDataType.STRING);
    }

    public static String getJobName(ElderFiole plugin, ItemStack item) {
        if (!isVial(plugin, item)) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey jobKey = new NamespacedKey(plugin, JOB_KEY);

        return container.get(jobKey, PersistentDataType.STRING);
    }

    public static Double getMultiplier(ElderFiole plugin, ItemStack item) {
        if (!isVial(plugin, item)) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey multiplierKey = new NamespacedKey(plugin, MULTIPLIER_KEY);

        return container.get(multiplierKey, PersistentDataType.DOUBLE);
    }

    public static Integer getDuration(ElderFiole plugin, ItemStack item) {
        if (!isVial(plugin, item)) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey durationKey = new NamespacedKey(plugin, DURATION_KEY);

        return container.get(durationKey, PersistentDataType.INTEGER);
    }
}
