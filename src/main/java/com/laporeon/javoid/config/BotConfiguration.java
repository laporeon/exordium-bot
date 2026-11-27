package com.laporeon.javoid.config;

import com.laporeon.javoid.commands.PrefixCommand;
import com.laporeon.javoid.listeners.CommandListener;
import com.laporeon.javoid.listeners.ReadyEventListener;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.List;
import java.util.ServiceLoader;

public class BotConfiguration {
    public static void start() throws InterruptedException {
        EnvironmentConfiguration.validate();

        JDABuilder.createLight(EnvironmentConfiguration.token(),
                          GatewayIntent.GUILD_MESSAGES,
                          GatewayIntent.MESSAGE_CONTENT)
                  .addEventListeners(
                          new ReadyEventListener(),
                          new CommandListener(loadPrefixCommands())
                  )
                  .build()
                  .awaitReady();
    }

    private static List<PrefixCommand> loadPrefixCommands() {
        return ServiceLoader.load(PrefixCommand.class)
                            .stream()
                            .map(ServiceLoader.Provider::get)
                            .toList();
    }
}