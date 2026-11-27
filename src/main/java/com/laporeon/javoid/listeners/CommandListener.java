package com.laporeon.javoid.listeners;

import com.laporeon.javoid.commands.BotCommand;
import com.laporeon.javoid.helpers.BundleManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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
    private final List<BotCommand> botCommands;
    private static final ErrorHandler IGNORE_UNKNOWN_MESSAGE =
            new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE);

    public CommandListener(List<BotCommand> botCommands) {
        this.botCommands = botCommands.stream()
                                      .sorted(Comparator.comparing(BotCommand::getName, String.CASE_INSENSITIVE_ORDER))
                                      .toList();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || !event.isFromGuild()) return;

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

        botCommands.stream()
                   .filter(command -> command.getName().equalsIgnoreCase(commandName))
                   .findFirst()
                   .ifPresent(command -> execute(command, event, args));
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String commandName = event.getName();

        if (HELP_COMMAND.equals(commandName)) {
            event.reply(helpMessage("/")).queue();
            return;
        }

        botCommands.stream()
                   .filter(command -> command.getName().equalsIgnoreCase(commandName))
                   .findFirst()
                   .ifPresent(command -> execute(command, event));
    }

    public List<SlashCommandData> slashCommandData() {
        List<SlashCommandData> data = new ArrayList<>();
        data.add(Commands.slash(HELP_COMMAND, BundleManager.get("help.self")));
        botCommands.forEach(cmd -> data.add(
                Commands.slash(cmd.getName(), cmd.getDescription()).addOptions(cmd.getOptions())));
        return data;
    }

    private void execute(BotCommand command, SlashCommandInteractionEvent event) {
        try {
            command.execute(event);
        } catch (Exception e) {
            log.error("Slash command '{}' failed for user {}", command.getName(), event.getUser().getId(), e);
            String text = BundleManager.get("command.failure", FAILURE_MESSAGE_TIMEOUT);
            if (event.isAcknowledged()) {
                event.getHook().sendMessage(text).setEphemeral(true).queue();
            } else {
                event.reply(text).setEphemeral(true).queue();
            }
        }
    }

    private void execute(BotCommand command, MessageReceivedEvent event, String[] args) {
        try {
            command.execute(event, args);
        } catch (Exception e) {
            log.error("BotCommand '{}' failed for user {}", command.getName(), event.getAuthor().getId(), e);
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
        event.getChannel().sendMessage(helpMessage(PREFIX)).queue();
    }

    private String helpMessage(String prefix) {
        String descriptions = botCommands.stream()
                                         .map(cmd -> "`" + prefix + cmd.getName() + "` — " + cmd.getDescription())
                                         .collect(Collectors.joining("\n"));

        return BundleManager.get("help.title") + "\n\n"
                + "`" + prefix + HELP_COMMAND + "` — " + BundleManager.get("help.self") + "\n"
                + descriptions;
    }
}
