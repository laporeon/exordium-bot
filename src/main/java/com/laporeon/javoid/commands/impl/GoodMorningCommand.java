package com.laporeon.javoid.commands.impl;

import com.google.auto.service.AutoService;
import com.laporeon.javoid.commands.BotCommand;
import com.laporeon.javoid.helpers.BundleManager;
import com.laporeon.javoid.helpers.RandomMessages;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@AutoService(BotCommand.class)
public class GoodMorningCommand implements BotCommand {
    private static final String COMMAND_NAME = "bdia";

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    public String getDescription() {
        return BundleManager.get("morning.description");
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessage(body()).queue();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.reply(body()).queue();
    }

    private String body() {
        String message = RandomMessages.generateMessage(RandomMessages.GOOD_MORNING_MESSAGES);
        return BundleManager.get("morning.body", message);
    }
}
