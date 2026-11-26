package com.laporeon.javoid.listeners;

import com.laporeon.javoid.commands.PrefixCommand;
import com.laporeon.javoid.helpers.BundleManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CommandListener extends ListenerAdapter {
    private static final Logger log = LoggerFactory.getLogger(CommandListener.class);
    private static final String PREFIX = "!";
    private static final String HELP_COMMAND = "ajuda";
    private static final int FAILURE_MESSAGE_TIMEOUT = 15;
    private final List<PrefixCommand> prefixCommands;
    private static final ErrorHandler IGNORE_UNKNOWN_MESSAGE =
            new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE);

    public CommandListener(List<PrefixCommand> prefixCommands) {
        this.prefixCommands = prefixCommands.stream()
                                            .sorted(Comparator.comparing(PrefixCommand::getName, String.CASE_INSENSITIVE_ORDER))
                                            .toList();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        String content = event.getMessage().getContentRaw().strip();
        if (!content.startsWith(PREFIX)) {
            return;
        }

        String[] args = content.substring(PREFIX.length()).strip().split("\\s+");
        String commandName = args[0].toLowerCase(Locale.ROOT);

        if (HELP_COMMAND.equals(commandName)) {
            sendHelp(event);
            return;
        }

        prefixCommands.stream()
                      .filter(command -> command.getName().equalsIgnoreCase(commandName))
                      .findFirst()
                      .ifPresent(command -> execute(command, event, args));
    }

    private void execute(PrefixCommand command, MessageReceivedEvent event, String[] args) {
        try {
            command.execute(event, args);
        } catch (Exception e) {
            log.error("PrefixCommand '{}' failed for user {}", command.getName(), event.getAuthor().getId(), e);
            event.getMessage()
                 .reply(BundleManager.get("command.failure", FAILURE_MESSAGE_TIMEOUT))
                 .queue(reply -> reply.delete().queueAfter(
                         FAILURE_MESSAGE_TIMEOUT,
                         TimeUnit.SECONDS,
                         null,
                         IGNORE_UNKNOWN_MESSAGE
                 ));
        }
    }

    private void sendHelp(MessageReceivedEvent event) {
        String descriptions = prefixCommands.stream()
                                            .map(cmd -> "`" + PREFIX + cmd.getName() + "` — " + cmd.getDescription())
                                            .collect(Collectors.joining("\n"));

        String helpMessage = BundleManager.get("help.title") + "\n\n"
                + "`" + PREFIX + HELP_COMMAND + "` — " + BundleManager.get("help.self") + "\n"
                + descriptions;

        event.getChannel().sendMessage(helpMessage).queue();
    }
}
