package net.jastrab.unleashedintegration.configuration;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@Value
@AllArgsConstructor
@ConstructorBinding
@ConfigurationProperties(prefix = "digikey")
public class DigiKeyConfigurationProperties {
    private final String authUri;
    private final String tokenUri;
    private final String redirectUri;
    private final String clientId;
    private final String clientSecret;
    private final String tokenId;
}

