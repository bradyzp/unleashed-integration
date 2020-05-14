package net.jastrab.unleashedintegration.configuration;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@Value
@AllArgsConstructor
@ConstructorBinding
@ConfigurationProperties(prefix = "integration")
public class IntegrationConfigurationProperties {
    private final int maxOrderAgeDays = 7;
}
