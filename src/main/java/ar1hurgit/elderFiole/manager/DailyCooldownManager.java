package ar1hurgit.elderFiole.manager;

import ar1hurgit.elderFiole.ElderFiole;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DailyCooldownManager {
    private final ElderFiole plugin;
    private File dataFile;
    private FileConfiguration dataConfig;

    public DailyCooldownManager(ElderFiole plugin) {
        this.plugin = plugin;
        loadData();
    }

    private void loadData() {
        dataFile = new File(plugin.getDataFolder(), "data.yml");

        if (!dataFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Could not create data.yml: " + e.getMessage());
            }
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    private long getCooldownDuration() {
        int hours = plugin.getConfig().getInt("daily-vial.cooldown", 24);
        return TimeUnit.HOURS.toMillis(hours);
    }

    public FileConfiguration getDataConfig() {
        return dataConfig;
    }

    public void saveData() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save data.yml: " + e.getMessage());
        }
    }

    public boolean canUseDaily(UUID playerId) {
        String path = "daily-cooldown." + playerId.toString();

        if (!dataConfig.contains(path)) {
            return true;
        }

        long lastUse = dataConfig.getLong(path);
        long currentTime = System.currentTimeMillis();

        return (currentTime - lastUse) >= getCooldownDuration();
    }

    public void setLastUse(UUID playerId) {
        String path = "daily-cooldown." + playerId.toString();
        dataConfig.set(path, System.currentTimeMillis());
        saveData();
    }

    public String getTimeRemaining(UUID playerId) {
        String path = "daily-cooldown." + playerId.toString();

        if (!dataConfig.contains(path)) {
            return "0h 0m";
        }

        long lastUse = dataConfig.getLong(path);
        long currentTime = System.currentTimeMillis();
        long remainingTime = getCooldownDuration() - (currentTime - lastUse);

        if (remainingTime <= 0) {
            return "0h 0m";
        }

        long hours = TimeUnit.MILLISECONDS.toHours(remainingTime);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(remainingTime) % 60;

        return hours + "h " + minutes + "m";
    }
}
