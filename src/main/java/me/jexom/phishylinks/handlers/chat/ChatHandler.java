package me.jexom.phishylinks.handlers.chat;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public interface ChatHandler {
    Mono<Void> handle(MessageCreateEvent event);
}
