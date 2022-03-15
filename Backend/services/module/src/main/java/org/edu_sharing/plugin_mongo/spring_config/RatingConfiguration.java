package org.edu_sharing.plugin_mongo.spring_config;

import org.edu_sharing.plugin_mongo.rating.IntegrityService;
import org.edu_sharing.plugin_mongo.rating.IntegrityServiceImpl;
import org.edu_sharing.service.authority.AuthorityService;
import org.edu_sharing.service.permission.PermissionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RatingConfiguration {

    @Bean
    public IntegrityService integrityService(AuthorityService authorityService){
        return  new IntegrityServiceImpl(authorityService);
    }
}
