package com.laporeon.javoid.commands.impl;

import com.laporeon.javoid.commands.PrefixCommand;
import com.laporeon.javoid.helpers.MessageManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class ClearChatCommand implements PrefixCommand {
    private static final Logger log = LoggerFactory.getLogger(ClearChatCommand.class);
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("100|[1-9]\\d?");
    private static final String COMMAND_NAME = "limpar";
    private static final int MIN_AMOUNT = 1;
    private static final int MAX_AMOUNT = 100;
    private static final long FEEDBACK_TTL_SECONDS = 10;
    private static final ErrorHandler IGNORE_UNKNOWN_MESSAGE =
            new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE);

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    public String getDescription() {
        return MessageManager.get("clear.description", MAX_AMOUNT);
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (!event.isFromGuild()) {
            replyTemporary(event, MessageManager.get("clear.guild-only"));
            return;
        }

        GuildMessageChannel channel = event.getGuildChannel();
        Member author = event.getMember();

        if (author == null || !author.hasPermission(channel, Permission.MESSAGE_MANAGE)) {
            replyTemporary(event, MessageManager.get("clear.no-permission"));
            return;
        }

        Member self = event.getGuild().getSelfMember();
        if (!self.hasPermission(channel, Permission.MESSAGE_MANAGE, Permission.MESSAGE_HISTORY)) {
            replyTemporary(event, MessageManager.get("clear.bot-no-permission"));
            return;
        }

        if (args.length > 2 || (args.length == 2 && !AMOUNT_PATTERN.matcher(args[1]).matches())) {
            replyTemporary(event, MessageManager.get("clear.usage", MIN_AMOUNT, MAX_AMOUNT));
            return;
        }

        int amount = args.length == 1 ? MAX_AMOUNT : Integer.parseInt(args[1]);

        clearMessages(channel, event.getMessage(), amount);
    }

    private void clearMessages(GuildMessageChannel channel, Message commandMessage, int amount) {
        channel.getIterableHistory()
               .skipTo(commandMessage.getIdLong())
               .takeAsync(amount)
               .thenCompose(history -> purge(channel, history))
               .thenAccept(deleted -> sendTemporary(channel, MessageManager.get("clear.success", deleted, FEEDBACK_TTL_SECONDS)))
               .exceptionally(failure -> {
                   log.warn("Failed to clear messages in channel {}", channel.getId(), failure);
                   sendTemporary(channel, MessageManager.get("clear.failure", FEEDBACK_TTL_SECONDS));
                   return null;
               });
    }

    private CompletableFuture<Integer> purge(GuildMessageChannel channel, List<Message> history) {
        CompletableFuture<?>[] deletions = channel.purgeMessages(history).toArray(new CompletableFuture[0]);
        return CompletableFuture.allOf(deletions).thenApply(ignored -> history.size());
    }

    private void replyTemporary(MessageReceivedEvent event, String text) {
        event.getMessage()
             .reply(text)
             .queue(reply -> reply.delete()
                                  .queueAfter(FEEDBACK_TTL_SECONDS, TimeUnit.SECONDS, null, IGNORE_UNKNOWN_MESSAGE)
             );
    }

    private void sendTemporary(GuildMessageChannel channel, String text) {
        channel.sendMessage(text)
               .queue(msg -> msg.delete()
                                .queueAfter(FEEDBACK_TTL_SECONDS, TimeUnit.SECONDS, null, IGNORE_UNKNOWN_MESSAGE)
               );
    }
}