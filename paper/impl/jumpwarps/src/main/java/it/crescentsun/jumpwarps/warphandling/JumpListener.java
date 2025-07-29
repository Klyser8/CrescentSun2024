package it.crescentsun.jumpwarps.warphandling;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import it.crescentsun.api.common.DatabaseNamespacedKeys;
import it.crescentsun.api.crescentcore.data.player.PlayerData;
import it.crescentsun.api.crescentcore.util.BungeeUtils;
import it.crescentsun.crescentmsg.api.MessageFormatter;
import it.crescentsun.crescentmsg.api.MessageType;
import it.crescentsun.jumpwarps.JumpWarps;
import it.crescentsun.jumpwarps.lang.JumpWarpLocalization;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class JumpListener implements Listener {

    private final JumpWarps plugin;

    public JumpListener(JumpWarps plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerSpawnPostJoin(PlayerSpawnLocationEvent event) {
        Location playerLocation = event.getSpawnLocation();
        if (playerLocation.clone().subtract(0, 1, 0).getBlock().isSolid()) {
            return;
        }
        JumpWarpData jumpWarp = plugin.getJumpWarpManager().getClosestJumpWarp(playerLocation);
        if (jumpWarp == null) {
            return;
        }
        Location zeroYPlayerLoc = playerLocation.clone();
        Location zeroYJumpWarpLoc = jumpWarp.getLocation().clone();
        zeroYPlayerLoc.setY(0);
        zeroYJumpWarpLoc.setY(0);
        if (zeroYPlayerLoc.distance(zeroYJumpWarpLoc) > 24) {
            return;
        }
        Location safeLocation = jumpWarp.getLocation().clone().add(
                plugin.random().nextInt(3) - 2,
                1,
                plugin.random().nextInt(3) -2);
        // Check difference between player Y and safe location Y
        double yDifference = Math.abs(playerLocation.getY() - safeLocation.getY());
        if (yDifference < 150) {
            return;
        }
        // By now, it's very likely that the user last logged out of the server through a Jump Warp. Safely teleport them to it.
        event.setSpawnLocation(safeLocation);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PotionEffect potionEffect = player.getPotionEffect(PotionEffectType.SPEED);
        if (potionEffect == null) {
            return;
        }
        if (potionEffect.getAmplifier() != 255) {
            return;
        }
        player.removePotionEffect(PotionEffectType.SPEED);
    }

    @EventHandler
    public void onRedstoneTrigger(BlockRedstoneEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.LIGHT_WEIGHTED_PRESSURE_PLATE) {
            return;
        }
        Location loc = block.getLocation();
        JumpWarpData jumpWarp = plugin.getJumpWarpManager().getJumpWarpAtLocation(loc);
        if (jumpWarp == null) {
            return;
        }
        World world = loc.getWorld();
        if (event.getOldCurrent() == 0 && event.getNewCurrent() > 0) {
            world.playSound(loc, Sound.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 1, 2);
        }
    }

    @EventHandler
    public void onPressurePlateTrigger(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        if (block.getType() != Material.LIGHT_WEIGHTED_PRESSURE_PLATE) {
            return;
        }
        Player player = event.getPlayer();
        JumpWarpData jumpWarp = plugin.getJumpWarpManager().getJumpWarpAtLocation(block.getLocation());
        if (jumpWarp == null) {
            return;
        }
        plugin.getJumpWarpManager().getJumpWarpBuffer().put(player, new JumpWarpManager.PlayerJumpWarpBufferEntry(jumpWarp, player.getWorld().getGameTime()));
    }

//    @EventHandler
    public void altOnPressurePlateTrigger(EntityInteractEvent event) {
        if (!(event.getEntity() instanceof Item item && item.getItemStack().getType() == Material.WRITTEN_BOOK)) {
            return;
        }
        if (item.getThrower() == null) {
            return;
        }
        if (!(Bukkit.getEntity(item.getThrower()) instanceof Player player)) {
            return;
        }
        Block block = event.getBlock();
        if (block.getType() != Material.LIGHT_WEIGHTED_PRESSURE_PLATE) {
            return;
        }
        BookMeta meta = (BookMeta) item.getItemStack().getItemMeta();
        if (meta == null) {
            return;
        }
        String jumpWarpName = meta.getTitle();
        if (meta.pages().isEmpty()) {
            return;
        }
        TextComponent firstPageComponent = (TextComponent) meta.pages().getFirst();
        String serverDestination = firstPageComponent.content();
        // Ensure there is no other jumpwarps in the radius of 64 blocks TODO: From the same guild it should be every 256 blocks!
        for (JumpWarpData jumpWarp : plugin.getJumpWarpManager().getAllData(true)) {
            if (jumpWarp.getLocation().distance(block.getLocation()) < 64) {
                player.sendMessage(MessageFormatter.formatCommandMessage(MessageType.INCORRECT, "There is already a Jump Warp within 64 blocks of this location.", "64 blocks", "Jump Warp"));
                item.setItemStack(new ItemStack(Material.WRITABLE_BOOK));
                return;
            }
        }
        // Check that the player has enough crystals for this action
        PlayerData data = plugin.getPlayerDataService().getData(player.getUniqueId());
        Optional<Integer> crystalsOptional = data.getDataValue(DatabaseNamespacedKeys.PLAYER_CRYSTALS_IN_VAULT);
        if (crystalsOptional.isEmpty() || crystalsOptional.get() < 10) {
            player.sendMessage(MessageFormatter.formatCommandMessage(MessageType.INCORRECT, "You do not have enough crystals (10) to create a Jump Warp.", "10", "Jump Warp"));
            item.setItemStack(new ItemStack(Material.WRITABLE_BOOK));
            return;
        }
    }

//    @EventHandler
    public void onBookSign(PlayerEditBookEvent event) {
        if (!event.isSigning()) { //TODO: implement writing of books for creation of jumpwarps. Possibly a
            return;
        }
        BookMeta bookmeta = event.getNewBookMeta();
        TextComponent firstPage = (TextComponent) bookmeta.pages().getFirst();
        if (!bookmeta.hasTitle() || bookmeta.title() == null) {
            return;
        }
        //noinspection DataFlowIssue
        String titleContent = ((TextComponent) bookmeta.title()).content();
        if (!titleContent.contains("{Jump Warp}")) {
            return;
        }
        // Remove {Jump Warp}
        Player player = event.getPlayer();
        String warpName = titleContent.replace("{Jump Warp} ", "");
        if (plugin.getJumpWarpManager().doesJumpWarpExist(warpName)) {
            event.setCancelled(true);
            player.sendMessage(MessageFormatter.formatCommandMessage(MessageType.INCORRECT, "A Jump Warp with the name " + warpName + " already exists.", warpName, "Jump Warp"));
        }
        String serverDestination = firstPage.content();
        if (!plugin.getJumpWarpManager().doesServerDestinationExist(serverDestination)) {
            event.setCancelled(true);
            player.sendMessage(MessageFormatter.formatCommandMessage(MessageType.INCORRECT, "The server destination " + serverDestination + " does not exist.", serverDestination, "Jump Warp"));
        }
        player.sendMessage(MessageFormatter.formatCommandMessage(MessageType.SUCCESS,
                "You've successfully signed a book for a Jump Warp called " + warpName + " to the server " + serverDestination + ".",
                warpName, "Jump Warp", serverDestination));
    }

    @EventHandler
    public void onPlayerJump(PlayerJumpEvent event) {
        Player player = event.getPlayer();
        // Check that block beneath is a gold pressure plate
        PotionEffect jumpEffect = player.getPotionEffect(PotionEffectType.JUMP_BOOST);
        if (jumpEffect == null || jumpEffect.getAmplifier() < 90) {
            return;
        }
        JumpWarpManager jumpWarpManager = plugin.getJumpWarpManager();
        JumpWarpManager.PlayerJumpWarpBufferEntry playerJumpWarpBufferEntry = jumpWarpManager.getJumpWarpBuffer().get(player);
        if (playerJumpWarpBufferEntry == null) {
            return;
        }
        JumpWarpData jumpWarp = playerJumpWarpBufferEntry.jumpWarp();
        if (jumpWarp == null) {
            return;
        }
        jumpWarpManager.getJumpWarpBuffer().remove(player);
        launchPlayer(player, jumpWarp);
    }

    private void launchPlayer(Player player, JumpWarpData jumpWarpBlock) {
        World world = player.getWorld();
        playLaunchSounds(player, world);
        applyLaunchPotionEffects(player);
        scheduleLaunchTask(player, jumpWarpBlock);
    }

    private void playLaunchSounds(Player player, World world) {
        world.playSound(player.getLocation().add(0, 16, 0),
                Sound.BLOCK_BEACON_DEACTIVATE, SoundCategory.BLOCKS, 2, 2);
        player.playSound(player, Sound.BLOCK_BEACON_DEACTIVATE, SoundCategory.BLOCKS, 100, 2);
        player.playSound(player, Sound.ITEM_ELYTRA_FLYING, SoundCategory.BLOCKS, 0.75f, 1.5f);
    }

    private void applyLaunchPotionEffects(Player player) {
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.SPEED, 100, 255, false, false, false));
        player.addPotionEffect(
                new PotionEffect(PotionEffectType.DARKNESS, 100, 255, false, false, false));
    }

    /**
     * Schedules a task that will teleport the player to the appropriate server after 2 seconds.
     * @param player The player to teleport.
     */
    private void scheduleLaunchTask(Player player, JumpWarpData jumpWarp) {
        World world = player.getWorld();
        new BukkitRunnable() {
            private int ticks = 0;
            private boolean isCurrentlyWarping = false;
            private CompletableFuture<PlayerData> future = null;

            @Override
            public void run() {
                if (ticks == 0) {
                    future = plugin.getPlayerDataService().saveDataAsync(player);
                }
                if (ticks >= 100) {
                    cancel();
                    plugin.getLogger().warning("Failed to send player " + player.getName() + " to server after 100 ticks.");
                    return;
                }
                //After 30 ticks, will attempt sending the player to the target server every tick.
                if (ticks >= 30 && !isCurrentlyWarping) {
                    plugin.getLogger().info("Sending player to server.");
                    isCurrentlyWarping = true;
                    future.thenApplyAsync(playerData -> {
                        if (playerData != null) {
                            BungeeUtils.sendPlayerToServer(
                                    plugin, player, jumpWarp.getDestinationServer());
                        } else {
                            player.sendMessage(JumpWarpLocalization.ERROR_CANNOT_TELEPORT.getFormattedMessage(player.locale(), jumpWarp.getDestinationServer()));
                        }
                        plugin.getPlayerDataService().removeData(player.getUniqueId());
                        cancel();
                        return playerData;
                    });
                }
                if (player.getVelocity().getY() > 1) {
                    spawnParticles(world, player);
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 10, 1);
    }

    private void spawnParticles(World world, Player player) {
        world.spawnParticle(Particle.END_ROD, player.getLocation().add(player.getVelocity()),
                10, 0.25, 0.5, 0.25, 0);
        world.spawnParticle(Particle.CLOUD, player.getLocation().add(player.getVelocity()),
                10, 0.25, 0.5, 0.25, 0);
    }

}
