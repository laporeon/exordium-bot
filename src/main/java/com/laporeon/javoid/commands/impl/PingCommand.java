package com.laporeon.javoid.commands.impl;

import com.laporeon.javoid.commands.PrefixCommand;
import com.laporeon.javoid.helpers.MessageManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class PingCommand implements PrefixCommand {
    private static final String COMMAND_NAME = "ping";

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    public String getDescription() {
        return MessageManager.get("ping.description");
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessage(MessageManager.get("ping.reply")).queue();
    }
}
