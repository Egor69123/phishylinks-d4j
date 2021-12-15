package me.jexom.phishylinks.util;

import discord4j.discordjson.json.ApplicationCommandPermissionsData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApplicationCommandWithPermissions {

    ApplicationCommandRequest command;
    List<ApplicationCommandPermissionsData> permissions;

}
