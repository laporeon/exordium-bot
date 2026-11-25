package com.laporeon.javoid.listeners;

import com.laporeon.javoid.commands.PrefixCommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CommandListener extends ListenerAdapter {
    private static final Logger log = LoggerFactory.getLogger(CommandListener.class);
    private static final String PREFIX = "!";
    private static final String HELP_COMMAND = "ajuda";
    private final Map<String, PrefixCommand> commands;

    public CommandListener(List<PrefixCommand> prefixCommands) {
        this.commands = prefixCommands.stream()
                                      .collect(Collectors.toMap(
                                              prefixCommand -> prefixCommand.getName().toLowerCase(Locale.ROOT),
                                        Function.identity()
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
            event.getMessage().reply("Não foi possível rodar o comando no momento. Aguarde uns segundos e tente novamente.").queue();
        }
    }

    private void sendHelp(MessageReceivedEvent event) {
        String descriptions = commands.values()
                                      .stream()
                                      .sorted((first, second) -> first.getName().compareToIgnoreCase(second.getName()))
                                      .map(prefixCommand -> "`!" + prefixCommand.getName() + "` — " + prefixCommand.getDescription())
                                      .collect(Collectors.joining("\n"));

        String helpMessage = "**Comandos disponíveis: **\n\n"
                + "`!help` — Mostra essa lista.\n"
                + descriptions;

        event.getChannel().sendMessage(helpMessage).queue();
    }
}
