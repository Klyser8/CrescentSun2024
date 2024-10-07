package it.crescentsun.jumpwarps.lang;

import it.crescentsun.crescentmsg.api.CrescentHexCodes;
import it.crescentsun.crescentmsg.api.lang.TranslatableMessage;
import net.kyori.adventure.text.Component;

import java.util.Locale;

public class JumpWarpLocalization {

    public static final TranslatableMessage UNKNOWN_USAGE = new TranslatableMessage();
    public static final TranslatableMessage HELP_LINE_1 = new TranslatableMessage();
    public static final TranslatableMessage HELP_LINE_2 = new TranslatableMessage();
    public static final TranslatableMessage HELP_LINE_3 = new TranslatableMessage();
    public static final TranslatableMessage HELP_LINE_4 = new TranslatableMessage();

    public static final TranslatableMessage ERROR_ONLY_PLAYERS = new TranslatableMessage();
    public static final TranslatableMessage ERROR_SPECIFY_JW_NAME = new TranslatableMessage();
    public static final TranslatableMessage ERROR_JW_ALREADY_EXISTS = new TranslatableMessage();
    public static final TranslatableMessage ERROR_JW_DOES_NOT_EXIST = new TranslatableMessage();
    public static final TranslatableMessage ERROR_SPECIFY_SERVER = new TranslatableMessage();
    public static final TranslatableMessage ERROR_UNKNOWN_SERVER = new TranslatableMessage();
    public static final TranslatableMessage ERROR_GENERIC = new TranslatableMessage();
    public static final TranslatableMessage ERROR_CANNOT_TELEPORT = new TranslatableMessage();

    public static final TranslatableMessage JW_CREATED_SUCCESSFULLY = new TranslatableMessage();
    public static final TranslatableMessage JW_DELETED_SUCCESSFULLY = new TranslatableMessage();

    public static final TranslatableMessage LIST_TITLE_SERVER = new TranslatableMessage();
    public static final TranslatableMessage LIST_HEADER_SERVER = new TranslatableMessage();
    public static final TranslatableMessage LIST_ROW_SERVER = new TranslatableMessage();

    public static final TranslatableMessage LIST_TITLE_NETWORK = new TranslatableMessage();
    public static final TranslatableMessage LIST_HEADER_NETWORK = new TranslatableMessage();
    public static final TranslatableMessage LIST_ROW_NETWORK = new TranslatableMessage();

    public static final TranslatableMessage NO_SAFE_LOCATION_FOUND = new TranslatableMessage();

