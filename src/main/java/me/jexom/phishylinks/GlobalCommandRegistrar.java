package me.jexom.phishylinks;

import discord4j.common.JacksonResources;
import discord4j.discordjson.json.ApplicationCommandData;
import discord4j.discordjson.json.ApplicationCommandPermissionsRequest;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.RestClient;
import discord4j.rest.service.ApplicationService;
import me.jexom.phishylinks.config.AppConfig;
import me.jexom.phishylinks.util.ApplicationCommandWithPermissions;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class GlobalCommandRegistrar implements ApplicationRunner {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private final RestClient client;
    private final AppConfig appConfig;

    //Use the rest client provided by our Bean
    public GlobalCommandRegistrar(RestClient client, AppConfig appConfig) {
        this.client = client;
        this.appConfig = appConfig;
    }

    //This method will run only once on each start up and is automatically called with Spring so blocking is okay.
    @Override
    public void run(ApplicationArguments args) throws IOException {
        //Create an ObjectMapper that supported Discord4J classes
        final JacksonResources d4jMapper = JacksonResources.create();

        // Convenience variables for the sake of easier to read code below.
        PathMatchingResourcePatternResolver matcher = new PathMatchingResourcePatternResolver();
        final ApplicationService applicationService = client.getApplicationService();
        final long applicationId = client.getApplicationId().block();


        Map<String, ApplicationCommandData> discordCommands;
        try {
            //These are commands already registered with discord from previous runs of the bot.
            discordCommands = applicationService
                    .getGuildApplicationCommands(applicationId, appConfig.getGuildId())
                    .collectMap(ApplicationCommandData::name)
                    .block();
        } catch (Exception e) {
            return;
        }

        //Get our commands json from resources as command data
        Map<String, ApplicationCommandWithPermissions> commands = new HashMap<>();
        for (Resource resource : matcher.getResources("commands/**/*.json")) {
            ApplicationCommandWithPermissions commandWithPerms = d4jMapper.getObjectMapper()
                    .readValue(resource.getInputStream(), ApplicationCommandWithPermissions.class);
            ApplicationCommandRequest request = commandWithPerms.getCommand();

            commands.put(request.name(), commandWithPerms);

            //Check if this is a new command that has not already been registered.
            if (!discordCommands.containsKey(request.name())) {
                //Not yet created with discord, lets do it now.
                String commandId = applicationService.createGuildApplicationCommand(applicationId, appConfig.getGuildId(), request).block().id();
                if (CollectionUtils.isNotEmpty(commandWithPerms.getPermissions())) {
                    applicationService.modifyApplicationCommandPermissions(
                            applicationId,
                            appConfig.getGuildId(),
                            Long.parseLong(commandId),
                            ApplicationCommandPermissionsRequest.builder()
                                    .addAllPermissions(commandWithPerms.getPermissions())
                                    .build()
                    ).block();
                }

                LOGGER.info("Created guild command: {}", request.name());
            }
        }

        //Check if any  commands have been deleted or changed.
        for (ApplicationCommandData discordCommand : discordCommands.values()) {
            long discordCommandId = Long.parseLong(discordCommand.id());

            ApplicationCommandWithPermissions commandWithPerms = commands.get(discordCommand.name());

            if (commandWithPerms == null) {
                //Removed command.json, delete global command
                applicationService.deleteGuildApplicationCommand(applicationId, appConfig.getGuildId(), discordCommandId).block();

                LOGGER.info("Deleted guild command: {}", discordCommand.name());
                continue; //Skip further processing on this command.
            }

            //Check if the command has been changed and needs to be updated.
            if (hasChanged(discordCommand, commandWithPerms.getCommand())) {
                String commandId = applicationService.modifyGuildApplicationCommand(applicationId, appConfig.getGuildId(), discordCommandId, commandWithPerms.getCommand()).block().id();
                if (CollectionUtils.isNotEmpty(commandWithPerms.getPermissions())) {
                    applicationService.modifyApplicationCommandPermissions(
                            applicationId,
                            appConfig.getGuildId(),
                            Long.parseLong(commandId),
                            ApplicationCommandPermissionsRequest.builder()
                                    .addAllPermissions(commandWithPerms.getPermissions())
                                    .build()
                    ).block();
                }

                LOGGER.info("Updated guild command: {}", commandWithPerms.getCommand().name());
            }
        }
    }

    private boolean hasChanged(ApplicationCommandData discordCommand, ApplicationCommandRequest command) {
        // Compare types
        if (!discordCommand.type().toOptional().orElse(1).equals(command.type().toOptional().orElse(1))) {
            return true;
        }

        //Check if description has changed.
        if (!discordCommand.description().equals(command.description().toOptional().orElse(""))) {
            return true;
        }

        //Check if default permissions have changed
        boolean discordCommandDefaultPermission = discordCommand.defaultPermission().toOptional().orElse(true);
        boolean commandDefaultPermission = command.defaultPermission().toOptional().orElse(true);

        if (discordCommandDefaultPermission != commandDefaultPermission) {
            return true;
        }

        //Check and return if options have changed.
        return !discordCommand.options().equals(command.options());
    }
}

