package it.crescentsun.jumpwarps;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import it.crescentsun.crescentcore.api.BungeeUtils;
import it.crescentsun.crescentcore.api.PlayerUtils;
import it.crescentsun.crescentcore.api.data.player.PlayerData;
import it.crescentsun.crescentcore.api.event.server.ProtoweaverConnectionEstablishedEvent;
import it.crescentsun.crescentcore.api.registry.CrescentNamespaceKeys;
import it.crescentsun.crescentmsg.api.MessageFormatter;
import it.crescentsun.crescentmsg.api.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.WritableBookMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class JumpListener implements Listener {

    private final JumpWarps plugin;

    public JumpListener(JumpWarps plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRedstoneTrigger(BlockRedstoneEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();
        JumpWarpData jumpWarp = plugin.getJumpWarpManager().getJumpWarpAtLocation(loc);
        if (jumpWarp == null) {
            return;
        }
        if (!JumpWarpUtil.isWithinJumpWarp(loc, jumpWarp)) {
            return;
        }
        Location fxLoc = block.getLocation().clone().add(0.5, 0.5, 0.5);
        World world = loc.getWorld();
        if (plugin.getJumpWarpManager().getJumpWarpAtLocation(loc) == null) {
            return;
        }
        if (event.getOldCurrent() == 0 && event.getNewCurrent() > 0) {
            world.playSound(fxLoc, Sound.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 1, 2);
        }
    }

//    @EventHandler
    public void onPressurePlateTrigger(EntityInteractEvent event) {
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
        PlayerData data = plugin.getCrescentCore().getPlayerDBManager().getData(player.getUniqueId());
        int crystals = data.getDataValue(CrescentNamespaceKeys.PLAYERS_CRYSTAL_AMOUNT);
        if (crystals < 10) {
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
        JumpWarpData jumpWarp = plugin.getJumpWarpManager().getJumpWarpAtLocation(player.getLocation());
        if (jumpWarp == null) {
            return;
        }
        PotionEffect jumpEffect = player.getPotionEffect(PotionEffectType.JUMP_BOOST);
        if (jumpEffect == null || jumpEffect.getAmplifier() < 90) {
            return;
        }
        if (JumpWarpUtil.isWithinJumpWarp(player.getLocation(), jumpWarp)) {
            // Player is within the jump warp, so we can launch them.
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 6, 93));
            launchPlayer(player, jumpWarp);
        }
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

            @Override
            public void run() {
                if (ticks >= 100) {
                    cancel();
                    plugin.getLogger().warning("Failed to send player " + player.getName() + " to server after 100 ticks.");
                    return;
                }
                //After 30 ticks, will attempt sending the player to the target server every tick.
                if (ticks >= 30 && !isCurrentlyWarping) {
                    plugin.getLogger().info("Sending player to server.");
                    isCurrentlyWarping = true;
                    BungeeUtils.saveDataAndSendPlayerToServer(
                            plugin.getCrescentCore(), plugin, player, jumpWarp.getDestinationServer()).thenApplyAsync((result) -> {
                                if (result) {
                                    plugin.getLogger().info("Player sent to server " + jumpWarp.getDestinationServer());
                                } else {
                                    plugin.getLogger().warning("Failed to send player to server " + jumpWarp.getDestinationServer());
                                }
                                cancel();
                                return result;
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
