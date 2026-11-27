package com.laporeon.javoid.commands.impl;

import com.google.auto.service.AutoService;
import com.laporeon.javoid.commands.BotCommand;
import com.laporeon.javoid.helpers.BundleManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@AutoService(BotCommand.class)
public class PingCommand implements BotCommand {
    private static final String COMMAND_NAME = "ping";

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    public String getDescription() {
        return BundleManager.get("ping.description");
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessage(BundleManager.get("ping.reply")).queue();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.reply(BundleManager.get("ping.reply")).queue();
    }
}
