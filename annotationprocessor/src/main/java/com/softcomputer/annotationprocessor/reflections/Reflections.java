package com.softcomputer.annotationprocessor.reflections;

import com.google.common.collect.Multimap;
import com.softcomputer.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.Collection;
import com.google.common.base.Optional;
import org.reflections.Configuration;

public class Reflections {
    private static Reflections instance;

    private org.reflections.Reflections reflections;
    private Reflections(org.reflections.Reflections reflections) {
        Validate.notNull(reflections);
        this.reflections = reflections;
    }

    public static Reflections collect() {
        if(instance == null) init();
        return instance;
    }

    private synchronized static void init() {
        if(instance == null) {
            instance = new Reflections(org.reflections.Reflections.collect());
        }
    }

    public static Scanner scann(ConfigurationBuilder builder) {
        Validate.notNull(builder);
        return new Scanner(builder.build());
    }

    public static class Scanner {
        private org.reflections.Reflections reflections;

        private Scanner(Configuration configuration) {
            Validate.notNull(configuration);
            reflections = new org.reflections.Reflections(configuration);
        }

        public void save(String baseDir) {
            Validate.notEmpty(baseDir);
            reflections.save(baseDir);
        }

        public void save(String baseDir, String fileName) {
            Validate.notEmpty(baseDir);
            Validate.notEmpty(fileName);
            reflections.save(baseDir + fileName);
        }
    }

    public Optional<String> getRawQuery(Class<?> type) {
        Validate.notNull(type);
        return getQuery(type.getName());
    }

    public Optional<String> getQuery(Class<?> type) {
        Validate.notNull(type);
        return getQuery(type.getName(), getKey(type));
    }

    public Optional<String> getQuery(Class<?> type, Class<?> foreignEntity) {
        Validate.notNull(type);
        Validate.notNull(foreignEntity);
        return getQuery(type.getName(), getKey(type, foreignEntity));
    }

    private String getKey(Class<?>... types) {
        Validate.notNull(types);
        Validate.validState(types.length > 0);

        String key = types[0].getName();
        if(types.length > 1) {
            String foreignTypes = StringUtils.EMPTY;
            for (int i=1; i<types.length; i++) {
                if(StringUtils.isEmpty(foreignTypes)) foreignTypes = types[i].getName();
                else foreignTypes += "," + types[i].getName();
            }
            key += "["+foreignTypes+"]";
        } else {
            key += "["+EntityScanner.ID_KEY+"]";
        }
        return key;
    }

    private Optional<String> getQuery(String... keys) {
        Optional<String> query = Optional.absent();
        if(reflections.getStore().keySet().contains(EntityScanner.class.getSimpleName())) {
            Multimap<String, String> queriesMap = reflections.getStore().get(EntityScanner.class.getSimpleName());
            if (queriesMap != null) {
                if(ArrayUtils.isNotEmpty(keys)) {
                    String ret = StringUtils.EMPTY;
                    for (String key: keys) {
                        if (queriesMap.containsKey(key)) {
                            Collection<String> queries = queriesMap.get(key);
                            if (queries.size() > 1) throw new RuntimeException();
                            Optional<String> queryOptional = CollectionUtils.first(queries);
                            if(!queryOptional.isPresent()) throw new RuntimeException("Invalid key["+key+"]");
                            if (queries.size() == 1) ret += queryOptional.get();
                        }
                    }
                    query = Optional.of(ret);
                }
            }
        }
        return query;
    }
}
