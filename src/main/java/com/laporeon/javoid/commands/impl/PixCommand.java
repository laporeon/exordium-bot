package com.laporeon.javoid.commands.impl;

import com.google.auto.service.AutoService;
import com.laporeon.javoid.commands.BotCommand;
import com.laporeon.javoid.helpers.BundleManager;
import com.laporeon.javoid.helpers.FileManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@AutoService(BotCommand.class)
public class PixCommand implements BotCommand {
    private static final String COMMAND_NAME = "pix";
    private static final String AUDIO_FILENAME = "pix.ogg";
    private static final double AUDIO_SECONDS = 5.15;

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    public String getDescription() {
        return BundleManager.get("pix.description");
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        event.getMessage().replyFiles(FileManager.voiceMessage(AUDIO_FILENAME, AUDIO_SECONDS)).setVoiceMessage(true).queue();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.replyFiles(FileManager.voiceMessage(AUDIO_FILENAME, AUDIO_SECONDS)).setVoiceMessage(true).queue();
    }

}