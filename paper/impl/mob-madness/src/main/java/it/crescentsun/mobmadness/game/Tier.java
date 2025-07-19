package it.crescentsun.mobmadness.game;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;

/**
 * Enums which represent the different possible tiers that are present in Mob Madness.
 */
public enum Tier {

    I(1, TextColor.color(127, 127, 127), 30, 0, 499), // Gray
    II(2, TextColor.color(8, 203, 24), 60, 500, 1499), // Green
    III(3, TextColor.color(113, 75, 254), 120, 1500, 2999), // Blue
    IV(4, TextColor.color(234, 1, 255), 210, 3000, 4999), // Fuchsia
    V(5, TextColor.color(255, 204, 1), 330, 5000, 7499), // Gold
    VI(6, TextColor.color(252, 53, 9), 480, 7500, 9999), // Red
    VII(7, TextColor.color(0, 0, 0), 660, 10000, 13333); // Black


    final int numericTier;
    final TextColor tierColor;
    final int defaultMaxWeight;
    final int minScore;
    final int maxScore;

    /**
     * Constructor for the Tier enum.
     * @param numericTier the numeric representation of the tier
     * @param tierColor the color associated with the tier
     * @param defaultMaxWeight the default maximum weight of mobs that can spawn in this tier.
     * @param minScore the minimum score required to reach this tier
     * @param maxScore the maximum score that can be achieved in this tier
     */
    Tier(int numericTier, TextColor tierColor, int defaultMaxWeight, int minScore, int maxScore) {
        this.numericTier = numericTier;
        this.tierColor = tierColor;
        this.defaultMaxWeight = defaultMaxWeight;
        this.minScore = minScore;
        this.maxScore = maxScore;
    }

    public int getNumericTier() {
        return numericTier;
    }

    public TextColor getTierColor() {
        return tierColor;
    }

    public int getDefaultMaxWeight() {
        return defaultMaxWeight;
    }

    public int getMinScore() {
        return minScore;
    }

    public int getMaxScore() {
        return maxScore;
    }
}
