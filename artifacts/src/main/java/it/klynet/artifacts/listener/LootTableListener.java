package it.klynet.artifacts.listener;

import it.klynet.artifacts.Artifacts;
import org.bukkit.event.Listener;

public class LootTableListener implements Listener {
    private final Artifacts plugin;
    public LootTableListener(Artifacts plugin) {
        this.plugin = plugin;
    }
}
