package com.laporeon.commands.impl;

import com.laporeon.commands.PrefixCommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class PingCommand implements PrefixCommand {
    private static final String COMMAND_NAME = "ping";

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    public String getDescription() {
        return "O \"oi, sumido\" dos bots";
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessage("PONG! \uD83C\uDFD3").queue();
    }
}
