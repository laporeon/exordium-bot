package com.laporeon.javoid.listeners;

import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;

public class ReadyEventListener extends ListenerAdapter {
    private static final Logger log = LoggerFactory.getLogger(ReadyEventListener.class);

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        SelfUser self = event.getJDA().getSelfUser();
        long startupMs = ManagementFactory.getRuntimeMXBean().getUptime();

        log.info("Bot is ready | botId={} | botName={} | guildsAvailable={} | guildsTotal={} | startupMs={} | jdaVersion={}",
                self.getIdLong(),
                self.getName(),
                event.getGuildAvailableCount(),
                event.getGuildTotalCount(),
                startupMs,
                JDAInfo.VERSION);

        int unavailableGuilds = event.getGuildUnavailableCount();
        if (unavailableGuilds > 0) {
            log.warn("Some guilds are unavailable at startup | guildsUnavailable={} | guildsTotal={}",
                    unavailableGuilds,
                    event.getGuildTotalCount());
        }
    }
}