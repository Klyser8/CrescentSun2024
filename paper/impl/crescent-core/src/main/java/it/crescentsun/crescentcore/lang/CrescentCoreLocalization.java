package it.crescentsun.crescentcore.lang;

import it.crescentsun.crescentmsg.api.CrescentHexCodes;
import it.crescentsun.crescentmsg.api.lang.TranslatableMessage;

import java.util.Locale;

public class CrescentCoreLocalization {

    public static final TranslatableMessage GENERIC_INCORRECT_COMMAND = new TranslatableMessage();
    public static final TranslatableMessage GENERIC_CONSOLE_INVALID = new TranslatableMessage();

    public static final TranslatableMessage SERVER_JOIN_MESSAGE_PLAYER = new TranslatableMessage();
    public static final TranslatableMessage SERVER_JOIN_MESSAGE_OTHER = new TranslatableMessage();

    public static final TranslatableMessage GENERIC_SPECIFY_SERVER_NAME = new TranslatableMessage();
    public static final TranslatableMessage GENERIC_ALREADY_CONNECTED_TO_SERVER = new TranslatableMessage();


    public static final TranslatableMessage GENERIC_AWAIT_TELEPORTATION = new TranslatableMessage();
    public static final TranslatableMessage GENERIC_TELEPORTATION_CANCELLED = new TranslatableMessage();
    public static final TranslatableMessage GENERIC_TELEPORTATION_FAILURE = new TranslatableMessage();

    public static final TranslatableMessage SAVING_PLAYER_DATA = new TranslatableMessage();
    public static final TranslatableMessage SAVING_PLAYER_DATA_SUCCESS = new TranslatableMessage();
    public static final TranslatableMessage SAVING_PLAYER_DATA_FAILURE = new TranslatableMessage();

    public static final TranslatableMessage RELOADING_DATA_SUCCESS = new TranslatableMessage();
    public static final TranslatableMessage RELOADING_DATA_FAILURE = new TranslatableMessage();

    public void registerEnglishTranslations() {
        GENERIC_INCORRECT_COMMAND.addTranslation(
                Locale.US,
                CrescentHexCodes.DARK_RED + "Incorrect command usage. Type " + CrescentHexCodes.SALMON + "\"%s\" " + CrescentHexCodes.DARK_RED + "for help.");
        SERVER_JOIN_MESSAGE_PLAYER.addTranslation(
                Locale.US,
                CrescentHexCodes.AQUA + "You've joined " + CrescentHexCodes.WHITE + "%s" + CrescentHexCodes.AQUA + ".");
        SERVER_JOIN_MESSAGE_OTHER.addTranslation(
                Locale.US,
                CrescentHexCodes.WHITE + "%s " + CrescentHexCodes.GOLD + "has joined the server."
        );
        GENERIC_CONSOLE_INVALID.addTranslation(
                Locale.US,
                CrescentHexCodes.DARK_RED + "Only players can run this command."
        );
        GENERIC_SPECIFY_SERVER_NAME.addTranslation(
                Locale.US,
                CrescentHexCodes.DARK_RED + "You must specify a server name."
        );
        GENERIC_AWAIT_TELEPORTATION.addTranslation(
                Locale.US,
                CrescentHexCodes.YELLOW + "You're being teleported to " + CrescentHexCodes.WHITE + "%s" + CrescentHexCodes.YELLOW + ", please wait."
        );
        GENERIC_TELEPORTATION_CANCELLED.addTranslation(
                Locale.US,
                CrescentHexCodes.RED + "Teleportation cancelled."
        );
        GENERIC_TELEPORTATION_FAILURE.addTranslation(
                Locale.US,
                CrescentHexCodes.RED + "Failed to teleport to " + CrescentHexCodes.SALMON + "%s" + CrescentHexCodes.RED + "!"
        );
        SAVING_PLAYER_DATA.addTranslation(
                Locale.US,
                CrescentHexCodes.YELLOW + "Saving all player data..."
        );
        SAVING_PLAYER_DATA_SUCCESS.addTranslation(
                Locale.US,
                CrescentHexCodes.GREEN + "Player data saved successfully."
        );
        SAVING_PLAYER_DATA_FAILURE.addTranslation(
                Locale.US,
                CrescentHexCodes.RED + "Failed to save player data. Check the console for more information."
        );
        RELOADING_DATA_FAILURE.addTranslation(
                Locale.US,
                CrescentHexCodes.RED + "Failed to reload data. Check the console for more information."
        );
        RELOADING_DATA_SUCCESS.addTranslation(
                Locale.US,
                CrescentHexCodes.GREEN + "Data reloaded from Database successfully."
        );
        GENERIC_ALREADY_CONNECTED_TO_SERVER.addTranslation(
                Locale.US,
                CrescentHexCodes.RED + "You are already connected to " + CrescentHexCodes.SALMON + "%s" + CrescentHexCodes.RED + "!"
        );
    }

