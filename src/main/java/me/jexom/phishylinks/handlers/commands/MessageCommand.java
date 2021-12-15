package me.jexom.phishylinks.handlers.commands;

import discord4j.core.event.domain.interaction.MessageInteractionEvent;
import reactor.core.publisher.Mono;

/**
 * A simple interface defining our message command class contract.
 * a getName() method to provide the case-sensitive name of the command.
 * and a handle() method which will house all the logic for processing each command.
 */
public interface MessageCommand {

    String getName();

    Mono<Void> handle(MessageInteractionEvent event);
}
