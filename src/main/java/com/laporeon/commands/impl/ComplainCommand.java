package com.laporeon.commands.impl;

import com.laporeon.commands.PrefixCommand;
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
        return "Ninguém quer solução aqui não.";
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessageEmbeds(
                new EmbedBuilder()
                        .setColor(Color.YELLOW)
                        .setTitle("SOLUÇÃO?? \uD83E\uDD28")
                        .setImage("https://i.imgur.com/q9cPy2m.jpeg")
                        .setDescription("Eu não quero solução, eu quero é reclamar!!").build()
        ).queue();
    }
}
