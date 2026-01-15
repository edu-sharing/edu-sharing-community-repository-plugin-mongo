package org.edu_sharing.plugin_mongo.mongock.driver.springdata.v4.config;

import io.mongock.api.config.MongockConfiguration;
import org.edu_sharing.plugin_mongo.mongock.driver.springdata.v4.SpringDataMongoV4Driver;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class SpringDataMongoV4Context extends SpringDataMongoV4ContextBase<MongockConfiguration, SpringDataMongoV4Driver> {

  @Override
  protected SpringDataMongoV4Driver buildDriver(MongoTemplate mongoTemplate,
                                                MongockConfiguration config,
                                                MongoDBConfiguration mongoDbConfig) {
    return SpringDataMongoV4Driver.withLockStrategy(
        mongoTemplate,
        config.getLockAcquiredForMillis(),
        config.getLockQuitTryingAfterMillis(),
        config.getLockTryFrequencyMillis());
  }

}

