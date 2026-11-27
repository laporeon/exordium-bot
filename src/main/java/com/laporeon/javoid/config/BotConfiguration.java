package com.laporeon.javoid.config;

import com.laporeon.javoid.commands.BotCommand;
import com.laporeon.javoid.exceptions.DiscordException;
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
    private static final String GUILD_NOT_FOUND_MESSAGE = "Development guild '%s' was not found. " +
            "Check DISCORD_GUILD_ID configuration and try again.";

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
        if (EnvironmentConfiguration.isDev()) {
            registerDevelopmentCommands(jda, data);
            return;
        }

        clearGuildCommands(jda);

        jda.updateCommands().addCommands(data).queue(
                registered -> log.info("{} global slash commands registered", registered.size()),
                error -> log.error("failed to register global slash commands", error)
        );
    }

    /**
     * Removes guild-scoped commands left over from development runs, which would otherwise
     * show up duplicated next to the global ones.
     */
    private static void clearGuildCommands(JDA jda) {
        for (Guild guild : jda.getGuilds()) {
            guild.updateCommands().queue(
                    cleared -> log.info("guild slash commands cleared | guild_id={}", guild.getId()),
                    error -> log.error("failed to clear guild slash commands | guild_id={}", guild.getId(), error)
            );
        }
    }

    private static void registerDevelopmentCommands(JDA jda, List<? extends CommandData> data) {
        String guildId = EnvironmentConfiguration.guildId()
                                                 .orElseThrow(() -> new DiscordException(
                                                         "DISCORD_GUILD_ID is required in development mode"));

        Guild guild = jda.getGuildById(guildId);
        if (guild == null) {
            throw new DiscordException(GUILD_NOT_FOUND_MESSAGE.formatted(guildId));
        }

        guild.updateCommands().addCommands(data).queue(
                registered -> log.info("development slash commands registered | count={} | guild_id={} | guild_name={}",
                        registered.size(), guild.getId(), guild.getName()),
                error -> log.error("failed to register development slash commands | guild_id={}", guildId, error)
        );
    }
}