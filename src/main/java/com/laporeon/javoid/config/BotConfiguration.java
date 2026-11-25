package com.laporeon.config;

import com.laporeon.commands.PrefixCommand;
import com.laporeon.commands.impl.ClearChatCommand;
import com.laporeon.commands.impl.ComplainCommand;
import com.laporeon.commands.impl.PingCommand;
import com.laporeon.listeners.CommandListener;
import com.laporeon.listeners.ReadyEventListener;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class BotConfiguration {
    private static final Logger log = LoggerFactory.getLogger(BotConfiguration.class);
    private static final List<PrefixCommand> PREFIX_COMMANDS = List.of(
            new PingCommand(),
            new ClearChatCommand(),
            new ComplainCommand()
    );

    public static void start() throws InterruptedException {
        EnvironmentConfiguration.validate();

        JDABuilder.createDefault(EnvironmentConfiguration.token())
                  .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                  .addEventListeners(
                          new ReadyEventListener(),
                          new CommandListener(PREFIX_COMMANDS)
                  )
                  .build()
                  .awaitReady();
    }
}
