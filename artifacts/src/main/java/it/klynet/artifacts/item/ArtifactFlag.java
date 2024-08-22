package it.klynet.artifacts.item;

/**
 * Flags to be applied to an artifact item.
 */
public enum ArtifactFlag {

    UNSPLITTABLE, // Prevents the item from being unstacked
    UNIQUE, // Adds a UUID to the item's NBT data, to make it unique.
    UNSTACKABLE, // Max stack size is 1,
    WITH_GLINT, // Adds the enchantment glint to the item
    HIDE_DROP_NAME, // Hides the drop name of the item

}
