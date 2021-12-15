package me.jexom.phishylinks.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.jexom.phishylinks.config.AppConfig;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@Log4j2
@Component
@RequiredArgsConstructor
public class ScheduledService {

    private final AppConfig appConfig;

    @Scheduled(cron = "0 */10 * * * *")
    private void ping() throws IOException {
        URL url = new URL(String.format("http://%s.herokuapp.com", appConfig.getHerokuAppName()));
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        con.setRequestMethod("GET");
        con.getResponseCode();
        con.disconnect();
    }
}
