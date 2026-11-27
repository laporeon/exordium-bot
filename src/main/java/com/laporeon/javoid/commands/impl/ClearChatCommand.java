package com.laporeon.javoid.commands.impl;

import com.google.auto.service.AutoService;
import com.laporeon.javoid.commands.BotCommand;
import com.laporeon.javoid.helpers.BundleManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@AutoService(BotCommand.class)
public class ClearChatCommand implements BotCommand {
    private static final Logger log = LoggerFactory.getLogger(ClearChatCommand.class);
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("100|[1-9]\\d?");
    private static final String COMMAND_NAME = "limpar";
    private static final String AMOUNT_OPTION = "quantidade";
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
        return BundleManager.get("clear.description", MAX_AMOUNT);
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(new OptionData(OptionType.INTEGER, AMOUNT_OPTION, BundleManager.get("clear.option"), false)
                .setRequiredRange(MIN_AMOUNT, MAX_AMOUNT));
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (!event.isFromGuild()) {
            replyTemporary(event, BundleManager.get("clear.guild-only"));
            return;
        }

        GuildMessageChannel channel = event.getGuildChannel();
        Member author = event.getMember();

        if (author == null || !author.hasPermission(channel, Permission.MESSAGE_MANAGE)) {
            replyTemporary(event, BundleManager.get("clear.no-permission"));
            return;
        }

        Member self = event.getGuild().getSelfMember();
        if (!self.hasPermission(channel, Permission.MESSAGE_MANAGE, Permission.MESSAGE_HISTORY)) {
            replyTemporary(event, BundleManager.get("clear.bot-no-permission"));
            return;
        }

        if (args.length > 2 || (args.length == 2 && !AMOUNT_PATTERN.matcher(args[1]).matches())) {
            replyTemporary(event, BundleManager.get("clear.usage", MIN_AMOUNT, MAX_AMOUNT));
            return;
        }

        int amount = args.length == 1 ? MAX_AMOUNT : Integer.parseInt(args[1]);

        clearMessages(channel, event.getMessage(), author, amount);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (!event.isFromGuild()) {
            event.reply(BundleManager.get("clear.guild-only")).setEphemeral(true).queue();
            return;
        }

        GuildMessageChannel channel = event.getGuildChannel();
        Member author = event.getMember();

        if (author == null || !author.hasPermission(channel, Permission.MESSAGE_MANAGE)) {
            event.reply(BundleManager.get("clear.no-permission")).setEphemeral(true).queue();
            return;
        }

        Member self = event.getGuild().getSelfMember();
        if (!self.hasPermission(channel, Permission.MESSAGE_MANAGE, Permission.MESSAGE_HISTORY)) {
            event.reply(BundleManager.get("clear.bot-no-permission")).setEphemeral(true).queue();
            return;
        }

        int amount = event.getOption(AMOUNT_OPTION, MAX_AMOUNT, OptionMapping::getAsInt);

        event.deferReply(true).queue();
        channel.getIterableHistory()
               .takeAsync(amount)
               .thenCompose(msgs -> purge(channel, msgs))
               .thenAccept(deleted -> {
                   log.info("{} message(s) deleted by userId={} | username={} | guildId={} | channelId={}",
                           deleted,
                           author.getIdLong(),
                           author.getUser().getName(),
                           channel.getGuild().getIdLong(),
                           channel.getIdLong());
                   event.getHook()
                        .sendMessage(BundleManager.get("clear.success", deleted, FEEDBACK_TTL_SECONDS))
                        .queue(ignored -> event.getHook()
                                               .deleteOriginal()
                                               .queueAfter(FEEDBACK_TTL_SECONDS, TimeUnit.SECONDS, null, IGNORE_UNKNOWN_MESSAGE));
               })
               .exceptionally(failure -> {
                   log.warn("Failed to clear messages | requested by userId={} | username={} | guildId={} | channelId={}",
                           author.getIdLong(),
                           author.getUser().getName(),
                           channel.getGuild().getIdLong(),
                           channel.getIdLong(),
                           failure);
                   event.getHook().sendMessage(BundleManager.get("clear.failure", FEEDBACK_TTL_SECONDS)).queue();
                   return null;
               });
    }

    private void clearMessages(GuildMessageChannel channel, Message commandMessage, Member author, int amount) {
        channel.getIterableHistory()
               .skipTo(commandMessage.getIdLong())
               .takeAsync(amount)
               .thenCompose(msgs -> purge(channel, msgs))
               .thenCompose(deleted -> {
                   log.info("{} message(s) deleted by userId={} | username={} | guildId={} | channelId={}",
                           deleted,
                           author.getIdLong(),
                           author.getUser().getName(),
                           channel.getGuild().getIdLong(),
                           channel.getIdLong());
                   return sendTemporary(channel, BundleManager.get("clear.success", deleted, FEEDBACK_TTL_SECONDS));
               })
               .thenCompose(ignored -> deleteCommandMessage(commandMessage))
               .exceptionally(failure -> {
                   log.warn("Failed to clear messages | requested by userId={} | username={} | guildId={} | channelId={}",
                           author.getIdLong(),
                           author.getUser().getName(),
                           channel.getGuild().getIdLong(),
                           channel.getIdLong(),
                           failure);
                   sendTemporary(channel, BundleManager.get("clear.failure", FEEDBACK_TTL_SECONDS));
                   return null;
               });
    }

    private CompletableFuture<Integer> purge(GuildMessageChannel channel, List<Message> messages) {
        CompletableFuture<?>[] deletions = channel.purgeMessages(messages).toArray(new CompletableFuture[0]);
        return CompletableFuture.allOf(deletions).thenApply(ignored -> messages.size());
    }

    private CompletableFuture<Void> deleteCommandMessage(Message commandMessage) {
        return commandMessage.delete()
                             .submit()
                             .exceptionally(failure -> {
                                 log.debug("Could not delete command message | messageId={} | guildId={} | channelId={}",
                                         commandMessage.getIdLong(),
                                         commandMessage.getGuild().getIdLong(),
                                         commandMessage.getChannel().getIdLong(),
                                         failure);
                                 return null;
                             });
    }

    private void replyTemporary(MessageReceivedEvent event, String text) {
        event.getMessage()
             .reply(text)
             .queue(reply -> reply.delete()
                                  .queueAfter(FEEDBACK_TTL_SECONDS, TimeUnit.SECONDS, null, IGNORE_UNKNOWN_MESSAGE)
             );
    }

    private CompletableFuture<Void> sendTemporary(GuildMessageChannel channel, String text) {
        return channel.sendMessage(text)
                      .submit()
                      .thenAccept(msg -> msg.delete()
                                            .queueAfter(FEEDBACK_TTL_SECONDS, TimeUnit.SECONDS, null, IGNORE_UNKNOWN_MESSAGE))
                      .exceptionally(failure -> {
                          log.debug("Could not send feedback message | guildId={} | channelId={}",
                                  channel.getGuild().getIdLong(),
                                  channel.getIdLong(),
                                  failure);
                          return null;
                      });
    }
}