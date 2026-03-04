package ar1hurgit.elderFiole;

import ar1hurgit.elderFiole.command.AmuletteCommand;
import ar1hurgit.elderFiole.command.ElderFioleCommand;
import ar1hurgit.elderFiole.command.FioleCommand;
import ar1hurgit.elderFiole.command.GiveFioleCommand;
import ar1hurgit.elderFiole.command.GivePerleCommand;
import ar1hurgit.elderFiole.database.DatabaseManager;
import ar1hurgit.elderFiole.listener.AmuletteEquipListener;
import ar1hurgit.elderFiole.listener.JobsExpBoostListener;
import ar1hurgit.elderFiole.listener.PlayerConnectionListener;
import ar1hurgit.elderFiole.listener.VialInteractListener;
import ar1hurgit.elderFiole.manager.BoostManager;
import ar1hurgit.elderFiole.manager.DailyCooldownManager;
import ar1hurgit.elderFiole.manager.JobBoostItemManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ElderFiole extends JavaPlugin {

    private DatabaseManager databaseManager;
    private BoostManager boostManager;
    private DailyCooldownManager cooldownManager;
    private JobBoostItemManager jobBoostItemManager;

    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();

        // Initialize database first
        this.databaseManager = new DatabaseManager(this);

        // Migrate from YAML to SQLite if needed
        migrateFromYamlToSqlite();

        // Initialize managers
        this.cooldownManager = new DailyCooldownManager(this);
        this.boostManager = new BoostManager(this);
        this.jobBoostItemManager = new JobBoostItemManager(this);

        // Load data
        this.boostManager.loadBoosts();
        this.jobBoostItemManager.loadItems();

        // Register commands
        getCommand("fiole").setExecutor(new FioleCommand(this));
        getCommand("dailyfiole").setExecutor(new ar1hurgit.elderFiole.command.DailyFioleCommand(this));
        getCommand("elderfiole").setExecutor(new ElderFioleCommand(this));
        getCommand("amulette").setExecutor(new AmuletteCommand(this));
        GiveFioleCommand giveFioleCommand = new GiveFioleCommand(this);
        getCommand("givefiole").setExecutor(giveFioleCommand);
        getCommand("givefiole").setTabCompleter(giveFioleCommand);
        GivePerleCommand givePerleCommand = new GivePerleCommand(this);
        getCommand("giveperle").setExecutor(givePerleCommand);
        getCommand("giveperle").setTabCompleter(givePerleCommand);

        // Register listeners
        getServer().getPluginManager().registerEvents(new VialInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new JobsExpBoostListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new AmuletteEquipListener(this), this);

        getLogger().info("ElderFiole enabled!");
    }

    @Override
    public void onDisable() {
        if (boostManager != null) {
            boostManager.saveBoosts();
            boostManager.shutdown();
        }

        if (jobBoostItemManager != null) {
            jobBoostItemManager.shutdown();
        }

        if (databaseManager != null) {
            databaseManager.close();
        }

        getLogger().info("ElderFiole disabled!");
    }

    private void migrateFromYamlToSqlite() {
        File dataFile = new File(getDataFolder(), "data.yml");

        if (dataFile.exists()) {
            getLogger().info("[ElderFiole] Migration des données YAML vers SQLite...");

            // Migrate daily cooldown data from YAML to SQLite
            migrateDailyCooldownsFromYaml(dataFile);

            try {
                if (dataFile.delete()) {
                    getLogger().info("[ElderFiole] Fichier data.yml supprimé après migration.");
                }
            } catch (Exception e) {
                getLogger().warning("[ElderFiole] Impossible de supprimer data.yml: " + e.getMessage());
            }
        }
    }

    private void migrateDailyCooldownsFromYaml(File dataFile) {
        try {
            FileConfiguration yamlConfig = YamlConfiguration.loadConfiguration(dataFile);
            String path = "daily-cooldown";

            if (!yamlConfig.contains(path)) {
                return;
            }

            Map<UUID, Long> cooldownData = new HashMap<>();
            for (String key : yamlConfig.getConfigurationSection(path).getKeys(false)) {
                try {
                    UUID playerId = UUID.fromString(key);
                    long lastUse = yamlConfig.getLong(path + "." + key);
                    cooldownData.put(playerId, lastUse);
                } catch (IllegalArgumentException e) {
                    getLogger().warning("[ElderFiole] UUID invalide dans data.yml: " + key);
                }
            }

            // Import cooldowns into database
            for (Map.Entry<UUID, Long> entry : cooldownData.entrySet()) {
                databaseManager.saveDailyCooldown(entry.getKey(), entry.getValue());
            }

            getLogger().info("[ElderFiole] " + cooldownData.size() + " cooldowns quotidiens migrés vers SQLite.");
        } catch (Exception e) {
            getLogger().warning("[ElderFiole] Erreur lors de la migration des cooldowns: " + e.getMessage());
        }
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public BoostManager getBoostManager() {
        return boostManager;
    }

    public DailyCooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public JobBoostItemManager getJobBoostItemManager() {
        return jobBoostItemManager;
    }
}
