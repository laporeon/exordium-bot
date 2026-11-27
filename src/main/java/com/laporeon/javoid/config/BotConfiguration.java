package com.laporeon.javoid.config;

import com.laporeon.javoid.commands.BotCommand;
import com.laporeon.javoid.listeners.CommandListener;
import com.laporeon.javoid.listeners.ReadyEventListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ServiceLoader;

public final class BotConfiguration {
    private static final Logger log = LoggerFactory.getLogger(BotConfiguration.class);

    private BotConfiguration() {
    }

    public static void start() throws InterruptedException {
        EnvironmentConfiguration.validate();

        List<BotCommand> commands = loadCommands();
        CommandListener commandListener = new CommandListener(commands);

        JDA jda = JDABuilder.createLight(EnvironmentConfiguration.token(),
                                    GatewayIntent.GUILD_MESSAGES,
                                    GatewayIntent.MESSAGE_CONTENT)
                            .addEventListeners(new ReadyEventListener(), commandListener)
                            .build()
                            .awaitReady();

        try {
            registerSlashCommands(jda, commandListener.slashCommandData());
        } catch (RuntimeException ex) {
            log.error("failed to register slash commands", ex);
        }
    }

    private static List<BotCommand> loadCommands() {
        return ServiceLoader.load(BotCommand.class)
                            .stream()
                            .map(ServiceLoader.Provider::get)
                            .toList();
    }

    private static void registerSlashCommands(JDA jda, List<? extends CommandData> data) {
        clearGuildCommands(jda);

        jda.updateCommands().addCommands(data).queue(
                registered -> log.info("{} global slash commands registered", registered.size()),
                error -> log.error("failed to register global slash commands", error)
        );
    }

    private static void clearGuildCommands(JDA jda) {
        for (Guild guild : jda.getGuilds()) {
            guild.updateCommands().queue(
                    cleared -> log.info("guild slash commands cleared | guild_id={}", guild.getId()),
                    error -> log.error("failed to clear guild slash commands | guild_id={}", guild.getId(), error)
            );
        }
    }
}
