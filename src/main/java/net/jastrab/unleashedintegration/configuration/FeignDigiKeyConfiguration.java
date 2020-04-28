package net.jastrab.unleashedintegration.configuration;

import feign.Client;
import feign.Logger;
import feign.RequestInterceptor;
import feign.http2client.Http2Client;
import net.jastrab.unleashedintegration.service.DigiKeyAuthService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignDigiKeyConfiguration {
    private final DigiKeyAuthService authService;
    private final String clientId;

    public FeignDigiKeyConfiguration(DigiKeyAuthService authService,
                                     DigiKeyConfigurationProperties properties) {
        this.authService = authService;
        this.clientId = properties.getClientId();
    }

    @Bean
    Logger.Level getLogLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    Client feignHttp2Client() {
        return new Http2Client();
    }

    @Bean
    RequestInterceptor digiKeyRequestInterceptor() {
        return template -> template.header("X-DIGIKEY-Client-Id", clientId)
                .header("X-DIGIKEY-Locale-Site", "US")
                .header("X-DIGIKEY-Locale-Language", "en")
                .header("X-DIGIKEY-Locale-Currency", "USD")
                .header("X-DIGIKEY-ShipToCountry", "us")
                .header("X-DIGIKEY-Customer-Id", "0")
                .header("Authorization", "Bearer " + authService.getToken().orElseThrow().getAccessToken());
    }

}
