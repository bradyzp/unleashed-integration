package net.jastrab.unleashedintegration.configuration;

import net.jastrab.unleashedintegration.service.UnleashedConfigService;
import net.jastrab.unleashedspringclient.UnleashedClientConfigProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DigiKeyConfigurationProperties.class)
public class CommonConfig {

    @Bean
    public UnleashedClientConfigProvider mongoProvider(UnleashedConfigService configService) {
        return new UnleashedClientConfigMongoProvider(configService);
    }

}
