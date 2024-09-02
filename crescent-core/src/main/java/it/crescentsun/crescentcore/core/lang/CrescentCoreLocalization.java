package it.crescentsun.crescentcore.core.lang;

import it.crescentsun.crescentmsg.api.MessageType;
import it.crescentsun.crescentmsg.api.lang.TranslatableMessage;
import net.kyori.adventure.text.Component;

import java.util.Locale;

public class CrescentCoreLocalization {

    public static final TranslatableMessage GENERIC_INCORRECT_COMMAND = new TranslatableMessage(MessageType.INCORRECT);
    public static final TranslatableMessage GENERIC_CONSOLE_INVALID = new TranslatableMessage(MessageType.INCORRECT);

    public static final TranslatableMessage SERVER_JOIN_MESSAGE_PLAYER = new TranslatableMessage(MessageType.SUCCESS);
    public static final TranslatableMessage SERVER_JOIN_MESSAGE_OTHER = new TranslatableMessage(MessageType.INFO);

    public static final TranslatableMessage GENERIC_SPECIFY_SERVER_NAME = new TranslatableMessage(MessageType.INCORRECT);

    public static final TranslatableMessage GENERIC_AWAIT_TELEPORTATION = new TranslatableMessage(MessageType.INFO);
    public static final TranslatableMessage GENERIC_TELEPORTATION_CANCELLED = new TranslatableMessage(MessageType.ERROR);

    public static final TranslatableMessage SAVING_PLAYER_DATA = new TranslatableMessage(MessageType.INFO);
    public static final TranslatableMessage SAVING_PLAYER_DATA_SUCCESS = new TranslatableMessage(MessageType.SUCCESS);
    public static final TranslatableMessage SAVING_PLAYER_DATA_FAILURE = new TranslatableMessage(MessageType.ERROR);

    public static final TranslatableMessage TEST_MESSAGE = new TranslatableMessage(MessageType.INFO);

    public void registerEnglishTranslations() {
        GENERIC_INCORRECT_COMMAND.addTranslation(
                Locale.US,
                Component.text("Incorrect command usage. Type \"%s\" for help."),
                "%s"
        );
        SERVER_JOIN_MESSAGE_PLAYER.addTranslation(
                Locale.US,
                Component.text("You've joined %s."),
                "%s"
        );
        SERVER_JOIN_MESSAGE_OTHER.addTranslation(
                Locale.US,
                Component.text("%s has joined the server."),
                "%s"
        );
        GENERIC_CONSOLE_INVALID.addTranslation(
                Locale.US,
                Component.text("Only players can run this command.")
        );
        GENERIC_SPECIFY_SERVER_NAME.addTranslation(
                Locale.US,
                Component.text("You must specify a server name.")
        );
        GENERIC_AWAIT_TELEPORTATION.addTranslation(
                Locale.US,
                Component.text("You're being teleported to %s, please wait.")
        );
        GENERIC_TELEPORTATION_CANCELLED.addTranslation(
                Locale.US,
                Component.text("Teleportation cancelled.")
        );
        SAVING_PLAYER_DATA.addTranslation(
                Locale.US,
                Component.text("Saving all player data...")
        );
        SAVING_PLAYER_DATA_SUCCESS.addTranslation(
                Locale.US,
                Component.text("Player data saved successfully.")
        );
        SAVING_PLAYER_DATA_FAILURE.addTranslation(
                Locale.US,
                Component.text("Failed to save player data. Check the console for more information.")
        );

        TEST_MESSAGE.addTranslation(
                Locale.US,
                Component.text("Hello, %s! <b>This is a test message</b>. <#ff00ff>You are currently in world</#> %s, a wonderful world full of worlds, at coordinates %s, %s, %s."),
                "%s", "%s", "%s", "%s", "%s"
        );
    }

    public void registerItalianTranslations() {
        GENERIC_INCORRECT_COMMAND.addTranslation(
                Locale.ITALY,
                Component.text("Utilizzo del comando errato. Digitare \"%s\" per ottenere aiuto."),
                "%s"
        );
        SERVER_JOIN_MESSAGE_PLAYER.addTranslation(
                Locale.ITALY,
                Component.text("Sei entrato in %s."),
                "%s"
        );
        SERVER_JOIN_MESSAGE_OTHER.addTranslation(
                Locale.ITALY,
                Component.text("%s è entrato nel server."),
                "%s"
        );
        GENERIC_CONSOLE_INVALID.addTranslation(
                Locale.ITALY,
                Component.text("Solo i giocatori possono eseguire questo comando.")
        );
        GENERIC_SPECIFY_SERVER_NAME.addTranslation(
                Locale.ITALY,
                Component.text("Devi specificare un nome di server.")
        );
        GENERIC_AWAIT_TELEPORTATION.addTranslation(
                Locale.ITALY,
                Component.text("Stai per essere teletrasportato a %s, attendi.")
        );
        GENERIC_TELEPORTATION_CANCELLED.addTranslation(
                Locale.ITALY,
                Component.text("Teletrasporto annullato.")
        );
        SAVING_PLAYER_DATA.addTranslation(
                Locale.ITALY,
                Component.text("Salvataggio di tutti i dati dei plugin...")
        );
        SAVING_PLAYER_DATA_SUCCESS.addTranslation(
                Locale.ITALY,
                Component.text("Dati dei plugin salvati con successo.")
        );
        SAVING_PLAYER_DATA_FAILURE.addTranslation(
                Locale.ITALY,
                Component.text("Impossibile salvare i dati dei plugin. Controlla la console per ulteriori informazioni.")
        );
        TEST_MESSAGE.addTranslation(
                Locale.ITALY,
                Component.text("Ciao, %s! Questo è un messaggio di prova. Attualmente ti trovi nel mondo %s, alle coordinate %s, %s, %s."),
                "%s", "%s", "%s", "%s", "%s"
        );
    }
}
