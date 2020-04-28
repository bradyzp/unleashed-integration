package net.jastrab.unleashedintegration.service;

import lombok.extern.slf4j.Slf4j;
import net.jastrab.unleashedintegration.model.UnleashedConfigurationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UnleashedConfigService {
    private final MongoTemplate mongoTemplate;
    private UnleashedConfigurationDTO.Environment currentEnv = UnleashedConfigurationDTO.Environment.SANDBOX;

    @Autowired
    public UnleashedConfigService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Load an UnleashedConfigurationDTO into the store
     */
    public void loadConfiguration(UnleashedConfigurationDTO configuration) {
        mongoTemplate.findAndReplace(Query.query(Criteria.where("environment").is(configuration.getEnvironment())),
                configuration, FindAndReplaceOptions.options().upsert());
    }

    public UnleashedConfigurationDTO getConfiguration(UnleashedConfigurationDTO.Environment environment) {
        return mongoTemplate.findOne(Query.query(Criteria.where("environment").is(environment)), UnleashedConfigurationDTO.class);
    }

    public UnleashedConfigurationDTO getCurrentConfiguration() {
        return getConfiguration(currentEnv);
    }

    public UnleashedConfigurationDTO.Environment setEnvironment(UnleashedConfigurationDTO.Environment environment) {
        log.info("Setting current unleashed environment to {}", environment);
        this.currentEnv = environment;
        return this.currentEnv;
    }


}
