package it.klynet.crystals;

import it.klynet.artifacts.api.ArtifactFactory;
import it.klynet.artifacts.item.ArtifactFlag;
import it.klynet.crystals.artifact.CrystalArtifact;
import it.klynet.crystals.artifact.CrystalixArtifact;
import it.klynet.crystals.crystalix.CrystalixManager;
import it.klynet.crystals.crystalix.listener.PlayerListener;
import it.klynet.klynetcore.KlyNetCore;
import it.klynet.klynetcore.api.CrystalsProvider;
import it.klynet.klynetcore.api.SoundEffect;
import it.klynet.klynetcore.api.VectorUtils;
import it.klynet.klynetcore.api.event.crystals.CrystalGenerationSource;
import it.klynet.klynetcore.api.event.crystals.GenerateCrystalsEvent;
import it.klynet.klynetcore.api.registry.ArtifactNamespaceKeys;
import it.klynet.klynetcore.api.registry.KlyNetNamespaceKeys;
import it.klynet.klynetcore.cmd.bukkit.BukkitCommandManager;
import it.klynet.klynetcore.core.data.player.PlayerData;
import it.klynet.klynetcore.plugindata.DataType;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public final class Crystals extends JavaPlugin implements CrystalsProvider {

    private final CrystalixManager crystalixManager = new CrystalixManager(this);
    public static final String PLUGIN_KEY = "crystals";
    private static Crystals instance;
    private final ConfigProvider configProvider = new ConfigProvider(this);
    @Override
    public void onEnable() {
        instance = this;
        getKlyNetCore().setCrystalsProvider(this);
        saveDefaultConfig();
        configProvider.loadFromConfig();
        BukkitCommandManager<CommandSender> commandManager = BukkitCommandManager.create(this);
        commandManager.registerCommand(new CrystalsCommands(this));
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        registerArtifacts();
        KlyNetCore.PLAYER_DATA_REGISTRY.registerPluginData(KlyNetNamespaceKeys.CRYSTALS_AMOUNT, DataType.INT,0);
        KlyNetCore.PLAYER_DATA_REGISTRY.registerPluginData(KlyNetNamespaceKeys.SETTINGS_SHOW_CRYSTALIX, DataType.BOOLEAN, true);
    }

    private void registerArtifacts() {
        ArtifactFactory.registerArtifact(ArtifactNamespaceKeys.CRYSTAL, new CrystalArtifact(this,
                Crystals.createKey("crystal"),
                new ItemStack(Material.PRISMARINE_CRYSTALS),
                "Crystal",
                ArtifactFlag.WITH_GLINT
        ));
        ArtifactFactory.registerArtifact(ArtifactNamespaceKeys.CRYSTALIX, new CrystalixArtifact(
                Crystals.createKey("crystalix"),
                new ItemStack(Material.ENDER_CHEST),
                "Crystalix",
                ArtifactFlag.WITH_GLINT,
                ArtifactFlag.HIDE_DROP_NAME
        ));
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            crystalixManager.removeCrystalix(player);
        }
    }

    public CrystalixManager getCrystalixManager() {
        return crystalixManager;
    }

    public KlyNetCore getKlyNetCore() {
        return KlyNetCore.getInstance();
    }
    public static Crystals getInstance() {
        return instance;
    }
    public static NamespacedKey createKey(String key) {
        return new NamespacedKey(PLUGIN_KEY, key);
    }
    public ConfigProvider getConfigProvider() {
        return configProvider;
    }

    @Override
    public void spawnCrystals(Player player, int amount, CrystalGenerationSource source) {
        if (amount > 64) {
            getLogger().warning("Tried to spawn more than 64 crystals at once. Amount was capped at 64.");
            amount = 64;
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
        int finalAmount = Math.min(event.getAmount(), 64);
        Player finalPlayer = event.getPlayer();

        double distanceInAngles = (2 * Math.PI) / finalAmount; //Angle increment: 360 degrees divided by the amount of crystals AKA distance between each crystal
        float widthMultiplier = 3.0f;

        CompletableFuture<ItemStack> itemFuture = ArtifactFactory.getItemAsync(ArtifactNamespaceKeys.CRYSTAL, finalAmount);
        // Step 1: Spawn Items in Shape
        itemFuture.thenAccept(itemStack -> {
            Bukkit.getScheduler().runTask(this, () -> {
                for (int i = 0; i < finalAmount; i++) {
                    ItemStack duplicate = itemStack.clone();
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
                        }
                        boolean isInAir = !crystal.isOnGround();
                        if (isInAir) {
                            world.spawnParticle(Particle.DOLPHIN, crystalLocation.clone().add(0, 0.25, 0), 5, 0.25, 0.25, 0.25, 0);
                            if (crystal.getTicksLived() % 4 == 0) {
                                world.spawnParticle(Particle.FIREWORK, crystalLocation.clone().add(0, 0.25, 0), 1, 0.25, 0.25, 0.25, 0);
                            }
                            if (crystal.getTicksLived() % 15 + getInstance().getKlyNetCore().getRandom().nextInt(10) == 0) {
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
    public void addCrystals(Player player, int amount) {
        PlayerData playerData = getKlyNetCore().getPlayerManager().getData(player.getUniqueId());
        int crystals = playerData.getData(KlyNetNamespaceKeys.CRYSTALS_AMOUNT);
        playerData.updateData(KlyNetNamespaceKeys.CRYSTALS_AMOUNT, crystals + amount);
        getLogger().info("Added " + amount + " crystals to " + player.getName());
    }

    @Override
    public void setCrystals(Player player, int amount) {
        PlayerData playerData = getKlyNetCore().getPlayerManager().getData(player.getUniqueId());
        playerData.updateData(KlyNetNamespaceKeys.CRYSTALS_AMOUNT, amount);
        getLogger().info("Set " + player.getName() + "'s crystals to " + amount);
    }

    @Override
    public void removeCrystals(Player player, int i) {
        PlayerData playerData = getKlyNetCore().getPlayerManager().getData(player.getUniqueId());
        int crystals = playerData.getData(KlyNetNamespaceKeys.CRYSTALS_AMOUNT);
        playerData.updateData(KlyNetNamespaceKeys.CRYSTALS_AMOUNT, Math.max(0, crystals - i));
        getLogger().info("Removed " + i + " crystals from " + player.getName());
    }

    @Override
    public int getCrystals(Player player) {
        return getKlyNetCore().getPlayerManager().getData(player.getUniqueId()).getData(KlyNetNamespaceKeys.CRYSTALS_AMOUNT);
    }

    public static void spawnCrystalOld(Player player, int amount) {
        List<Item> crystals = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger();
        Location spawnLocation = player.getLocation().clone();
        final Location targetLocation = spawnLocation.clone().add(0, 3, 0); // Target location with Y offset

        Bukkit.getScheduler().runTaskTimer(Crystals.getInstance(), outerTask -> {
            if (counter.getAndIncrement() < amount) {
                Item crystal = player.getWorld().dropItem(spawnLocation, ArtifactFactory.getItem(ArtifactNamespaceKeys.CRYSTAL, 1));
                crystal.setCanPlayerPickup(false);
                crystal.setGravity(false);
                crystal.setVelocity(new Vector(0, 0, 0));
                crystals.add(crystal);

                Bukkit.getScheduler().runTaskTimer(Crystals.getInstance(), innerTask -> {
                    if (!crystals.contains(crystal)) {
                        innerTask.cancel();
                        return;
                    }

                    long timeAlive = crystal.getTicksLived();
                    double distanceY = targetLocation.getY() - crystal.getLocation().getY();
                    // Adjust spiral radius based on distance to target, considering X, Y, and Z.
                    double radius = Math.max(0.1, Math.sqrt(crystal.getLocation().distanceSquared(targetLocation)) / 40);

                    double angle = Math.toRadians((double) timeAlive * 5); // Adjust rotation speed here
                    double dx = radius * Math.cos(angle);
                    double dz = radius * Math.sin(angle);
                    double dy = 0.05; // Adjust upward motion speed here to ensure it reaches the target

                    Vector velocity = new Vector(dx, dy, dz);
                    crystal.setVelocity(velocity);

                    // Check if the crystal has reached the target location, considering X, Y, and Z.
                    if (crystal.getLocation().distance(targetLocation) <= 0.5) { // Consider a small threshold to allow for convergence
                        crystal.setGravity(true);
                        crystal.setVelocity(new Vector(0, 0.1, 0)); // Gentle drop
                        crystal.setOwner(player.getUniqueId());
                        crystal.setCanPlayerPickup(true);
                        crystal.setPickupDelay(20);
                        innerTask.cancel();
                    }

                }, 0, 1); // Start immediately, repeat every tick
            }
            if (crystals.size() >= amount) {
                outerTask.cancel(); // Cancel the spawning task once the desired amount is reached
            }
        }, 0, 10); // Initial spawn delay and period between spawns.
    }








}
