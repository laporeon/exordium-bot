package com.laporeon.javoid.commands.impl;

import com.google.auto.service.AutoService;
import com.laporeon.javoid.commands.PrefixCommand;
import com.laporeon.javoid.helpers.BundleManager;
import com.laporeon.javoid.helpers.RandomMessages;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@AutoService(PrefixCommand.class)
public class GoodMorningCommand implements PrefixCommand {
    private static final String COMMAND_NAME = "bdia";

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    public String getDescription() {
        return BundleManager.get("morning.description");
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        String message = RandomMessages.generateMessage(RandomMessages.GOOD_MORNING_MESSAGES);
        event.getChannel().sendMessage(BundleManager.get("morning.body", message)).queue();
    }
}
