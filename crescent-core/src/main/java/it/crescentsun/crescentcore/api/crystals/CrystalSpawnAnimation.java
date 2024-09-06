package it.crescentsun.crescentcore.api.crystals;

/**
 * Represents the different animations that can be used when spawning a crystal.
 * This is used to determine how the crystal should be animated when spawned.
 * The min and max values are used to determine the amount of crystals required to use the animation.
 */
public enum CrystalSpawnAnimation {

    CIRCLING_EXPLOSION(5, 32),
    HOVER(1, 1),
    SPRING_SIMULTANEOUS(2,12),
    SPRING_SEQUENTIAL(3, 12);

    private final int min;
    private final int max;
    CrystalSpawnAnimation(int min, int max) {
        this.min = min;
        this.max = max;
    }

    /**
     * @return The minimum amount of crystals required to use this animation.
     */
    public int getMin() {
        return min;
    }

    /**
     * @return The maximum amount of crystals required to use this animation.
     */
    public int getMax() {
        return max;
    }
}
