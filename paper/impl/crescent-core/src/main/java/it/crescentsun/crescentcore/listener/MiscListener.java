package it.crescentsun.crescentcore.listener;

import it.crescentsun.api.crescentcore.event.player.PlayerJoinEventPostDBLoad;
import it.crescentsun.api.crescentcore.util.AdvancementUtil;
import it.crescentsun.crescentcore.CrescentCore;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MiscListener implements Listener {

    private final CrescentCore crescentCore;
    public MiscListener(CrescentCore crescentCore) {
        this.crescentCore = crescentCore;
    }

    @EventHandler
    public void onPlayerJoinPostDBLoad(PlayerJoinEventPostDBLoad event) {
        if (!crescentCore.getServerName().equalsIgnoreCase("crescentcraft")) {
            return;
        }
        AdvancementUtil.awardAdvancementCriteria(event.getPlayer(), "crescentsun:crescentcraft/root", "root");
    }

}
