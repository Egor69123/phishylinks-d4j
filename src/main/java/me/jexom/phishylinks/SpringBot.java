package me.jexom.phishylinks;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.interaction.MessageInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import discord4j.rest.RestClient;
import lombok.extern.log4j.Log4j2;
import me.jexom.phishylinks.listeners.ChatListener;
import me.jexom.phishylinks.listeners.MessageCommandListener;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import reactor.core.publisher.Mono;

@Log4j2
@SpringBootApplication
@EnableScheduling
public class SpringBot {

    public static void main(String[] args) {
        //Start spring application
        ApplicationContext springContext = new SpringApplicationBuilder(SpringBot.class)
                .build()
                .run(args);

        //Login
        DiscordClientBuilder.create(System.getenv("BOT_TOKEN")).build().gateway().setEnabledIntents(
                IntentSet.of(Intent.GUILD_MEMBERS, Intent.GUILD_MESSAGES)
        ).withGateway(gatewayClient -> {
            MessageCommandListener messageCommandListener = new MessageCommandListener(springContext);
            ChatListener chatListener = new ChatListener(springContext);

            Mono<Void> onMessageCommandMono = gatewayClient
                    .on(MessageInteractionEvent.class, messageCommandListener::handle)
                    .then();

            Mono<Void> onChat = gatewayClient
                    .on(MessageCreateEvent.class, chatListener::handle)
                    .then();

            return Mono.when(onMessageCommandMono)
                    .and(onChat);
        }).block();
    }

    @Bean
    public RestClient discordRestClient() {
        return RestClient.create(System.getenv("BOT_TOKEN"));
    }
}
