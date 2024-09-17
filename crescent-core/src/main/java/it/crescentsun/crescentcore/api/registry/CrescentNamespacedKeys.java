package it.crescentsun.crescentcore.api.registry;

import org.bukkit.NamespacedKey;

@SuppressWarnings("unused")
public class CrescentNamespacedKeys {

    // KEY NAMES
    public static final String NAMESPACE_CRESCENTCORE = "crescentcore";
    public static final String NAMESPACE_JUMPWARPS = "jumpwarps";
    public static final String NAMESPACE_CRYSTALS = "crystals";
    public static final String NAMESPACE_ARTIFACTS = "artifacts";

    // PLAYER DATA
    // crescent-core
    public static final NamespacedKey PLAYER_USERNAME = new NamespacedKey(NAMESPACE_CRESCENTCORE, "username");
    public static final NamespacedKey PLAYER_UUID = new NamespacedKey(NAMESPACE_CRESCENTCORE, "player_uuid");
    public static final NamespacedKey PLAYER_FIRST_LOGIN = new NamespacedKey(NAMESPACE_CRESCENTCORE, "first_login");
    public static final NamespacedKey PLAYER_LAST_SEEN = new NamespacedKey(NAMESPACE_CRESCENTCORE, "last_seen");
    // jumpwarps
    public static final NamespacedKey PLAYER_JUMPWARPS_USED = new NamespacedKey(NAMESPACE_JUMPWARPS, "jumpwarps_used");
    // crystals
    public static final NamespacedKey PLAYERS_CRYSTAL_AMOUNT = new NamespacedKey(NAMESPACE_CRYSTALS, "crystal_amount");
    public static final NamespacedKey PLAYER_SHOW_CRYSTALIX = new NamespacedKey(NAMESPACE_CRYSTALS, "show_crystalix");

    // SERVER DATA
    // jumpwarps
    public static final NamespacedKey JUMPWARPS = new NamespacedKey(NAMESPACE_JUMPWARPS, "jumpwarps");
    public static final NamespacedKey JUMPWARPS_STATS = new NamespacedKey(NAMESPACE_JUMPWARPS, "stats");
    // crystals
    public static final NamespacedKey CRYSTALS_STATS = new NamespacedKey(NAMESPACE_CRYSTALS, "stats");

}
