package com.laporeon.javoid.commands.impl;

import com.google.auto.service.AutoService;
import com.laporeon.javoid.commands.BotCommand;
import com.laporeon.javoid.helpers.BundleManager;
import com.laporeon.javoid.helpers.RandomMessages;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@AutoService(BotCommand.class)
public class DiscouragingCommand implements BotCommand {
    private static final String COMMAND_NAME = "desmotivacional";

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    public String getDescription() {
        return BundleManager.get("discouraging.description");
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessage(randomMessage()).queue();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.reply(randomMessage()).queue();
    }

    private String randomMessage() {
        return RandomMessages.generateMessage(RandomMessages.DISCOURAGING_MESSAGES);
    }
}
