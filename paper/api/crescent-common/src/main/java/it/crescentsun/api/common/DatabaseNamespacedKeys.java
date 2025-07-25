package it.crescentsun.api.common;

import org.bukkit.NamespacedKey;

import static it.crescentsun.api.common.PluginNamespacedKeys.*;

public class DatabaseNamespacedKeys {

    // PLAYER DATA
    // crescent-core
    public static final NamespacedKey PLAYER_USERNAME = new NamespacedKey(NAMESPACE_CRESCENTCORE, "username");
    public static final NamespacedKey PLAYER_UUID = new NamespacedKey(NAMESPACE_CRESCENTCORE, "player_uuid");
    public static final NamespacedKey PLAYER_FIRST_LOGIN = new NamespacedKey(NAMESPACE_CRESCENTCORE, "first_login");
    public static final NamespacedKey PLAYER_LAST_SEEN = new NamespacedKey(NAMESPACE_CRESCENTCORE, "last_seen");
    public static final NamespacedKey PLAYER_PLAY_TIME = new NamespacedKey(NAMESPACE_CRESCENTCORE, "play_time");
    public static final NamespacedKey PLAYER_LAST_LOGIN = new NamespacedKey(NAMESPACE_CRESCENTCORE, "last_login");
    // jumpwarps
    public static final NamespacedKey PLAYER_JUMPWARPS_USED = new NamespacedKey(NAMESPACE_JUMPWARPS, "jumpwarps_used");
    // crystals
    public static final NamespacedKey PLAYER_CRYSTALS_SPAWNED = new NamespacedKey(NAMESPACE_CRYSTALS, "crystals_spawned"); //TODO: RENAME DATABASE COLUMN
    public static final NamespacedKey PLAYER_CRYSTALS_IN_VAULT = new NamespacedKey(NAMESPACE_CRYSTALS, "crystals_in_vault");
    public static final NamespacedKey PLAYER_CRYSTALS_CLAIMED = new NamespacedKey(NAMESPACE_CRYSTALS, "crystals_claimed"); //
    public static final NamespacedKey PLAYER_SHOW_CRYSTALIX = new NamespacedKey(NAMESPACE_CRYSTALS, "show_crystalix");

    // SERVER DATA
    // jumpwarps
    public static final NamespacedKey JUMPWARPS = new NamespacedKey(NAMESPACE_JUMPWARPS, "jumpwarps");
    public static final NamespacedKey JUMPWARPS_STATS = new NamespacedKey(NAMESPACE_JUMPWARPS, "stats");
    // crystals
    public static final NamespacedKey CRYSTALS_STATS = new NamespacedKey(NAMESPACE_CRYSTALS, "stats");

}
