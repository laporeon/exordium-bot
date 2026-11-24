package com.laporeon.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public interface PrefixCommand {
    String getName();
    String getDescription();
    void execute(MessageReceivedEvent event, String[] args);
}
