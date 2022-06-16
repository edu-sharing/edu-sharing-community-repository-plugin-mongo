package org.edu_sharing.plugin_mongo.mongo.automation;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.edu_sharing.plugin_mongo.mongo.automation.annotation.Initialize;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Aspect
@Component
public class MongoDBRepositoryInitializationAspect {

    public static final String SETUP_INDICES = "initialize";

    //@Before("public * execution(@org.edu_sharing.plugin_mongo.*Repository.*(..)")
    @Before("bean(*Repository) && within(org.edu_sharing.plugin_mongo..*)")
    public void initializeRepositoryJoinPoint(JoinPoint joinPoint) throws InvocationTargetException, IllegalAccessException {

        Class<?> targetClass = joinPoint.getTarget().getClass();
        List<Method> methods = Arrays.stream(targetClass.getMethods())
                .filter(x -> x.getName().equals(SETUP_INDICES) && x.getParameters().length == 0 || x.getAnnotation(Initialize.class) != null)
                .collect(Collectors.toList());

        for (Method method : methods) {
            method.invoke(joinPoint.getTarget());
        }
    }
}
