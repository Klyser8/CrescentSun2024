package it.crescentsun.api.artifacts.item;

/**
 * Flags to be applied to an artifact item.
 */
public enum ArtifactFlag {

    /**
     * Prevents the item from being unstacked
     */
    UNSPLITTABLE("unsplittable", "Prevents unstacking", 0),
    /**
     * Adds a UUID to the item's NBT data, to make it unique.
     */
    UNIQUE("unique", "Artifact has a unique id", 1),
    /**
     * Prevents the item from being stacked beyond 1
     */
    UNSTACKABLE("unstackable", "Prevents stacking beyond 1", 2),
    /**
     * Adds an enchantment glint to the item
     */
    WITH_GLINT("glint", "", 3),
    /**
     * Hides the drop name of the item
     */
    HIDE_DROP_NAME("no_drop_name",  "Invisible drop name", 4),
    /**
     * Prevents the item from being given out with the /art give command.
     */
    NO_GIVE("no_give", "", 5);


    private final String flagName;
    private final String description;
    private final int id;
    ArtifactFlag(String flagName, String description, int id) {
        this.flagName = flagName;
        this.description = description;
        this.id = id;
    }

    public String getName() {
        return flagName;
    }

    public String getDescription() {
        return description;
    }

    public int getId() {
        return id;
    }

    public static ArtifactFlag fromString(String flagName) {
        for (ArtifactFlag flag : values()) {
            if (flag.getName().equalsIgnoreCase(flagName)) {
                return flag;
            }
        }
        return null;
    }

    public static ArtifactFlag fromId(int id) {
        for (ArtifactFlag flag : values()) {
            if (flag.getId() == id) {
                return flag;
            }
        }
        return null;
    }
}
