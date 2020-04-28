package net.jastrab.unleashedintegration.configuration;

import net.jastrab.unleashedintegration.service.UnleashedConfigService;
import net.jastrab.unleashedspringclient.UnleashedClientConfigProvider;

public class UnleashedClientConfigMongoProvider implements UnleashedClientConfigProvider {
    private final UnleashedConfigService configService;

    public UnleashedClientConfigMongoProvider(UnleashedConfigService configService) {
        this.configService = configService;
    }

    @Override
    public String getApiId() {
        return configService.getCurrentConfiguration().getApiId();
    }

    @Override
    public void setApiId(String apiId) {

    }

    @Override
    public String getApiKey() {
        return configService.getCurrentConfiguration().getApiKey();
    }

    @Override
    public void setApiKey(String apiKey) {

    }

}
