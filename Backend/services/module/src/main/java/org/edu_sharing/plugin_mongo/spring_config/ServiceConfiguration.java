package org.edu_sharing.plugin_mongo.spring_config;

import org.edu_sharing.plugin_mongo.integrity.IntegrityService;
import org.edu_sharing.plugin_mongo.integrity.IntegrityServiceImpl;
import org.edu_sharing.service.authority.AuthorityService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfiguration {

    @Bean
    public IntegrityService integrityService(AuthorityService authorityService){
        return  new IntegrityServiceImpl(authorityService);
    }
}
