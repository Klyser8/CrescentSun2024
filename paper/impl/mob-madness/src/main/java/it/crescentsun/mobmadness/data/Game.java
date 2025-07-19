package it.crescentsun.mobmadness.data;

import it.crescentsun.mobmadness.data.entity.Beast;
import it.crescentsun.mobmadness.data.entity.BossBeast;
import it.crescentsun.mobmadness.game.MapType;
import it.crescentsun.mobmadness.game.Tier;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;

public class Game {

    // Team used for glow effect on players below 33% HP
    public static final String LOW_HP_PLAYERS_TEAM_NAME = "lowHP_players";

    // How many ticks have elapsed since the start of the game
    private int ticksElapsed;
    // The current bound of the game. Dictates the amount of mobs that spawn
    private int bound;
    // The maximum weight of mobs that can spawn in the game
    private int maxWeight;
    // The current weight of mobs that have spawned in the game
    private int currentWeight;
    // The map's respawn anchor block
    private final Block respawnAnchor;
    // Current game tier
    private Tier tier = Tier.I;
    // Map of players and their corresponding Survivor data
    private final Map<Player, Survivor> survivors;
    // Map of mobs and their corresponding Beast data
    private final Map<Mob, Beast<?>> beasts;
    // The current boss of the game
    private BossBeast<?> currentBoss = null;
    // The team containing all low HP players. Used for glowing.
    private Team lowHPTeam; //TODO: Implement low HP team
    // The scoreboard manager used to manage the scoreboard
    private ScoreboardManager scoreboardManager;
    // The game's scoreboard
    private Scoreboard scoreboard;
    // Main game loop
    private final BukkitTask gameLoop;
    // Data of the map being played
    private final GameMap map;

    public Game(BukkitTask gameLoop, MapType mapType) {
        this.respawnAnchor = mapType.getSpawnLocation().add(0, -1, 0).getBlock();
        // Create new team
        this.gameLoop = gameLoop;
        this.map = new GameMap(mapType);
        this.survivors = new HashMap<>();
        this.beasts = new HashMap<>();
        setupScoreboard();
    }

    private void setupScoreboard() {
        scoreboardManager = Bukkit.getScoreboardManager();
        scoreboard = scoreboardManager.getNewScoreboard();

        lowHPTeam = scoreboard.registerNewTeam(LOW_HP_PLAYERS_TEAM_NAME);

        lowHPTeam.color(NamedTextColor.RED);
    }

    /**
     * Calculates what the average score of the game is.
     *
     * @return the average score calculated
     */
    public int calculateAvgScore() {
        int score = 0;
        for (Player player : survivors.keySet()) {
            score += player.getLevel();
        }
        return score / survivors.size();
    }

    /**
     * Gets the current time (in seconds) of the game.
     *
     * @return time
     */
    public int getTicksElapsed() {
        return ticksElapsed;
    }

    /**
     * Sets the current time (in ticks) of the game.
     *
     * @param ticksElapsed the new time (in ticks).
     */
    public void setTicksElapsed(int ticksElapsed) {
        this.ticksElapsed = ticksElapsed;
    }

    /**
     * Gets the current bound of the game.
     * The bound is what dictates the frequency at which mobs spawn in a map.
     * Based off the size of the map, current players & current tier.
     *
     * @return bound
     */
    public int getBound() {
        return bound;
    }

    /**
     * Sets the current bound of the game.
     *
     * @param bound the new bound
     */
    public void setBound(int bound) {
        this.bound = bound;
    }

    /**
     * Gets the current weight of the game.
     * Each mob has a different weight. Harder mobs have a higher weight.
     *
     * @return {@link #currentWeight}
     */
    public int getCurrentWeight() {
        return currentWeight;
    }

    /**
     * Sets the current weight of the game.
     *
     * @param currentWeight the new weight
     */
    public void setCurrentWeight(int currentWeight) {
        this.currentWeight = currentWeight;
    }

    /**
     * Gets the current max weight of the game.
     * Dictated by current map, players present & tier.
     * Once the current weight reaches the {@link #maxWeight}, no more mobs can spawn.
     *
     * @return {@link #maxWeight}
     */
    public int getMaxWeight() {
        return maxWeight;
    }

    /**
     * Sets the maximum weight of the game.
     * @param maxWeight the new maximum weight
     */
    public void setMaxWeight(int maxWeight) {
        this.maxWeight = maxWeight;
    }

    /**
     * Gets the current tier of the game.
     * The current tier is based off the average score of all the players.
     *
     * @return {@link #tier}
     */
    public Tier getTier() {
        return tier;
    }

    /**
     * Sets the current tier of the game.
     *
     * @param tier the new tier
     */
    public void setTier(Tier tier) {
        this.tier = tier;
    }

    /**
     * Gets a map containing {@link Player} instances and their respective {@link Survivor} instances.
     *
     * @return {@link #survivors}
     */
    public Map<Player, Survivor> getSurvivors() {
        return survivors;
    }

    /**
     * Gets the current {@link GameMap} of the game.
     *
     * @return {@link #map}
     */
    public GameMap getMap() {
        return map;
    }

    /**
     * Gets the {@link BukkitTask} object of the game.
     * Runs all scheduled tasks of the game.
     *
     * @return {@link #gameLoop}
     */
    public BukkitTask getGameLoop() {
        return gameLoop;
    }

    /**
     * Util method to return a copy of the map's spawn point more easily.
     *
     * @return the map's spawn point
     */
    public Location getMapSpawn() {
        return map.getMapType().getSpawnLocation();
    }

    public Map<Mob, Beast<?>> getBeastMap() {
        return beasts;
    }

    /**
     * Returns the amount of charges the game's respawn anchor has.
     *
     * @return how many charges the respawn anchor currently has
     */
    public int getAnchorCharges() {
        return ((RespawnAnchor) respawnAnchor.getBlockData()).getCharges();
    }

    /**
     * Sets the amount of charges of the game's respawn anchor.
     *
     * @param charges how many charges the respawn anchor should have. MAX 4
     */
    public void setAnchorCharges(int charges) {
        RespawnAnchor anchor = (RespawnAnchor) respawnAnchor.getBlockData();
        anchor.setCharges(charges);
        respawnAnchor.setBlockData(anchor);
    }

    public Block getRespawnAnchor() {
        return respawnAnchor;
    }

    public Scoreboard getBoard() {
        return scoreboard;
    }

    public BossBeast getCurrentBoss() {
        return currentBoss;
    }

    public void setCurrentBoss(BossBeast currentBoss) {
        this.currentBoss = currentBoss;
    }
}
