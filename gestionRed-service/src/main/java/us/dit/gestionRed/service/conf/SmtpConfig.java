package us.dit.gestionRed.service.conf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:release.properties")
public class SmtpConfig {
    @Value("${smtp.username}")
    private String username;

    @Value("${smtp.password}")
    private String password;

    @Value("${smtp.from}")
    private String from;

    @Value("${smtp.to}")
    private String to;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }
}
