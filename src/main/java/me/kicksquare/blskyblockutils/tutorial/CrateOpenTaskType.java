package me.kicksquare.blskyblockutils.tutorial;

import com.leonardobishop.quests.bukkit.tasktype.BukkitTaskType;
import com.leonardobishop.quests.bukkit.util.TaskUtils;
import com.leonardobishop.quests.common.player.QPlayer;
import com.leonardobishop.quests.common.player.questprogressfile.TaskProgress;
import com.leonardobishop.quests.common.quest.Quest;
import com.leonardobishop.quests.common.quest.Task;
import me.PM2.customcrates.api.CrateOpenEvent;
import me.kicksquare.blskyblockutils.BLSkyblockUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import java.lang.Record;

public class CrateOpenTaskType extends BukkitTaskType {
    private BLSkyblockUtils plugin;

    public CrateOpenTaskType(BLSkyblockUtils blSkyblockUtils) {
        super("opencrates", "kicksquare", "open a specialized crate key");

        super.addConfigValidator(TaskUtils.useRequiredConfigValidator(this, "amount"));
        super.addConfigValidator(TaskUtils.useIntegerConfigValidator(this, "amount"));
        this.plugin = blSkyblockUtils;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCrateOpen(CrateOpenEvent event) {
        Player player = event.getPlayer();

        QPlayer qPlayer = this.plugin.getQuestAPI().getPlayerManager().getPlayer(player.getUniqueId());
        if (qPlayer == null) {
            return;
        }

        for (TaskUtils.PendingTask pendingTask : TaskUtils.getApplicableTasks(player, qPlayer, this)) {
            Quest quest = pendingTask.quest();
            Task task = pendingTask.task();
            TaskProgress taskProgress = pendingTask.taskProgress();

            super.debug("Player opened a crate key", quest.getId(), task.getId(), player.getUniqueId());

            int numberOfNeededCrateKeyOpens = (int) task.getConfigValue("amount");

            int progress = TaskUtils.incrementIntegerTaskProgress(taskProgress);
            super.debug("Incrementing task progress (now " + progress + ")", quest.getId(), task.getId(), player.getUniqueId());
            System.out.println("Incrementing task progress (now " + progress + ")");

            if (progress >= numberOfNeededCrateKeyOpens) {
                super.debug("Marking task as complete", quest.getId(), task.getId(), player.getUniqueId());
                System.out.println("Marking task as complete");
                taskProgress.setCompleted(true);
            }

//            TaskUtils.sendTrackAdvancement(player, quest, task, taskProgress, numberOfNeededCrateKeyOpens);
        }
    }
}
