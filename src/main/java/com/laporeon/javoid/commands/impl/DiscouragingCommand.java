package com.laporeon.javoid.commands.impl;

import com.google.auto.service.AutoService;
import com.laporeon.javoid.commands.PrefixCommand;
import com.laporeon.javoid.helpers.BundleManager;
import com.laporeon.javoid.helpers.RandomMessages;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@AutoService(PrefixCommand.class)
public class DiscouragingCommand implements PrefixCommand {
    private static final String COMMAND_NAME = "desmotivacional";

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    public String getDescription() {
        return BundleManager.get("discouraging.description");
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        String message = RandomMessages.generateMessage(RandomMessages.DISCOURAGING_MESSAGES);
        event.getChannel().sendMessage(message).queue();
    }
}