    public void registerEnglishTranslations() {
        UNKNOWN_USAGE.addTranslation(
                Locale.US,
                CrescentHexCodes.DARK_RED + "Unknown usage. Type " +
                        CrescentHexCodes.SALMON + "\"/jw help\"" +
                        CrescentHexCodes.DARK_RED + " for help.");
        HELP_LINE_1.addTranslation(
                Locale.US,
                CrescentHexCodes.AQUA + "[JumpWarps Help] (" +
                        CrescentHexCodes.WHITE + "/jw help" +
                        CrescentHexCodes.AQUA + ")");
        HELP_LINE_2.addTranslation(
                Locale.US,
                CrescentHexCodes.AQUA + "\"" +
                        CrescentHexCodes.WHITE + "/jumpwarps create <warp_name> <target_server_name>" +
                        CrescentHexCodes.AQUA + "\": creates a new Jump Warp at the player's location.");
        HELP_LINE_3.addTranslation(
                Locale.US,
                CrescentHexCodes.AQUA + "\"" +
                        CrescentHexCodes.WHITE + "/jumpwarps delete <warp_name>" +
                        CrescentHexCodes.AQUA + "\": deletes the specified Jump Warp.");
        HELP_LINE_4.addTranslation(
                Locale.US,
                CrescentHexCodes.AQUA + "\"" +
                        CrescentHexCodes.WHITE + "/jumpwarps list" +
                        CrescentHexCodes.AQUA + "\": lists all Jump Warps in all servers.");
        ERROR_ONLY_PLAYERS.addTranslation(
                Locale.US,
                CrescentHexCodes.DARK_RED + "Only players can use this command.");
        ERROR_SPECIFY_JW_NAME.addTranslation(
                Locale.US,
                CrescentHexCodes.DARK_RED + "You must specify a Jump Warp name.");
        ERROR_JW_ALREADY_EXISTS.addTranslation(
                Locale.US,
                CrescentHexCodes.RED + "A Jump Warp called " +
                        CrescentHexCodes.SALMON + "%s" +
                        CrescentHexCodes.RED + " already exists!");
        ERROR_JW_DOES_NOT_EXIST.addTranslation(
                Locale.US,
                CrescentHexCodes.RED + "Couldn't find a Jump Warp called " +
                        CrescentHexCodes.SALMON + "%s" +
                        CrescentHexCodes.RED + ". Did you spell it right?");
        ERROR_SPECIFY_SERVER.addTranslation(
                Locale.US,
                CrescentHexCodes.DARK_RED + "You must specify a target server name.");
        ERROR_UNKNOWN_SERVER.addTranslation(
                Locale.US,
                CrescentHexCodes.RED + "Couldn't find a server called " +
                        CrescentHexCodes.SALMON + "%s" +
                        CrescentHexCodes.RED + ". Did you spell it right?");
        ERROR_CANNOT_TELEPORT.addTranslation(
                Locale.US,
                CrescentHexCodes.RED + "Couldn't teleport you to the destination server. Please try again later.");
        ERROR_GENERIC.addTranslation(
                Locale.US,
                CrescentHexCodes.RED + "Something went wrong. Please try again later.");
        JW_CREATED_SUCCESSFULLY.addTranslation(
                Locale.US,
                CrescentHexCodes.GREEN + "Jump Warp \"" +
                        CrescentHexCodes.WHITE + "%s" +
                        CrescentHexCodes.GREEN + "\" created successfully!");
        JW_DELETED_SUCCESSFULLY.addTranslation(
                Locale.US,
                CrescentHexCodes.AQUA + "Jump Warp \"" +
                        CrescentHexCodes.WHITE + "%s" +
                        CrescentHexCodes.AQUA + "\" deleted successfully.");
        LIST_TITLE_NETWORK.addTranslation(
                Locale.US,
                CrescentHexCodes.NIGHT_PEARL + "- Jump Warps (" +
                        CrescentHexCodes.AQUA + "NETWORK" +
                        CrescentHexCodes.NIGHT_PEARL + ") -");
        LIST_HEADER_NETWORK.addTranslation(
                Locale.US,
                "Name | " +
                        "Server | " +
                        "Location | " +
                        "Destination");
        LIST_ROW_NETWORK.addTranslation(
                Locale.US,
                CrescentHexCodes.AQUA + "%s" + " | " +
                        CrescentHexCodes.WHITE + "%s" + " | " +
                        CrescentHexCodes.WHITE + "%s, %s, %s, %s" + " | " +
                        CrescentHexCodes.WHITE + "%s");
        LIST_TITLE_SERVER.addTranslation(
                Locale.US,
                CrescentHexCodes.NIGHT_PEARL + "- Jump Warps (" +
                        CrescentHexCodes.AQUA + "%s" +
                        CrescentHexCodes.NIGHT_PEARL + ") -");
        LIST_HEADER_SERVER.addTranslation(
                Locale.US,
                "Name | " +
                        "Location | " +
                        "Destination");
        LIST_ROW_SERVER.addTranslation(
                Locale.US,
                CrescentHexCodes.AQUA + "%s" + " | " +
                        CrescentHexCodes.WHITE + "%s, %s, %s, %s" + " | " +
                        CrescentHexCodes.WHITE + "%s");
        NO_SAFE_LOCATION_FOUND.addTranslation(
                Locale.US,
                CrescentHexCodes.AQUA + "Couldn't find a safe location to bring you back to. You've been teleported to your" +
                        CrescentHexCodes.WHITE + " spawn point" +
                        CrescentHexCodes.AQUA + ".");
    }

