package org.edu_sharing.plugin_mongo.mongo.automation.annotation;

import java.lang.annotation.*;

/**
 * Used to indicate a method to be used to initialize a MongoDB repo.
 * This can be used to create indices and collections
 * It will be called before executing public methods of classes ending with Repository in
 * the edu.sharing.plugin_mongo packages and subpackages namespace
 * by {@link org.edu_sharing.plugin_mongo.mongo.automation.MongoDBRepositoryInitializationAspect}
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Initialize {
}
