package com.softcomputer.annotationprocessor.reflections;

import com.google.common.base.Optional;
import com.softcomputer.annotationprocessor.annotations.Column;
import com.softcomputer.annotationprocessor.annotations.MetaData;
import com.softcomputer.annotationprocessor.annotations.Table;
import org.apache.commons.lang3.StringUtils;

import org.reflections.scanners.AbstractScanner;
import java.lang.reflect.Field;
import java.util.*;

public class EntityScanner extends AbstractScanner {
    protected static final String ID_KEY = "byId";
    public EntityScanner() {
    }

    public void scan(Object obj) {
        if (obj != null) {
            Class<?> cls = (Class) obj;
            Table table = cls.getAnnotation(Table.class);
            if (table != null) {
                if (StringUtils.isEmpty(table.name())) throw new RuntimeException();

                String className = this.getMetadataAdapter().getClassName(cls);
                List<Field> fields = this.getMetadataAdapter().getFields(cls);
                fields.addAll(getParentFields(cls.getSuperclass()));
                Map<String, Optional<Column>> fieldMapping = new HashMap<String, Optional<Column>>();
                for (Field field : fields) {
                    Column column = field.getAnnotation(Column.class);
                    fieldMapping.put(getFieldKey(field), column != null ? Optional.of(column) : Optional.<Column>absent());
                }
                parseMetaData(cls, fieldMapping);
                Map<Class<?>, Column> foreignEntities = new HashMap<Class<?>, Column>();

                String sql = StringUtils.EMPTY;
                Optional<Column> primaryKey = Optional.absent();
                for (String field : fieldMapping.keySet()) {
                    Optional<Column> optionalColumn = fieldMapping.get(field);
                    if (optionalColumn.isPresent()) {
                        Column column = optionalColumn.get();
                        if (column.isPrimaryKey()) {
                            if (primaryKey.isPresent()) throw new RuntimeException();
                            primaryKey = Optional.of(column);
                        }
                        if (!void.class.equals(column.foreignEntity())) {
                            if (foreignEntities.containsKey(column.foreignEntity())) throw new RuntimeException();
                            foreignEntities.put(column.foreignEntity(), column);
                        }
                        if (StringUtils.isEmpty(sql)) sql = column.name();
                        else sql += ", " + column.name();
                    }
                }
                if (StringUtils.isNotEmpty(sql)) {
                    sql = "SELECT " + sql;
                    sql += " FROM " + table.name();
                    if (StringUtils.isNotEmpty(table.where())) {
                        sql += " WHERE " +table.where();
                    }
                    getStore().put(className, sql);

                    if (primaryKey.isPresent()) {
                        String queryPrimaryKey = primaryKey.get().name() + primaryKey.get().whereClause();
                        if (StringUtils.isEmpty(table.where())) {
                            queryPrimaryKey = " WHERE " + queryPrimaryKey;
                        } else {
                            queryPrimaryKey = " AND " + queryPrimaryKey;
                        }
                        getStore().put(className + "["+ID_KEY+"]", queryPrimaryKey);
                    }
                    if (foreignEntities.size() > 0) {
                        for (Class<?> key : foreignEntities.keySet()) {
                            String query = foreignEntities.get(key).name() + foreignEntities.get(key).whereClause();
                            if (StringUtils.isEmpty(table.where())) {
                                query = " WHERE " + query;
                            } else {
                                query = " AND " + query;
                            }
                            getStore().put(className + "[" + key.getCanonicalName() + "]", query);
                        }
                    }
                }
            }
        }
    }


    private List<Field> getParentFields(Class<?> cls){
        if(cls != null && !Object.class.equals(cls)) {
            List<Field> ret = this.getMetadataAdapter().getFields(cls);
            ret.addAll(getParentFields(cls.getSuperclass()));
            return ret;
        }
        return new ArrayList<Field>();
    }

    private void parseMetaData(Class<?> cls, Map<String, Optional<Column>> fieldsMapping) {
        MetaData metaData = cls.getAnnotation(MetaData.class);
        if(metaData != null && !void.class.equals(metaData.type())) {
            List<Field> metaDataFields = this.getMetadataAdapter().getFields(metaData.type());
            for (Field field: metaDataFields) {
                String key = getFieldKey(field);
                if(fieldsMapping.containsKey(key) && !fieldsMapping.get(key).isPresent()) {
                    Column column = field.getAnnotation(Column.class);
                    if(column != null) {
                        fieldsMapping.put(key, Optional.of(column));
                    }
                }
            }
        }
    }

    private String getFieldKey(Field field) {
        return field.getName();
    }
}

