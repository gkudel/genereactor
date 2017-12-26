package com.softcomputer;

import com.google.common.collect.Multimap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.util.Collection;
import com.google.common.base.Optional;

public class Reflections {
    private static Reflections instance;

    private org.reflections.Reflections reflections;
    private Reflections(org.reflections.Reflections reflections) {
        Validate.notNull(reflections);
        this.reflections = reflections;
    }

    public Reflections(String packageName)  {
        Validate.notEmpty(packageName);
         reflections = new org.reflections.Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(packageName))
                .setMetadataAdapter(new EntityMetadataAdapter())
                .setScanners(new EntityScanner())
        );
    }

    public static Reflections collect() {
        if(instance == null) init();
        return instance;
    }

    private synchronized static void init() {
        if(instance == null) {
            instance = new Reflections(org.reflections.Reflections.collect());;
        }
    }

    public void save(String fileName) {
        Validate.notEmpty(fileName);
        reflections.save(fileName);
    }

    public Optional<String> getQuery(Class<?> type) {
        Validate.notNull(type);
        return getQuery(getKey(type));
    }

    public Optional<String> getQuery(Class<?> type, Class<?> foreignEntity) {
        Validate.notNull(type);
        Validate.notNull(foreignEntity);
        return getQuery(getKey(type, foreignEntity));
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
        }
        return key;
    }

    private Optional<String> getQuery(String key) {
        Optional<String> query = Optional.absent();
        if(reflections.getStore().keySet().contains(EntityScanner.class.getSimpleName())) {
            Multimap<String, String> queriesMap = reflections.getStore().get(EntityScanner.class.getSimpleName());
            if (queriesMap != null) {
                if (queriesMap.containsKey(key)) {
                    Collection<String> queries = queriesMap.get(key);
                    if (queries.size() > 1) throw new RuntimeException();
                    if (queries.size() == 1) query = Optional.of(queries.iterator().next());
                }
            }
        }
        return query;
    }
}
