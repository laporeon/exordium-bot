package com.laporeon.javoid.config;

import com.laporeon.javoid.commands.PrefixCommand;
import com.laporeon.javoid.commands.impl.ClearChatCommand;
import com.laporeon.javoid.commands.impl.ComplainCommand;
import com.laporeon.javoid.commands.impl.PingCommand;
import com.laporeon.javoid.listeners.CommandListener;
import com.laporeon.javoid.listeners.ReadyEventListener;
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
