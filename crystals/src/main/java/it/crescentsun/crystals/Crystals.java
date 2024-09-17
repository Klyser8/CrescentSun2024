package it.crescentsun.crystals;

import it.crescentsun.artifacts.api.ArtifactFactory;
import it.crescentsun.artifacts.api.ArtifactUtil;
import it.crescentsun.artifacts.item.Artifact;
import it.crescentsun.artifacts.item.ArtifactFlag;
import it.crescentsun.crescentcore.CrescentCore;
import it.crescentsun.crescentcore.api.PrematureAccessException;
import it.crescentsun.crescentcore.api.crystals.CrystalSpawnAnimation;
import it.crescentsun.crescentcore.api.crystals.event.CrystalSource;
import it.crescentsun.crescentcore.api.crystals.event.GenerateCrystalsEvent;
import it.crescentsun.crescentcore.api.data.DataType;
import it.crescentsun.crescentcore.api.data.player.PlayerData;
import it.crescentsun.crescentcore.api.registry.CrescentNamespacedKeys;
import it.crescentsun.crescentcore.api.util.SoundEffect;
import it.crescentsun.crescentcore.api.util.VectorUtils;
import it.crescentsun.crystals.artifact.CrystalArtifact;
import it.crescentsun.crystals.crystalix.CrystalixManager;
import it.crescentsun.crescentcore.api.CrystalsProvider;
import it.crescentsun.crescentcore.api.registry.ArtifactNamespaceKeys;
import it.crescentsun.crescentcore.cmd.bukkit.BukkitCommandManager;
import it.crescentsun.crystals.data.CrystalsSettings;
import it.crescentsun.crystals.data.CrystalsStatistics;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class Crystals extends JavaPlugin implements CrystalsProvider {

    public static final UUID SETTINGS_UUID = UUID.fromString("051ed6ab-ab7a-40e8-ad5c-8d6e7ab8b175");
    public static final UUID STATISTICS_UUID = UUID.fromString("d6d3909a-0fae-4d1f-a124-f564783256ee");

    private static Crystals instance;
    private CrystalsStatistics statistics;
    private CrystalsSettings settings;
    @Override
    public void onEnable() {
        instance = this;
        getCrescentCore().setCrystalsProvider(this);
        BukkitCommandManager<CommandSender> commandManager = BukkitCommandManager.create(this);
        commandManager.registerCommand(new CrystalsCommands(this));
        registerArtifacts();
        CrescentCore.PLAYER_DATA_ENTRY_REGISTRY.registerPlayerDataEntry(CrescentNamespacedKeys.PLAYERS_CRYSTAL_AMOUNT, DataType.INT,0);

        CrescentCore.PLUGIN_DATA_REGISTRY.registerDataClass(this, CrystalsStatistics.class);
        CrescentCore.PLUGIN_DATA_REGISTRY.registerDataClass(this, CrystalsSettings.class);
    }

    private void registerArtifacts() {
        ArtifactFactory.registerArtifact(ArtifactNamespaceKeys.CRYSTAL, new CrystalArtifact(this,
                Crystals.createKey("crystal"),
                new ItemStack(Material.PRISMARINE_CRYSTALS),
                "Crystal",
                ArtifactFlag.WITH_GLINT
        ));
    }

    public CrescentCore getCrescentCore() {
        return CrescentCore.getInstance();
    }
    public static Crystals getInstance() {
        return instance;
    }
    public static NamespacedKey createKey(String key) {
        return new NamespacedKey(getInstance().getName().toLowerCase(), key);
    }

    @Override
    public void spawnCrystals(Player player, int amount, CrystalSource source, CrystalSpawnAnimation spawnAnimation) {
        if (amount > 99) {
            getLogger().warning("Tried to spawn more than 64 crystals at once. Amount was capped at 64.");
            amount = 99;
        }
        List<Item> crystals = new ArrayList<>();
        Location center = player.getLocation();
        Location target = center.clone().add(0, 3, 0); // Target location with Y offset
        World world = player.getWorld();

        GenerateCrystalsEvent event = new GenerateCrystalsEvent(amount, source, player);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        int finalAmount = Math.min(event.getAmount(), 99);
        Player finalPlayer = event.getPlayer();

        double distanceInAngles = (2 * Math.PI) / finalAmount; //Angle increment: 360 degrees divided by the amount of crystals AKA distance between each crystal
        float widthMultiplier = 3.0f;

        CompletableFuture<ItemStack> itemFuture = ArtifactFactory.getItemAsync(ArtifactNamespaceKeys.CRYSTAL, finalAmount);
        // Step 1: Spawn Items in Shape
        itemFuture.thenAccept(crystalStack -> {
            Bukkit.getScheduler().runTask(this, () -> {
                for (int i = 0; i < finalAmount; i++) {
                    ItemStack duplicate = crystalStack.clone();
                    duplicate.setAmount(1);
                    double currentAngle = distanceInAngles * i;
                    Location spawnLocation = center.clone().add(
                            Math.cos(currentAngle) * widthMultiplier,
                            0,
                            Math.sin(currentAngle) * widthMultiplier);
                    Item crystal = world.dropItem(spawnLocation, duplicate);
                    crystal.setCanPlayerPickup(false);
                    crystal.setGravity(false);
                    crystal.setVelocity(new Vector(0, 0, 0));
                    crystals.add(crystal);
                }
                Bukkit.getScheduler().runTaskTimer(Crystals.getInstance(), bukkitTask -> {
                    boolean isAnimationDone = true;
                    boolean hasPlayedSound = false;
                    for (int i = 0; i < crystals.size(); i++) {
                        Item crystal = crystals.get(i);
                        if (crystal.isDead()) {
                            continue;
                        }
                        Location crystalLocation = crystal.getLocation();
                        if (crystal.getTicksLived() < 200) {
                            long timeAlive = crystal.getTicksLived(); //t
                            double timeAliveMultiplier = 0.1;
                            double angleIncrement = timeAlive * timeAliveMultiplier;
                            double totalAngle = distanceInAngles * i + angleIncrement;
                            Location nextLocation = center.clone().add(
                                    Math.cos(totalAngle) * widthMultiplier / Math.max((timeAlive * timeAliveMultiplier * 0.25), 1),
                                    timeAlive * timeAliveMultiplier / 6,
                                    Math.sin(totalAngle) * widthMultiplier / Math.max((timeAlive * timeAliveMultiplier * 0.25), 1));
                            Vector direction = VectorUtils.getDirection(crystalLocation.toVector(), nextLocation.toVector());
                            double distance = Math.max(crystal.getLocation().distance(nextLocation), 0.01);
                            crystal.setVelocity(direction.multiply(0.1 * distance));
                        } else if (crystal.getTicksLived() == 200) {
                            crystal.setGravity(true);
                            //Shoot in random direction
                            crystal.setVelocity(new Vector(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5).multiply(0.5));
                            if (!hasPlayedSound) {
                                new SoundEffect(Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.5f, 1.5f).playSoundAtLocation(target);
                                new SoundEffect(Sound.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, 0.4f, 0.75f).playSoundAtLocation(target);
                                new SoundEffect(Sound.BLOCK_AMETHYST_BLOCK_BREAK, SoundCategory.PLAYERS, 0.4f, 0.5f).playSoundAtLocation(target);
                                hasPlayedSound = true;
                            }
                            Artifact artifact = ArtifactUtil.identifyArtifact(crystalStack);
                            if (artifact instanceof CrystalArtifact crystalArtifact) {
                                crystalArtifact.
                            }
                        }
                        boolean isInAir = !crystal.isOnGround();
                        if (isInAir) {
                            world.spawnParticle(Particle.DOLPHIN, crystalLocation.clone().add(0, 0.25, 0), 5, 0.25, 0.25, 0.25, 0);
                            if (crystal.getTicksLived() % 4 == 0) {
                                world.spawnParticle(Particle.FIREWORK, crystalLocation.clone().add(0, 0.25, 0), 1, 0.25, 0.25, 0.25, 0);
                            }
                            if (crystal.getTicksLived() % 15 + getInstance().getCrescentCore().getRandom().nextInt(10) == 0) {
                                new SoundEffect(Sound.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 1.5f, 1.5f).playSoundAtLocation(crystalLocation);
                            }
                            isAnimationDone = false;
                        } else {
                            if (finalPlayer != null) {
                                crystal.setThrower(finalPlayer.getUniqueId());
                            }
                            crystal.setCanPlayerPickup(true);
                            crystal.setPickupDelay(20);
                        }
                    }
                    if (isAnimationDone) {
                        bukkitTask.cancel();
                    }
                }, 0, 1); // Start immediately, repeat every tick
            });
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });

    }

    @Override
    public void addCrystals(Player player, int amount, CrystalSource source) {
        PlayerData playerData = getCrescentCore().getPlayerDataManager().getData(player.getUniqueId());
        int crystals = playerData.getDataValue(CrescentNamespacedKeys.PLAYERS_CRYSTAL_AMOUNT);
        playerData.updateDataValue(CrescentNamespacedKeys.PLAYERS_CRYSTAL_AMOUNT, crystals + amount);
        switch (source) {
            default:
                getStatistics().setCrystalsGenerated(getStatistics().getCrystalsGenerated() + amount);
                break;
        }
        getLogger().info("Added " + amount + " crystals to " + player.getName());
    }

    @Override
    public void setCrystals(Player player, int amount, CrystalSource source) {
        PlayerData playerData = getCrescentCore().getPlayerDataManager().getData(player.getUniqueId());
        int oldCrystalAmount = playerData.getDataValue(CrescentNamespacedKeys.PLAYERS_CRYSTAL_AMOUNT);
        playerData.updateDataValue(CrescentNamespacedKeys.PLAYERS_CRYSTAL_AMOUNT, amount);
        int difference = amount - oldCrystalAmount;
        if (difference > 0) {
            getStatistics().setCrystalsGenerated(getStatistics().getCrystalsGenerated() + difference);
        } else {
            getStatistics().setCrystalsLost(getStatistics().getCrystalsLost() + Math.abs(difference));
        }
        getLogger().info("Set " + player.getName() + "'s crystals to " + amount);
    }

    @Override
    public void removeCrystals(Player player, int amount, CrystalSource source) {
        PlayerData playerData = getCrescentCore().getPlayerDataManager().getData(player.getUniqueId());
        int crystals = playerData.getDataValue(CrescentNamespacedKeys.PLAYERS_CRYSTAL_AMOUNT);
        playerData.updateDataValue(CrescentNamespacedKeys.PLAYERS_CRYSTAL_AMOUNT, Math.max(0, crystals - amount));
        if (Objects.requireNonNull(source) == CrystalSource.SALE) {
            getStatistics().setCrystalsSpent(getStatistics().getCrystalsSpent() + amount);
        } else {
            getStatistics().setCrystalsLost(getStatistics().getCrystalsLost() + amount);
        }
        getLogger().info("Removed " + amount + " crystals from " + player.getName());
    }

    @Override
    public int getCrystals(Player player) {
        return getCrescentCore().getPlayerDataManager().getData(player.getUniqueId()).getDataValue(CrescentNamespacedKeys.PLAYERS_CRYSTAL_AMOUNT);
    }

    public CrystalsStatistics getStatistics() {
        if (statistics == null) {
            throw new PrematureAccessException("CrystalsStatistics object is not initialized yet.");
        }
        return statistics;
    }

    public void setStatistics(CrystalsStatistics statistics) {
        if (this.statistics == null) {
            this.statistics = statistics;
        } else {
            getLogger().warning("Attempted overwriting of CrystalsData object. Ignoring.");
        }
    }

    public CrystalsSettings getSettings() {
        return settings;
    }

    public void setSettings(CrystalsSettings settings) {
        if (this.settings == null) {
            this.settings = settings;
        } else {
            getLogger().warning("Attempted overwriting of CrystalsSettings object. Ignoring.");
        }
    }
}
