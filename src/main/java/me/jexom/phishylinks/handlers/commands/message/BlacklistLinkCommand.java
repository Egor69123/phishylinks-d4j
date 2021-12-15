package me.jexom.phishylinks.handlers.commands.message;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.MessageInteractionEvent;
import discord4j.discordjson.possible.Possible;
import lombok.RequiredArgsConstructor;
import me.jexom.phishylinks.config.AppConfig;
import me.jexom.phishylinks.handlers.commands.MessageCommand;
import me.jexom.phishylinks.service.BlacklistService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BlacklistLinkCommand implements MessageCommand {

    private final BlacklistService blacklistService;
    private final AppConfig appConfig;

    @Override
    public String getName() {
        return "Blacklist links";
    }

    @Override
    public Mono<Void> handle(MessageInteractionEvent event) {
        List<Snowflake> staffRoles = appConfig.getStaffRoles().stream()
                .map(Snowflake::of)
                .collect(Collectors.toList());

        return event.deferReply().withEphemeral(true).and(event.getTargetMessage().flatMap(message -> {
            if (event.getInteraction().getMember().stream().noneMatch(member -> member.getRoleIds().stream().anyMatch(staffRoles::contains))) {
                return event.editReply("You do no have the permission to use this command");
            }
            List<String> links = new ArrayList<>();
            Matcher matcher = Pattern.compile("(http|ftp|https)(://)([\\w-]+(?:\\.[\\w-]+)+)([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?")
                    .matcher(message.getContent());

            matcher.results().forEach(result -> links.add(result.group()));
            if (links.isEmpty()) {
                return event.editReply().withContent(Possible.of(Optional.of("No links found")));
            }

            List<String> blacklistLinks = blacklistService.addLinks(links);
            return event.editReply().withContent(Possible.of(Optional.of(
                    "Domains added to blacklist:\n" + String.join("\n", blacklistLinks)
            ))).and(message.getAuthorAsMember()
                    .flatMap(member -> member.addRole(Snowflake.of(appConfig.getMuteRoleId()), "Blacklisted link"))
            ).and(message.delete());
        }));
    }
}
