package ar1hurgit.elderFiole;

import ar1hurgit.elderFiole.command.FioleCommand;
import ar1hurgit.elderFiole.listener.JobsExpBoostListener;
import ar1hurgit.elderFiole.listener.VialInteractListener;
import ar1hurgit.elderFiole.manager.BoostManager;
import ar1hurgit.elderFiole.manager.DailyCooldownManager;
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
        getCommand("fiole").setExecutor(new FioleCommand(this));
        getCommand("dailyfiole").setExecutor(new ar1hurgit.elderFiole.command.DailyFioleCommand(this));
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new VialInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new JobsExpBoostListener(this), this);

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
}
