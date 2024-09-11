package it.crescentsun.crescentcore.core.lang;

import it.crescentsun.crescentmsg.api.MessageType;
import it.crescentsun.crescentmsg.api.lang.TranslatableMessage;
import net.kyori.adventure.text.Component;

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

    public static final TranslatableMessage SAVING_PLAYER_DATA = new TranslatableMessage();
    public static final TranslatableMessage SAVING_PLAYER_DATA_SUCCESS = new TranslatableMessage();
    public static final TranslatableMessage SAVING_PLAYER_DATA_FAILURE = new TranslatableMessage();

    public static final TranslatableMessage RELOADING_DATA_SUCCESS = new TranslatableMessage();
    public static final TranslatableMessage RELOADING_DATA_FAILURE = new TranslatableMessage();

    public void registerEnglishTranslations() {
        GENERIC_INCORRECT_COMMAND.addTranslation(
                Locale.US,
                Component.text("<@dark_red>Incorrect command usage. Type </@><@salmon>\"%s\"</@> <@dark_red>for help.</@>"));
        SERVER_JOIN_MESSAGE_PLAYER.addTranslation(
                Locale.US,
                Component.text("<@aqua>You've joined</@><@white> %s</@><@aqua>.</@>")
        );
        SERVER_JOIN_MESSAGE_OTHER.addTranslation(
                Locale.US,
                Component.text("<@white>%s</@> <@yellow>has joined the server.</@>")
        );
        GENERIC_CONSOLE_INVALID.addTranslation(
                Locale.US,
                Component.text("<@dark_red>Only players can run this command.</@>")
        );
        GENERIC_SPECIFY_SERVER_NAME.addTranslation(
                Locale.US,
                Component.text("<@dark_red>You must specify a server name.</@>")
        );
        GENERIC_AWAIT_TELEPORTATION.addTranslation(
                Locale.US,
                Component.text("<@yellow>You're being teleported to</@> <@white>%s</@><@yellow>, please wait.</@>")
        );
        GENERIC_TELEPORTATION_CANCELLED.addTranslation(
                Locale.US,
                Component.text("<@red>Teleportation cancelled.</@>")
        );
        SAVING_PLAYER_DATA.addTranslation(
                Locale.US,
                Component.text("<@yellow>Saving all player data...</@>")
        );
        SAVING_PLAYER_DATA_SUCCESS.addTranslation(
                Locale.US,
                Component.text("<@green>Player data saved successfully.</@>")
        );
        SAVING_PLAYER_DATA_FAILURE.addTranslation(
                Locale.US,
                Component.text("<@red>Failed to save player data. Check the console for more information.</@>")
        );
        RELOADING_DATA_FAILURE.addTranslation(
                Locale.US,
                Component.text("<@red>Failed to reload data. Check the console for more information.</@>")
        );
        RELOADING_DATA_SUCCESS.addTranslation(
                Locale.US,
                Component.text("<@green>Data reloaded from Database successfully.</@>")
        );
        GENERIC_ALREADY_CONNECTED_TO_SERVER.addTranslation(
                Locale.US,
                Component.text("<@red>You are already connected to</@> <@salmon>%s</@><@red>!</@>")
        );
    }

    public void registerItalianTranslations() {
        GENERIC_INCORRECT_COMMAND.addTranslation(
                Locale.ITALY,
                Component.text("<@dark_red>Utilizzo del comando errato. Digitare</@> <@salmon>\"%s\"</@> <@dark_red>per ottenere aiuto.</@>")
        );
        SERVER_JOIN_MESSAGE_PLAYER.addTranslation(
                Locale.ITALY,
                Component.text("<@aqua>Sei entrato in</@> <@white>%s</@white><@aqua>.</@>")
        );
        SERVER_JOIN_MESSAGE_OTHER.addTranslation(
                Locale.ITALY,
                Component.text("<@white>%s</@> <@yellow>è entrato nel server.</@>")
        );
        GENERIC_SPECIFY_SERVER_NAME.addTranslation(
                Locale.ITALY,
                Component.text("<@dark_red>Devi specificare il nome di un server.</@>")
        );
        GENERIC_AWAIT_TELEPORTATION.addTranslation(
                Locale.ITALY,
                Component.text("<@yellow>Stai per essere teletrasportato a</@> <@white>%s</@><@yellow>, attendi.</@>")
        );
        GENERIC_TELEPORTATION_CANCELLED.addTranslation(
                Locale.ITALY,
                Component.text("<@red>Teletrasporto annullato.</@>")
        );
        SAVING_PLAYER_DATA.addTranslation(
                Locale.ITALY,
                Component.text("<@yellow>Salvataggio di tutti i dati dei giocatori...</@>")
        );
        SAVING_PLAYER_DATA_SUCCESS.addTranslation(
                Locale.ITALY,
                Component.text("<@green>Dati dei giocatori salvati con successo.</@>")
        );
        RELOADING_DATA_FAILURE.addTranslation(
                Locale.ITALY,
                Component.text("<@red>Impossibile ricaricare i dati dei plugin. Controlla la console per ulteriori informazioni.</@>")
        );
        RELOADING_DATA_SUCCESS.addTranslation(
                Locale.ITALY,
                Component.text("<@green>Dati dei plugin ricaricati dal Database con successo.</@>")
        );
        SAVING_PLAYER_DATA_FAILURE.addTranslation(
                Locale.ITALY,
                Component.text("<@red>Impossibile salvare i dati dei plugin. Controlla la console per ulteriori informazioni.</@>")
        );
        GENERIC_ALREADY_CONNECTED_TO_SERVER.addTranslation(
                Locale.ITALY,
                Component.text("<@red>Sei già connesso a</@> <@salmon>%s</@><@red>!</@>")
        );
    }
}
