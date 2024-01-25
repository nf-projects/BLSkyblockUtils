package me.kicksquare.blskyblockutils.tutorial;

import com.bgsoftware.superiorskyblock.api.events.IslandCreateEvent;
import com.leonardobishop.quests.bukkit.tasktype.BukkitTaskType;
import com.leonardobishop.quests.bukkit.util.TaskUtils;
import com.leonardobishop.quests.common.player.QPlayer;
import com.leonardobishop.quests.common.player.questprogressfile.TaskProgress;
import com.leonardobishop.quests.common.quest.Quest;
import com.leonardobishop.quests.common.quest.Task;
import me.kicksquare.blskyblockutils.BLSkyblockUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class IslandCreateTaskType extends BukkitTaskType {
    private BLSkyblockUtils plugin;

    public IslandCreateTaskType(BLSkyblockUtils blSkyblockUtils) {
        super("islandcreate", "kicksquare", "create a SuperiorSkyblock Island");

        super.addConfigValidator(TaskUtils.useRequiredConfigValidator(this, "amount"));
        super.addConfigValidator(TaskUtils.useIntegerConfigValidator(this, "amount"));
        this.plugin = blSkyblockUtils;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void islandCreated(IslandCreateEvent event) {
        Player player = event.getPlayer().asPlayer();

        QPlayer qPlayer = this.plugin.getQuestAPI().getPlayerManager().getPlayer(player.getUniqueId());
        if (qPlayer == null) {
            return;
        }

        for (TaskUtils.PendingTask pendingTask : TaskUtils.getApplicableTasks(player, qPlayer, this)) {
            Quest quest = pendingTask.quest();
            Task task = pendingTask.task();
            TaskProgress taskProgress = pendingTask.taskProgress();

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
