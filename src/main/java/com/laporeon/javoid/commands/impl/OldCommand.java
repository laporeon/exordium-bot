package com.laporeon.javoid.commands.impl;

import com.laporeon.javoid.commands.PrefixCommand;
import com.laporeon.javoid.helpers.MessageManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;

public class OldCommand implements PrefixCommand {
    private static final String COMMAND_NAME = "velho";

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    public String getDescription() {
        return MessageManager.get("old.description");
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        event.getChannel()
             .sendMessageEmbeds(
                new EmbedBuilder()
                        .setColor(Color.GRAY)
                        .setTitle(MessageManager.get("old.title"))
                        .setThumbnail(MessageManager.get("old.imageUrl"))
                        .setDescription(MessageManager.get("old.body")).build())
             .queue();
    }
}
