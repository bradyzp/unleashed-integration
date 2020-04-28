package net.jastrab.unleashedintegration.model;

import lombok.Value;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Value
@Document(collection = "UnleashedConfig")
public class UnleashedConfigurationDTO {
    public enum Environment {
        PRODUCTION, SANDBOX
    }

    @Indexed(unique = true)
    private final Environment environment;
    private final String apiId;
    private final String apiKey;

    @PersistenceConstructor
    public UnleashedConfigurationDTO(Environment environment, String apiId, String apiKey) {
        this.environment = environment;
        this.apiId = apiId;
        this.apiKey = apiKey;
    }

}
