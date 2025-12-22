package ar1hurgit.elderFiole.listener;

import ar1hurgit.elderFiole.ElderFiole;
import com.gamingmesh.jobs.api.JobsExpGainEvent;
import com.gamingmesh.jobs.container.Job;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class JobsExpBoostListener implements Listener {
    private final ElderFiole plugin;

    public JobsExpBoostListener(ElderFiole plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJobsExpGain(JobsExpGainEvent event) {
        // Get player - the event returns OfflinePlayer, we need to cast and check if
        // online
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        Job job = event.getJob();

        if (player == null || job == null) {
            return;
        }

        double totalMultiplier = plugin.getBoostManager().getTotalMultiplier(player, job);

        if (totalMultiplier > 1.0) {
            double originalExp = event.getExp();
            double boostedExp = originalExp * totalMultiplier;
            event.setExp(boostedExp);
        }
    }
}
