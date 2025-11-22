package it.crescentsun.api.crescentcore.util;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AdvancementUtil {

    /**
     * Awards advancement criteria to a player.
     *
     * @param target The player to award the advancement criteria to.
     * @param advancementNamespacedKey The namespaced key of the advancement.
     * @param criteria The criteria to award.
     *
     * @return True if any criteria were awarded, false otherwise.
     */
    public static boolean awardAdvancementCriteria(@NotNull Player target, @NotNull String advancementNamespacedKey, @NotNull String ...criteria) {
        // Grant advancement if applicable
        NamespacedKey key = NamespacedKey.fromString(advancementNamespacedKey);
        if (key == null) {
            return false;
        }
        Advancement advancement = Bukkit.getAdvancement(key);
        if (advancement == null) {
            return false;
        }
        if (target.getAdvancementProgress(advancement).isDone()) {
            return false;
        }
        boolean wasAnyAwarded = false;
        for (String criterion : criteria) {
            if (target.getAdvancementProgress(advancement).awardCriteria(criterion)) {
                wasAnyAwarded = true;
            }
        }
        return wasAnyAwarded;
    }

}
