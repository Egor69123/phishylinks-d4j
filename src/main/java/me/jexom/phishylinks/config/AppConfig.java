package me.jexom.phishylinks.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties("application")
@Getter
@Setter
public class AppConfig {

    private String swaggerPassword;

    private Long guildId;

    private Long muteRoleId;
    private Long notificationChannelId;

    private String herokuAppName;

    private List<Long> staffRoles;
}
