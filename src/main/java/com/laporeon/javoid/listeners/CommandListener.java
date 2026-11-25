package com.laporeon.javoid.listeners;

import com.laporeon.javoid.commands.PrefixCommand;
import com.laporeon.javoid.helpers.MessageManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CommandListener extends ListenerAdapter {
    private static final Logger log = LoggerFactory.getLogger(CommandListener.class);
    private static final String PREFIX = "!";
    private static final String HELP_COMMAND = "ajuda";
    private static final int FAILURE_MESSAGE_TIMEOUT = 15;
    private static final ErrorHandler IGNORE_UNKNOWN_MESSAGE =
            new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE);
    private final Map<String, PrefixCommand> commands;

    public CommandListener(List<PrefixCommand> prefixCommands) {
        this.commands = prefixCommands.stream()
                                      .collect(Collectors.toMap(
                                              prefixCommand -> prefixCommand.getName().toLowerCase(Locale.ROOT),
                                              Function.identity(),
                                              (first, second) -> {
                                                  throw new IllegalStateException("Duplicate command name: " + first.getName());
                                              },
                                              LinkedHashMap::new
                                      ));
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

        PrefixCommand prefixCommand = commands.get(commandName);
        if (prefixCommand == null) {
            return;
        }

        try {
            prefixCommand.execute(event, args);
        } catch (Exception e) {
            log.error("PrefixCommand '{}' failed for user {}", commandName, event.getAuthor().getId(), e);
            event.getMessage()
                 .reply(MessageManager.get("command.failure", FAILURE_MESSAGE_TIMEOUT))
                 .queue(reply -> reply.delete().queueAfter(
                         FAILURE_MESSAGE_TIMEOUT,
                         TimeUnit.SECONDS,
                         null,
                         IGNORE_UNKNOWN_MESSAGE
                 ));
        }
    }

    private void sendHelp(MessageReceivedEvent event) {
        String descriptions = commands.values()
                                      .stream()
                                      .map(cmd -> "`" + PREFIX + cmd.getName() + "` — " + cmd.getDescription())
                                      .collect(Collectors.joining("\n"));

        String helpMessage = MessageManager.get("help.title") + "\n\n"
                + "`" + PREFIX + HELP_COMMAND + "` — " + MessageManager.get("help.self") + "\n"
                + descriptions;

        event.getChannel().sendMessage(helpMessage).queue();
    }
}
