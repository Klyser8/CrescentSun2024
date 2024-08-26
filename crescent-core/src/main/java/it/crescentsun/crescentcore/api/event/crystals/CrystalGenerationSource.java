package it.crescentsun.crescentcore.api.event.crystals;

/**
 * Represents the reason a crystal was spawned for a player.
 * This is used to determine how the player gained the crystal.
 */
public enum CrystalGenerationSource {

    /**
     * The player gained crystals through a command.
     */
    COMMAND,
    /**
     * The player gained crystals through an advancement.
     */
    ADVANCEMENT,
    /**
     * The player gained crystals through a mob drop.
     */
    MOB_DROP,
    /**
     * The player gained crystals through a chest loot.
     */
    CHEST_LOOT,
    /**
     * The player gained crystals through another miscellaneous reason.
     */
    MISC

}
