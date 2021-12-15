package me.jexom.phishylinks.listeners;

import discord4j.core.event.domain.message.MessageCreateEvent;
import me.jexom.phishylinks.handlers.chat.ChatHandler;
import org.springframework.context.ApplicationContext;
import reactor.core.publisher.Flux;

import java.util.Collection;

public class ChatListener {
    private final Collection<ChatHandler> handlers;

    public ChatListener(ApplicationContext applicationContext) {
        //Get all classes that implement our SlashCommand interface and annotated with @Component
        handlers = applicationContext.getBeansOfType(ChatHandler.class).values();
    }

    public Flux<Void> handle(MessageCreateEvent event) {
        //Convert our list to a flux that we can iterate through
        return Flux.fromIterable(handlers)
                //Have our command class handle all logic related to its specific command.
                .flatMap(command -> command.handle(event));
    }
}