    public void registerItalianTranslations() {
        UNKNOWN_USAGE.addTranslation(
                Locale.ITALY,
                CrescentHexCodes.DARK_RED + "Uso sconosciuto. Digita " +
                        CrescentHexCodes.SALMON + "\"/jw help\"" +
                        CrescentHexCodes.DARK_RED + " per aiuto.");
        HELP_LINE_1.addTranslation(
                Locale.ITALY,
                CrescentHexCodes.AQUA + "[Aiuto JumpWarps] (" +
                        CrescentHexCodes.WHITE + "/jw help" +
                        CrescentHexCodes.AQUA + ")");
        HELP_LINE_2.addTranslation(
                Locale.ITALY,
                CrescentHexCodes.AQUA + "\"" +
                        CrescentHexCodes.WHITE + "/jumpwarps create <nome_warp> <server_destinazione>" +
                        CrescentHexCodes.AQUA + "\": crea un nuovo Jump Warp alla posizione del giocatore.");
        HELP_LINE_3.addTranslation(
                Locale.ITALY,
                CrescentHexCodes.AQUA + "\"" +
                        CrescentHexCodes.WHITE + "/jumpwarps delete <nome_warp>" +
                        CrescentHexCodes.AQUA + "\": elimina il Jump Warp specificato.");
        HELP_LINE_4.addTranslation(
                Locale.ITALY,
                CrescentHexCodes.AQUA + "\"" +
                        CrescentHexCodes.WHITE + "/jumpwarps list" +
                        CrescentHexCodes.AQUA + "\": elenca tutti i Jump Warp in tutti i server.");
        ERROR_ONLY_PLAYERS.addTranslation(
                Locale.ITALY,
                CrescentHexCodes.DARK_RED + "Solo i giocatori possono usare questo comando.");
        ERROR_SPECIFY_JW_NAME.addTranslation(
                Locale.ITALY,
                CrescentHexCodes.DARK_RED + "Devi specificare un nome per il Jump Warp.");
        ERROR_JW_ALREADY_EXISTS.addTranslation(
                Locale.ITALY,
                CrescentHexCodes.RED + "Un Jump Warp chiamato " +
                        CrescentHexCodes.SALMON + "%s" +
                        CrescentHexCodes.RED + " esiste già!");
        ERROR_JW_DOES_NOT_EXIST.addTranslation(
                Locale.ITALY,
                CrescentHexCodes.RED + "Non è stato trovato un Jump Warp chiamato " +
                        CrescentHexCodes.SALMON + "%s" +
                        CrescentHexCodes.RED + ". Hai scritto correttamente?");
        ERROR_SPECIFY_SERVER.addTranslation(
                Locale.ITALY,
                CrescentHexCodes.DARK_RED + "Devi specificare un nome per il server di destinazione.");
        ERROR_UNKNOWN_SERVER.addTranslation(
                Locale.ITALY,
                CrescentHexCodes.RED + "Non è stato trovato un server chiamato " +
                        CrescentHexCodes.SALMON + "%s" +
                        CrescentHexCodes.RED + ". Hai scritto correttamente?");
        ERROR_CANNOT_TELEPORT.addTranslation(
                Locale.ITALY,
                CrescentHexCodes.RED + "Non è stato possibile teletrasportarti al server di destinazione. Riprova più tardi.");
        ERROR_GENERIC.addTranslation(
                Locale.ITALY,
                CrescentHexCodes.RED + "Qualcosa è andato storto. Riprova più tardi.");
        JW_CREATED_SUCCESSFULLY.addTranslation(
                Locale.ITALY,
                CrescentHexCodes.GREEN + "Jump Warp \"" +
                        CrescentHexCodes.WHITE + "%s" +
                        CrescentHexCodes.GREEN + "\" creato con successo!");
        JW_DELETED_SUCCESSFULLY.addTranslation(
                Locale.ITALY,
                CrescentHexCodes.AQUA + "Jump Warp \"" +
                        CrescentHexCodes.WHITE + "%s" +
                        CrescentHexCodes.AQUA + "\" eliminato con successo.");
        LIST_TITLE_NETWORK.addTranslation(
                Locale.ITALY,
                CrescentHexCodes.NIGHT_PEARL + "- Jump Warps (" +
                        CrescentHexCodes.AQUA + "NETWORK" +
                        CrescentHexCodes.NIGHT_PEARL + ") -");
        LIST_HEADER_NETWORK.addTranslation(
                Locale.ITALY,
                "Nome | " +
                        "Server | " +
                        "Posizione | " +
                        "Destinazione");
        LIST_ROW_NETWORK.addTranslation(
                Locale.ITALY,
                CrescentHexCodes.AQUA + "%s" + " | " +
                        CrescentHexCodes.WHITE + "%s" + " | " +
                        CrescentHexCodes.WHITE + "%s, %s, %s, %s" + " | " +
                        CrescentHexCodes.WHITE + "%s");
        LIST_TITLE_SERVER.addTranslation(
                Locale.ITALY,
                CrescentHexCodes.NIGHT_PEARL + "- Jump Warps (" +
                        CrescentHexCodes.AQUA + "%s" +
                        CrescentHexCodes.NIGHT_PEARL + ") -");
        LIST_HEADER_SERVER.addTranslation(
                Locale.ITALY,
                "Nome | " +
                        "Posizione | " +
                        "Destinazione");
        LIST_ROW_SERVER.addTranslation(
                Locale.ITALY,
                CrescentHexCodes.AQUA + "%s" + " | " +
                        CrescentHexCodes.WHITE + "%s, %s, %s, %s" + " | " +
                        CrescentHexCodes.WHITE + "%s");
        NO_SAFE_LOCATION_FOUND.addTranslation(
                Locale.ITALY,
                CrescentHexCodes.AQUA + "Non è stato possibile trovare una posizione sicura per riportarti indietro. Sei stato teletrasportato al tuo" +
                        CrescentHexCodes.WHITE + " punto di spawn" +
                        CrescentHexCodes.AQUA + ".");
    }

}
