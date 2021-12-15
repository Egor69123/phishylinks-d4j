package me.jexom.phishylinks.listeners;

import discord4j.core.event.domain.interaction.MessageInteractionEvent;
import me.jexom.phishylinks.handlers.commands.MessageCommand;
import org.springframework.context.ApplicationContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public class MessageCommandListener {
    private final Collection<MessageCommand> commands;

    public MessageCommandListener(ApplicationContext applicationContext) {
        //Get all classes that implement our SlashCommand interface and annotated with @Component
        commands = applicationContext.getBeansOfType(MessageCommand.class).values();
    }


    public Mono<Void> handle(MessageInteractionEvent event) {
        //Convert our list to a flux that we can iterate through
        return Flux.fromIterable(commands)
                //Filter out all commands that don't match the name this event is for
                .filter(command -> command.getName().equals(event.getCommandName()))
                //Get the first (and only) item in the flux that matches our filter
                .next()
                //Have our command class handle all logic related to its specific command.
                .flatMap(command -> command.handle(event));
    }
}
