package com.laporeon.javoid.commands.impl;

import com.google.auto.service.AutoService;
import com.laporeon.javoid.commands.BotCommand;
import com.laporeon.javoid.helpers.BundleManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;

@AutoService(BotCommand.class)
public class ComplainCommand implements BotCommand {
    private static final String COMMAND_NAME = "reclamar";

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    public String getDescription() {
        return BundleManager.get("complain.description");
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessageEmbeds(embed()).queue();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.replyEmbeds(embed()).queue();
    }

    private MessageEmbed embed() {
        return new EmbedBuilder()
                .setColor(Color.YELLOW)
                .setTitle(BundleManager.get("complain.title"))
                .setImage(BundleManager.get("complain.imageUrl"))
                .setDescription(BundleManager.get("complain.body")).build();
    }
}
