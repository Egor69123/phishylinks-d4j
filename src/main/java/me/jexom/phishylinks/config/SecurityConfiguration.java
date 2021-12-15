package me.jexom.phishylinks.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    private final AppConfig appConfig;

    private static final String[] FILTERS = {
            "/", "/csrf", "/v2/api-docs", "/v3/api-docs", "/swagger-resources/configuration/ui",
            "/configuration/ui", "/swagger-resources", "/swagger-resources/configuration/security",
            "/configuration/security",
            "/swagger-ui.html", "/swagger-ui/*", "/webjars/**",
            "/actuator", "/actuator/*"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser("admin").password(passwordEncoder().encode(appConfig.getSwaggerPassword()))
                .authorities("ROLE_USER");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors()
                .and()
                .csrf().disable()
                .authorizeRequests()
                .antMatchers(FILTERS).permitAll()
                .antMatchers("/**").authenticated()
                .and().httpBasic();
    }

}
