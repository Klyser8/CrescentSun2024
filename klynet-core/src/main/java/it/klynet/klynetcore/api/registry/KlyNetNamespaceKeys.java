package it.klynet.klynetcore.api.registry;

import org.bukkit.NamespacedKey;

@SuppressWarnings("unused")
public class KlyNetNamespaceKeys {

    public static final String SERVER_KEY = "server";
    public static final NamespacedKey SERVER_TOTAL_CRYSTALS = new NamespacedKey(SERVER_KEY, "total_crystals");
    protected static final String SETTINGS_KEY = "settings";
    public static final NamespacedKey SETTINGS_SHOW_CRYSTALIX = new NamespacedKey(SETTINGS_KEY, "show_crystalix");
    protected static final String KLYNETCORE_KEY = "klynetcore";
    public static final NamespacedKey PLAYER_USERNAME = new NamespacedKey(KLYNETCORE_KEY, "username");
    public static final NamespacedKey PLAYER_UUID = new NamespacedKey(KLYNETCORE_KEY, "player_uuid");
    public static final NamespacedKey PLAYER_FIRST_LOGIN = new NamespacedKey(KLYNETCORE_KEY, "first_login");
    public static final NamespacedKey PLAYER_LAST_SEEN = new NamespacedKey(KLYNETCORE_KEY, "last_seen");
    protected static final String JUMPWARPS_KEY = "jumpwarps";
    public static final NamespacedKey JUMPWARPS_USED = new NamespacedKey(JUMPWARPS_KEY, "jumpwarps_used");
    protected static final String CRYSTALS_KEY = "crystals";
    public static final NamespacedKey CRYSTALS_AMOUNT = new NamespacedKey(CRYSTALS_KEY, "crystals_amount");

}
