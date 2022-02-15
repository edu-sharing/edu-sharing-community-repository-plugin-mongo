package org.edu_sharing.plugin_mongo.spring_config;

import com.mongodb.client.MongoDatabase;
import org.edu_sharing.plugin_mongo.rating.RatingIntegrityService;
import org.edu_sharing.plugin_mongo.rating.RatingIntegrityServiceImpl;
import org.edu_sharing.plugin_mongo.rating.RatingServiceImpl;
import org.edu_sharing.service.authority.AuthorityService;
import org.edu_sharing.service.authority.AuthorityServiceFactory;
import org.edu_sharing.service.permission.PermissionService;
import org.edu_sharing.service.rating.RatingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RatingConfiguration {

//    @Bean(name = "ratingService")
//    public RatingService ratingService(MongoDatabase mongoDatabase, RatingIntegrityService integrityService){
//        return new RatingServiceImpl(mongoDatabase, integrityService);
//    }

    @Bean
    public RatingIntegrityService ratingIntegrityService(AuthorityService authorityService, PermissionService permissionService){
        return  new RatingIntegrityServiceImpl(authorityService, permissionService);
    }

}
