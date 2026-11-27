package com.laporeon.javoid.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public interface PrefixCommand {
    String getName();
    String getDescription();
    void execute(MessageReceivedEvent event, String[] args);
    void execute(SlashCommandInteractionEvent event);

    default List<OptionData> getOptions() {
        return List.of();
    }
}
