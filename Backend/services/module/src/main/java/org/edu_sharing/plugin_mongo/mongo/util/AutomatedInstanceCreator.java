package org.edu_sharing.plugin_mongo.mongo.util;

import org.bson.codecs.configuration.CodecConfigurationException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * The instance is created by the constructor in which most of the parameters match the serialized object.
 * The names of the parameters and the fields of the serialized object must be the same.
 */
public class AutomatedInstanceCreator<T> extends BufferedInstanceCreator<T> {

    private final Class<T> clazz;

    public AutomatedInstanceCreator(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public T getInstance() {
        // sorts constructor according to the number of parameters in descending order
        List<Constructor<?>> constructors = Arrays.stream(clazz.getConstructors())
                .sorted((x, y) -> Integer.compare(y.getParameterCount(), x.getParameterCount()))
                .collect(Collectors.toList());

        // find the first constructor in which all params are specified
        List<Object> paramValues = new ArrayList<>();
        for (Constructor<?> constructor : constructors) {
            paramValues.clear();
            Parameter[] params = constructor.getParameters();
            boolean constructorFound = true;
            for (Parameter param : params) {

                // check if we have a value stored under the parameter name
                if (buffer.containsKey(param.getName())) {
                    constructorFound = false;
                    break;
                }

                Object value = buffer.get(param.getName());

                // check if the value can be null otherwise skip this constructor
                if(param.getAnnotation(NotNull.class) != null && value == null){
                    constructorFound = false;
                    break;
                }

                paramValues.add(buffer.get(param.getName()));
            }

            if (!constructorFound) {
                continue;
            }

            try {
                return (T) constructor.newInstance(paramValues);
            } catch (Exception e) {
                throw new CodecConfigurationException(e.getMessage(), e);
            }
        }

        throw new CodecConfigurationException("No constructor found for " + clazz.getName());
    }
}
