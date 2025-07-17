package it.crescentsun.api.crescentcore;

import java.util.List;

/**
 * The main API interface for CrescentCore.
 * Here are miscellaneous methods that may be used to interact with the plugin.
 */
public interface CrescentCoreAPI {

    /**
     * Gets the current server's name.
     *
     * @return the server name
     */
    String getServerName();

    /**
     * Gets the list of servers.
     *
     * @return an immutable list of server names
     */
    List<String> getServerList();

}
