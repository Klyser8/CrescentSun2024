package it.crescentsun.jumpwarps.lang;

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
                Component.text("<@dark_red>Unknown usage. Type</@>" +
                        " <@salmon>\"/jw help\"</@>" +
                        " <@dark_red>for help.</@>"));
        HELP_LINE_1.addTranslation(
                Locale.US,
                Component.text("<@aqua>[JumpWarps Help] (</@>" +
                        "<@white>/jw help</@>" +
                        "<@aqua>)</@>"));
        HELP_LINE_2.addTranslation(
                Locale.US,
                Component.text("<@aqua>\"</@>" +
                        "<@white>/jumpwarps create <warp_name> <target_server_name></@>" +
                        "<@aqua>\": creates a new Jump Warp at the player's location.</@>"));
        HELP_LINE_3.addTranslation(
                Locale.US,
                Component.text("<@aqua>\"</@>" +
                        "<@white>/jumpwarps delete <warp_name></@>" +
                        "<@aqua>\": deletes the specified Jump Warp.</@>"));
        HELP_LINE_4.addTranslation(
                Locale.US,
                Component.text("<@aqua>\"</@>" +
                        "<@white>/jumpwarps list</@>" +
                        "<@aqua>\": lists all Jump Warps in all servers.</@>"));
        ERROR_ONLY_PLAYERS.addTranslation(
                Locale.US,
                Component.text("<@dark_red>Only players can use this command.</@>"));
        ERROR_SPECIFY_JW_NAME.addTranslation(
                Locale.US,
                Component.text("<@dark_red>You must specify a Jump Warp name.</@>"));
        ERROR_JW_ALREADY_EXISTS.addTranslation(
                Locale.US,
                Component.text("<@red>A Jump Warp called </@>" +
                        "<@salmon>%s</@>" +
                        "<@red> already exists!</@>"));
        ERROR_JW_DOES_NOT_EXIST.addTranslation(
                Locale.US,
                Component.text("<@red>Couldn't find a Jump Warp called </@>" +
                        "<@salmon>%s</@>" +
                        "<@red>. Did you spell it right?</@>"));
        ERROR_SPECIFY_SERVER.addTranslation(
                Locale.US,
                Component.text("<@dark_red>You must specify a target server name.</@>"));
        ERROR_UNKNOWN_SERVER.addTranslation(
                Locale.US,
                Component.text("<@red>Couldn't find a server called </@>" +
                        "<@salmon>%s</@>" +
                        "<@red>. Did you spell it right?</@>"));
        ERROR_GENERIC.addTranslation(
                Locale.US,
                Component.text("<@red>Something went wrong. Please try again later.</@>"));
        JW_CREATED_SUCCESSFULLY.addTranslation(
                Locale.US,
                Component.text("<@green>Jump Warp \"</@>" +
                        "<@white>%s</@>" +
                        "<@green>\" created successfully!</@>"));
        JW_DELETED_SUCCESSFULLY.addTranslation(
                Locale.US,
                Component.text("<@aqua>Jump Warp \"</@>" +
                        "<@white>%s</@>" +
                        "<@aqua>\" deleted successfully.</@>"));
        LIST_TITLE_NETWORK.addTranslation(
                Locale.US,
                Component.text("<@night_pearl>- Jump Warps (</@>" +
                        "<@aqua>NETWORK</@>)" +
                        "<@night_pearl> -</@>"));
        LIST_HEADER_NETWORK.addTranslation(
                Locale.US,
                Component.text("Name | " +
                        "Server | " +
                        "Location | " +
                        "Destination"));
        LIST_ROW_NETWORK.addTranslation(
                Locale.US,
                Component.text("<@aqua>%s</@> | " +
                        "<@white>%s</@> | " +
                        "<@white>%s, %s, %s, %s</@> | " +
                        "<@white>%s</@>"));;

        LIST_TITLE_SERVER.addTranslation(
                Locale.US,
                Component.text("<@night_pearl>- Jump Warps (</@>" +
                        "<@aqua>%s</@>)" +
                        "<@night_pearl> -</@>"));
        LIST_HEADER_SERVER.addTranslation(
                Locale.US,
                Component.text("Name | " +
                        "Location | " +
                        "Destination"));
        LIST_ROW_SERVER.addTranslation(
                Locale.US,
                Component.text("<@aqua>%s</@> | " +
                        "<@white>%s, %s, %s, %s</@> | " +
                        "<@white>%s</@>"));

        NO_SAFE_LOCATION_FOUND.addTranslation(
                Locale.US,
                Component.text("<@aqua>Couldn't find a safe location to bring you back to. You've been teleported to your</@>" +
                        "<@white> spawn point</@>" +
                        "<@aqua>.</@>"));
    }

    public void registerItalianTranslations() {
    UNKNOWN_USAGE.addTranslation(
            Locale.ITALY,
            Component.text("<@dark_red>Uso sconosciuto. Digita</@>" +
                    " <@salmon>\"/jw help\"</@>" +
                    " <@dark_red>per aiuto.</@>"));
    HELP_LINE_1.addTranslation(
            Locale.ITALY,
            Component.text("<@aqua>[Aiuto JumpWarps] (</@>" +
                    "<@white>/jw help</@>" +
                    "<@aqua>)</@>"));
    HELP_LINE_2.addTranslation(
            Locale.ITALY,
            Component.text("<@aqua>\"</@>" +
                    "<@white>/jumpwarps create <nome_warp> <server_destinazione></@>" +
                    "<@aqua>\": crea un nuovo Jump Warp alla posizione del giocatore.</@>"));
    HELP_LINE_3.addTranslation(
            Locale.ITALY,
            Component.text("<@aqua>\"</@>" +
                    "<@white>/jumpwarps delete <nome_warp></@>" +
                    "<@aqua>\": elimina il Jump Warp specificato.</@>"));
    HELP_LINE_4.addTranslation(
            Locale.ITALY,
            Component.text("<@aqua>\"</@>" +
                    "<@white>/jumpwarps list</@>" +
                    "<@aqua>\": elenca tutti i Jump Warp in tutti i server.</@>"));
    ERROR_ONLY_PLAYERS.addTranslation(
            Locale.ITALY,
            Component.text("<@dark_red>Solo i giocatori possono usare questo comando.</@>"));
    ERROR_SPECIFY_JW_NAME.addTranslation(
            Locale.ITALY,
            Component.text("<@dark_red>Devi specificare un nome per il Jump Warp.</@>"));
    ERROR_JW_ALREADY_EXISTS.addTranslation(
            Locale.ITALY,
            Component.text("<@red>Un Jump Warp chiamato </@>" +
                    "<@salmon>%s</@>" +
                    "<@red> esiste già!</@>"));
    ERROR_JW_DOES_NOT_EXIST.addTranslation(
            Locale.ITALY,
            Component.text("<@red>Non è stato trovato un Jump Warp chiamato </@>" +
                    "<@salmon>%s</@>" +
                    "<@red>. Hai scritto correttamente?</@>"));
    ERROR_SPECIFY_SERVER.addTranslation(
            Locale.ITALY,
            Component.text("<@dark_red>Devi specificare un nome per il server di destinazione.</@>"));
    ERROR_UNKNOWN_SERVER.addTranslation(
            Locale.ITALY,
            Component.text("<@red>Non è stato trovato un server chiamato </@>" +
                    "<@salmon>%s</@>" +
                    "<@red>. Hai scritto correttamente?</@>"));
    ERROR_GENERIC.addTranslation(
            Locale.ITALY,
            Component.text("<@red>Qualcosa è andato storto. Riprova più tardi.</@>"));
    JW_CREATED_SUCCESSFULLY.addTranslation(
            Locale.ITALY,
            Component.text("<@green>Jump Warp \"</@>" +
                    "<@white>%s</@>" +
                    "<@green>\" creato con successo!</@>"));
    JW_DELETED_SUCCESSFULLY.addTranslation(
            Locale.ITALY,
            Component.text("<@aqua>Jump Warp \"</@>" +
                    "<@white>%s</@>" +
                    "<@aqua>\" eliminato con successo.</@>"));
    LIST_HEADER_NETWORK.addTranslation(
            Locale.ITALY,
            Component.text("<@aqua>Jump Warp:</@>"));
    // Format: - <jumpwarp_name> (worldName, X, Y, Z) -> [<destination_server_name]
    LIST_ROW_NETWORK.addTranslation(
            Locale.ITALY,
            Component.text("<@aqua>- </@>" +
                    "<@white>%s</@>" +
                    "<@aqua> (%s, %s, %s, %s) -> </@>" +
                    "<@white>[%s]</@>"));
    LIST_HEADER_SERVER.addTranslation(
            Locale.ITALY,
            Component.text("<@aqua>Jump Warp in </@>" +
                    "<@white>%s</@>" +
                    "<@aqua>:</@>"));
    // Format: - [server_name] <jumpwarp_name> (worldName, X, Y, Z) -> [<destination_server_name]
    LIST_ROW_SERVER.addTranslation(
            Locale.ITALY,
            Component.text("<@aqua>- </@>" +
                    "<@white>[%s] %s </@>" +
                    "<@aqua>(%s, %s, %s, %s) -> </@>" +
                    "<@white>[%s]</@>"));

    NO_SAFE_LOCATION_FOUND.addTranslation(
            Locale.ITALY,
            Component.text("<@aqua>Non è stato possibile trovare una posizione sicura per riportarti in questo server. Sei stato teletrasportato al tuo</@>" +
                    "<@white> punto di spawn</@>" +
                    "<@aqua>.</@>"));
    }

}
