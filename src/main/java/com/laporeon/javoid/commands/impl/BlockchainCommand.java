package com.laporeon.javoid.commands.impl;

import com.google.auto.service.AutoService;
import com.laporeon.javoid.commands.PrefixCommand;
import com.laporeon.javoid.helpers.BundleManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@AutoService(PrefixCommand.class)
public class BlockchainCommand implements PrefixCommand {
    private static final String COMMAND_NAME = "blockchain";

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    public String getDescription() {
        return BundleManager.get("blockchain.description");
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessage(BundleManager.get("blockchain.body")).queue();
    }
}
