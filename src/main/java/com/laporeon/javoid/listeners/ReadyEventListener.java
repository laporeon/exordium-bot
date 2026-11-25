package com.laporeon.listeners;

import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class ReadyEventListener extends ListenerAdapter {
    private static final Logger log = LoggerFactory.getLogger(ReadyEventListener.class);

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        log.info("ExordiumBot is up and ready | timestamp={}", Instant.now());
    }
}
