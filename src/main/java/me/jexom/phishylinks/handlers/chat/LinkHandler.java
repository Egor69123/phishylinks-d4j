package me.jexom.phishylinks.handlers.chat;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import lombok.RequiredArgsConstructor;
import me.jexom.phishylinks.config.AppConfig;
import me.jexom.phishylinks.domain.BlacklistedLink;
import me.jexom.phishylinks.repository.BlacklistedLinkRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LinkHandler implements ChatHandler {

    private final BlacklistedLinkRepository blacklistedLinkRepository;
    private final AppConfig appConfig;

    @Override
    public Mono<Void> handle(MessageCreateEvent event) {
        String message = event.getMessage().getContent();

        List<String> links = new ArrayList<>();
        Matcher matcher = Pattern.compile("(http|ftp|https)(://)([\\w-]+(?:\\.[\\w-]+)+)([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?")
                .matcher(message);

        matcher.results().forEach(result -> {
            Matcher domainMatcher = Pattern.compile("https?://(?:www\\.)?(?<domain>[\\w.]*)/?.*").matcher(result.group());
            if (domainMatcher.matches()) {
                String domain = domainMatcher.group("domain");
                links.add(domain);
            }
        });
        if (links.isEmpty()) {
            return Mono.empty();
        }

        List<String> blacklistedLinks = blacklistedLinkRepository.findAll().stream()
                .map(BlacklistedLink::getLink)
                .collect(Collectors.toList());

        for (String link : links) {
            if (blacklistedLinks.contains(link)) {
                Member member = event.getMember().orElseThrow();
                event.getGuild()
                        .flatMap(guild -> guild.getChannelById(Snowflake.of(appConfig.getNotificationChannelId())))
                        .flatMap(guildChannel -> ((MessageChannel) guildChannel).createMessage(MessageCreateSpec.builder()
                                .content(member.getMention() + " muted for posting a blacklisted link.")
                                .addEmbed(EmbedCreateSpec.builder()
                                        .author(EmbedCreateFields.Author.of(member.getDisplayName(), null, member.getAvatarUrl()))
                                        .addField("Original message", message, false)
                                        .build())
                                .build())).block();
                return member.addRole(Snowflake.of(appConfig.getMuteRoleId()), "Blacklisted link")
                        .and(event.getMessage().delete());
            }
        }

        return Mono.empty();
    }
}
