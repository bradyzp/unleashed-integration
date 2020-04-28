package net.jastrab.unleashedintegration.configuration;

import lombok.extern.slf4j.Slf4j;
import net.jastrab.unleashedintegration.model.OAuthTokenDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.stereotype.Component;

@Slf4j
//@Component
public class MongoIndexConfigComponent {
    private final MongoTemplate mongoTemplate;
    private final MongoMappingContext mappingContext;

    @Autowired
    public MongoIndexConfigComponent(MongoTemplate mongoTemplate, MongoMappingContext mappingContext) {
        this.mongoTemplate = mongoTemplate;
        this.mappingContext = mappingContext;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initIndicesAfterStartup() {
        log.info("Initializing MongoDB Indexes");
        final IndexOperations indexOperations = mongoTemplate.indexOps(OAuthTokenDTO.class);
        final IndexResolver resolver = new MongoPersistentEntityIndexResolver(mappingContext);

        resolver.resolveIndexFor(OAuthTokenDTO.class).forEach(indexOperations::ensureIndex);
    }
}
