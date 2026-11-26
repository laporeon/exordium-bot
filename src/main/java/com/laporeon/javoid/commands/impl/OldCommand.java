package com.laporeon.javoid.commands.impl;

import com.google.auto.service.AutoService;
import com.laporeon.javoid.commands.PrefixCommand;
import com.laporeon.javoid.helpers.BundleManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;

@AutoService(PrefixCommand.class)
public class OldCommand implements PrefixCommand {
    private static final String COMMAND_NAME = "velho";

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    public String getDescription() {
        return BundleManager.get("old.description");
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        event.getChannel()
             .sendMessageEmbeds(
                new EmbedBuilder()
                        .setColor(Color.GRAY)
                        .setTitle(BundleManager.get("old.title"))
                        .setThumbnail(BundleManager.get("old.imageUrl"))
                        .setDescription(BundleManager.get("old.body")).build())
             .queue();
    }
}
