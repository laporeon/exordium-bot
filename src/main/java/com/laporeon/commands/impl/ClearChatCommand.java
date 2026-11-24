package com.laporeon.commands.impl;

import com.laporeon.commands.PrefixCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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
    private static final long FEEDBACK_TTL_SECONDS = 5;
    private static final String USAGE = """
        **Valor inválido.** Informe um número entre %d e %d.

        **Exemplos:**
        `!limpar` → remove as últimas %d mensagens
        `!limpar 10` → remove as últimas 10 mensagens"""
            .formatted(MIN_AMOUNT, MAX_AMOUNT, MAX_AMOUNT);

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    public String getDescription() {
        return "Remove até as últimas %d mensagens do canal.".formatted(MAX_AMOUNT);
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (!event.isFromGuild()) {
            replyTemporary(event, "Esse comando só funciona em servidores.");
            return;
        }

        GuildMessageChannel channel = event.getGuildChannel();
        Member author = event.getMember();

        if (author == null || !author.hasPermission(channel, Permission.MESSAGE_MANAGE)) {
            replyTemporary(event, "Você não tem permissões suficientes para executar esse comando.");
            return;
        }

        Member self = event.getGuild().getSelfMember();
        if (!self.hasPermission(channel, Permission.MESSAGE_MANAGE, Permission.MESSAGE_HISTORY)) {
            replyTemporary(event, "Preciso das permissões **Gerenciar mensagens** e **Ver histórico de mensagens** neste canal.");
            return;
        }

        if (args.length > 2 || (args.length == 2 && !AMOUNT_PATTERN.matcher(args[1]).matches())) {
            replyTemporary(event, USAGE);
            return;
        }

        int amount = args.length == 1 ? MAX_AMOUNT : Integer.parseInt(args[1]);

        clearMessages(channel, event.getMessage(), amount);
    }

    private void clearMessages(GuildMessageChannel channel, Message commandMessage, int amount) {
        channel.getIterableHistory()
               .skipTo(commandMessage.getIdLong())
               .takeAsync(amount)
               .thenCompose(history -> purge(channel, commandMessage, history))
               .thenAccept(deleted -> sendTemporary(channel, "%d mensagem(ns) removida(s).".formatted(deleted)))
               .exceptionally(failure -> {
                   log.warn("Failed to clear messages in channel {}", channel.getId(), failure);
                   sendTemporary(channel, "Não foi possível remover algumas mensagens.");
                   return null;
               });
    }

    private CompletableFuture<Integer> purge(GuildMessageChannel channel, Message commandMessage, List<Message> history) {
        List<Message> toDelete = new ArrayList<>(history);
        toDelete.add(commandMessage);

        CompletableFuture<?>[] deletions = channel.purgeMessages(toDelete).toArray(new CompletableFuture[0]);
        return CompletableFuture.allOf(deletions).thenApply(ignored -> history.size());
    }

    private void replyTemporary(MessageReceivedEvent event, String text) {
        event.getMessage()
             .reply(text)
             .queue(reply -> reply.delete().queueAfter(FEEDBACK_TTL_SECONDS, TimeUnit.SECONDS));
    }

    private void sendTemporary(GuildMessageChannel channel, String text) {
        channel.sendMessage(text)
               .queue(message -> message.delete().queueAfter(FEEDBACK_TTL_SECONDS, TimeUnit.SECONDS));
    }
}