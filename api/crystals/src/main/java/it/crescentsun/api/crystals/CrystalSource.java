package it.crescentsun.api.crystals;

/**
 * Represents the reason a crystal was spawned for a player.
 * This is used to determine how the player gained the crystal.
 */
public enum CrystalSource {

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
     * The player gained crystals through selling an item.
     */
    SALE,
    /**
     * The player gained crystals through breaking a crystal box.
     */
    CRYSTAL_BOX,
    /**
     * The player gained crystals through a challenge (Parkour etc.).
     */
    CHALLENGE,
    /**
     * The player gained crystals through finding a secret.
     */
    SECRET

}
