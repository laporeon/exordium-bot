package com.laporeon.javoid.commands.impl;

import com.laporeon.javoid.commands.PrefixCommand;
import com.laporeon.javoid.helpers.MessageManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;

public class ComplainCommand implements PrefixCommand  {
    private static final String COMMAND_NAME = "reclamar";

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    public String getDescription() {
        return MessageManager.get("complain.description");
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessageEmbeds(
                new EmbedBuilder()
                        .setColor(Color.YELLOW)
                        .setTitle(MessageManager.get("complain.title"))
                        .setImage(MessageManager.get("complain.imageUrl"))
                        .setDescription(MessageManager.get("complain.body")).build()
        ).queue();
    }
}
