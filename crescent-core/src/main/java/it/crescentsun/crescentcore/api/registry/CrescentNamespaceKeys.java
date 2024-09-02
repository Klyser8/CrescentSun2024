package it.crescentsun.crescentcore.api.registry;

import org.bukkit.NamespacedKey;

@SuppressWarnings("unused")
public class CrescentNamespaceKeys {

    // KEY NAMES
    protected static final String KEY_CRESCENTCORE = "crescentcore";
    protected static final String KEY_JUMPWARPS = "jumpwarps";
    protected static final String KEY_CRYSTALS = "crystals";

    // PLAYER DATA
    // crescent-core
    public static final NamespacedKey PLAYER_USERNAME = new NamespacedKey(KEY_CRESCENTCORE, "username");
    public static final NamespacedKey PLAYER_UUID = new NamespacedKey(KEY_CRESCENTCORE, "player_uuid");
    public static final NamespacedKey PLAYER_FIRST_LOGIN = new NamespacedKey(KEY_CRESCENTCORE, "first_login");
    public static final NamespacedKey PLAYER_LAST_SEEN = new NamespacedKey(KEY_CRESCENTCORE, "last_seen");
    // jumpwarps
    public static final NamespacedKey PLAYER_JUMPWARPS_USED = new NamespacedKey(KEY_JUMPWARPS, "jumpwarps_used");
    // crystals
    public static final NamespacedKey PLAYERS_CRYSTAL_AMOUNT = new NamespacedKey(KEY_CRYSTALS, "crystal_amount");
    public static final NamespacedKey PLAYER_SHOW_CRYSTALIX = new NamespacedKey(KEY_CRYSTALS, "show_crystalix");

    // SERVER DATA
    // crescent-core
    public static final NamespacedKey CRESCENTCORE_SERVER_STATS = new NamespacedKey(KEY_CRESCENTCORE, "server_stats");
    // jumpwarps
    public static final NamespacedKey JUMPWARPS = new NamespacedKey(KEY_JUMPWARPS, "jumpwarps");
    public static final NamespacedKey JUMPWARPS_STATS = new NamespacedKey(KEY_JUMPWARPS, "stats");
    // crystals
    public static final NamespacedKey CRYSTALS_STATS = new NamespacedKey(KEY_CRYSTALS, "stats");

}