    public void registerItalianTranslations() {
        GENERIC_INCORRECT_COMMAND.addTranslation(
                Locale.ITALY,
                CrescentHexCodes.DARK_RED + "Utilizzo del comando errato. Digitare " + CrescentHexCodes.SALMON + "\"%s\" " + CrescentHexCodes.DARK_RED + "per ottenere aiuto."
        );
        SERVER_JOIN_MESSAGE_PLAYER.addTranslation(
                Locale.ITALY,
                CrescentHexCodes.AQUA + "Sei entrato in " + CrescentHexCodes.WHITE + "%s" + CrescentHexCodes.AQUA + "."
        );
        SERVER_JOIN_MESSAGE_OTHER.addTranslation(
                Locale.ITALY,
                CrescentHexCodes.WHITE + "%s " + CrescentHexCodes.YELLOW + "è entrato nel server."
        );
        GENERIC_SPECIFY_SERVER_NAME.addTranslation(
                Locale.ITALY,
                CrescentHexCodes.DARK_RED + "Devi specificare il nome di un server."
        );
        GENERIC_AWAIT_TELEPORTATION.addTranslation(
                Locale.ITALY,
                CrescentHexCodes.YELLOW + "Stai per essere teletrasportato a " + CrescentHexCodes.WHITE + "%s" + CrescentHexCodes.YELLOW + ", attendi."
        );
        GENERIC_TELEPORTATION_CANCELLED.addTranslation(
                Locale.ITALY,
                CrescentHexCodes.RED + "Teletrasporto annullato."
        );
        GENERIC_TELEPORTATION_FAILURE.addTranslation(
                Locale.ITALY,
                CrescentHexCodes.RED + "Impossibile teletrasportarsi a " + CrescentHexCodes.SALMON + "%s" + CrescentHexCodes.RED + "!"
        );
        SAVING_PLAYER_DATA.addTranslation(
                Locale.ITALY,
                CrescentHexCodes.YELLOW + "Salvataggio di tutti i dati dei giocatori..."
        );
        SAVING_PLAYER_DATA_SUCCESS.addTranslation(
                Locale.ITALY,
                CrescentHexCodes.GREEN + "Dati dei giocatori salvati con successo."
        );
        RELOADING_DATA_FAILURE.addTranslation(
                Locale.ITALY,
                CrescentHexCodes.RED + "Impossibile ricaricare i dati dei plugin. Controlla la console per ulteriori informazioni."
        );
        RELOADING_DATA_SUCCESS.addTranslation(
                Locale.ITALY,
                CrescentHexCodes.GREEN + "Dati dei plugin ricaricati dal Database con successo."
        );
        SAVING_PLAYER_DATA_FAILURE.addTranslation(
                Locale.ITALY,
                CrescentHexCodes.RED + "Impossibile salvare i dati dei plugin. Controlla la console per ulteriori informazioni."
        );
        GENERIC_ALREADY_CONNECTED_TO_SERVER.addTranslation(
                Locale.ITALY,
                CrescentHexCodes.RED + "Sei già connesso a " + CrescentHexCodes.SALMON + "%s" + CrescentHexCodes.RED + "!"
        );
    }
}
