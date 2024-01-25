package me.kicksquare.blskyblockutils.tutorial;

import com.leonardobishop.quests.bukkit.tasktype.BukkitTaskType;
import com.leonardobishop.quests.bukkit.util.TaskUtils;
import com.leonardobishop.quests.common.player.QPlayer;
import com.leonardobishop.quests.common.player.questprogressfile.TaskProgress;
import com.leonardobishop.quests.common.quest.Quest;
import com.leonardobishop.quests.common.quest.Task;
import me.angeschossen.lands.api.events.ChunkPostClaimEvent;
import me.kicksquare.blskyblockutils.BLSkyblockUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class ClaimChunksTaskType extends BukkitTaskType {
    private BLSkyblockUtils plugin;

    public ClaimChunksTaskType(BLSkyblockUtils blSkyblockUtils) {
        super("claimchunks", "kicksquare", "Makes the user claim X Lands chunks");

        super.addConfigValidator(TaskUtils.useRequiredConfigValidator(this, "amount"));
        super.addConfigValidator(TaskUtils.useIntegerConfigValidator(this, "amount"));
        this.plugin = blSkyblockUtils;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void chunkClaimed(ChunkPostClaimEvent event) {
        Player player = event.getLandPlayer().getPlayer();

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
