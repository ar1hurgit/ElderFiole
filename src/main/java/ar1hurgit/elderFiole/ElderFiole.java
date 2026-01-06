package ar1hurgit.elderFiole;

import org.bukkit.plugin.java.JavaPlugin;

public final class ElderFiole extends JavaPlugin {

    private BoostManager boostManager;
    private DailyCooldownManager cooldownManager;

    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();

        // Initialize managers
        this.cooldownManager = new DailyCooldownManager(this);
        this.boostManager = new BoostManager(this);

        // Load data
        this.boostManager.loadBoosts();

        // Register commands
        getCommand("fiole").setExecutor(new ar1hurgit.elderFiole.command.FioleCommand(this));
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new ar1hurgit.elderFiole.listener.VialInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new ar1hurgit.elderFiole.listener.JobsExpBoostListener(this), this);

        getLogger().info("ElderFiole enabled!");
    }

    @Override
    public void onDisable() {
        if (boostManager != null) {
            boostManager.saveBoosts();
            boostManager.shutdown();
        }
        getLogger().info("ElderFiole disabled!");
    }

    public BoostManager getBoostManager() {
        return boostManager;
    }

    public DailyCooldownManager getCooldownManager() {
        return cooldownManager;
    }
