package it.crescentsun.artifacts.item;

/**
 * Flags to be applied to an artifact item.
 */
public enum ArtifactFlag {

    /**
     * Prevents the item from being unstacked
     */
    UNSPLITTABLE("unsplittable", 0),
    /**
     * Adds a UUID to the item's NBT data, to make it unique.
     */
    UNIQUE("unique", 1),
    /**
     * Prevents the item from being stacked beyond 1
     */
    UNSTACKABLE("unstackable", 2),
    /**
     * Adds an enchantment glint to the item
     */
    WITH_GLINT("glint", 3),
    /**
     * Hides the drop name of the item
     */
    HIDE_DROP_NAME("no_drop_name", 4);


    private final String flagName;
    private final int id;
    ArtifactFlag(String flagName, int id) {
        this.flagName = flagName;
        this.id = id;
    }

    public String getName() {
        return flagName;
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
