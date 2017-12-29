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
    public enum QueryId {
        UNDEFIEND("Undefined"),
        READBYID("ById"),
        DELETE("Delete"),
        INSERT("Insert"),
        UPDATE("Update");

        private final String id;
        QueryId(String id) {
            this.id = id;
        }

        public String getId() {
            return  id;
        }
    }
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
                Map<String, Optional<Column>> fieldsMapping = new HashMap<String, Optional<Column>>();
                Optional<Column> primaryKey = Optional.absent();
                for (Field field : fields) {
                    Column column = field.getAnnotation(Column.class);
                    fieldsMapping.put(getFieldKey(field), column != null ? Optional.of(column) : Optional.<Column>absent());
                }
                parseMetaData(cls, fieldsMapping);
                for (String key : fieldsMapping.keySet()) {
                    Optional<Column> columnOptional = fieldsMapping.get(key);
                    if(columnOptional.isPresent() && columnOptional.get().isPrimaryKey()) {
                        if (primaryKey.isPresent()) throw new RuntimeException();
                        primaryKey = columnOptional;
                    }
                }
                generateReadQueries(className, table, fieldsMapping, primaryKey);
                generateDeleteQuery(className, table, primaryKey);
                generateInsertQuery(className, table, fieldsMapping);
                generateUpdateQuery(className, table, fieldsMapping, primaryKey);
            }
        }
    }

    private void generateReadQueries(String className, Table table, Map<String, Optional<Column>> fieldsMapping, Optional<Column> primaryKey ) {
        String sql = StringUtils.EMPTY;
        Map<Class<?>, Column> foreignEntities = new HashMap<Class<?>, Column>();
        for (String field : fieldsMapping.keySet()) {
            Optional<Column> optionalColumn = fieldsMapping.get(field);
            if (optionalColumn.isPresent()) {
                Column column = optionalColumn.get();
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
                getStore().put(className + "["+QueryId.READBYID.getId()+"]", queryPrimaryKey);

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

    private void generateDeleteQuery(String className, Table table, Optional<Column> primaryKey ) {
        if(primaryKey.isPresent()) {
            String sql = "DELETE FROM " + table.name() + " WHERE " + primaryKey.get().name() + " = ? ";
            getStore().put(className + "[" + QueryId.DELETE.getId() + "]", sql);
        }
    }

    private void generateInsertQuery(String className, Table table, Map<String, Optional<Column>> fieldsMapping ) {
        String sql = "INSERT INTO " + table.name();
        String columns = StringUtils.EMPTY;
        String values = StringUtils.EMPTY;
        for (String key : fieldsMapping.keySet()) {
            Optional<Column> columnOptional = fieldsMapping.get(key);
            if(columnOptional.isPresent() && columnOptional.get().isUpdated()) {
                if(StringUtils.isEmpty(columns))  {
                    columns = columnOptional.get().name();
                    values = " ? ";
                } else {
                    columns += ", " + columnOptional.get().name();
                    values += ", ? ";
                }
            }
        }
        if(!StringUtils.isEmpty(columns)) {
            sql = sql + "(" + columns + ") VALUES(" + values + ")";
            getStore().put(className + "[" + QueryId.INSERT.getId() + "]", sql);
        }
    }

    private void generateUpdateQuery(String className, Table table,
                                      Map<String, Optional<Column>> fieldsMapping, Optional<Column> primaryKey) {
        if(primaryKey.isPresent()) {
            String sql = "UPDATE " + table.name() + " SET ";
            String columns = StringUtils.EMPTY;
            for (String key : fieldsMapping.keySet()) {
                Optional<Column> columnOptional = fieldsMapping.get(key);
                if (columnOptional.isPresent() && columnOptional.get().isUpdated() && !columnOptional.get().isPrimaryKey()) {
                    if (StringUtils.isEmpty(columns)) {
                        columns = columnOptional.get().name() + " = ? ";
                    } else {
                        columns +=  ", " + columnOptional.get().name() + " = ? ";
                    }
                }
            }
            if (!StringUtils.isEmpty(columns)) {
                sql = sql + columns + " WHERE " + primaryKey.get().name() + " = ?";
                getStore().put(className + "[" + QueryId.UPDATE.getId() + "]", sql);
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

