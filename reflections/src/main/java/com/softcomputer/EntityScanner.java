package com.softcomputer;

import com.google.common.base.Optional;
import com.softcomputer.annotations.Column;
import com.softcomputer.annotations.MetaData;
import com.softcomputer.annotations.Table;
import org.apache.commons.lang3.StringUtils;

import org.reflections.scanners.AbstractScanner;
import java.lang.reflect.Field;
import java.util.*;

public class EntityScanner extends AbstractScanner {
    public EntityScanner() {
    }

    public void scan(Object obj) {
        try {
            if (obj != null) {
                Class<?> cls = (Class) obj;
                Table table = cls.getAnnotation(Table.class);
                if (table != null) {
                    if (StringUtils.isEmpty(table.name())) throw new RuntimeException();

                    String className = this.getMetadataAdapter().getClassName(cls);
                    List<Field> fields = this.getMetadataAdapter().getFields(cls);
                    fields.addAll(getParentFields(cls.getSuperclass()));
                    Map<String, Optional<Column>> fieldMapping = new HashMap<>();
                    for (Field field : fields) {
                        Column column = field.getAnnotation(Column.class);
                        fieldMapping.put(getFieldKey(field), column != null ? Optional.of(column) : Optional.absent());
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
                        String whereClause = " WHERE ";
                        if (StringUtils.isNotEmpty(table.where())) {
                            whereClause += table.where() + " AND ";
                        }
                        if (primaryKey.isPresent()) {
                            String query = sql + whereClause + primaryKey.get().name() + " = ?";
                            getStore().put(className, query);
                        }
                        if (foreignEntities.size() > 0) {
                            for (Class<?> key : foreignEntities.keySet()) {
                                String query = sql + whereClause + foreignEntities.get(key).name() + " = ?";
                                getStore().put(className + "[" + key.getCanonicalName() + "]", query);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.print(e.getMessage());
            throw e;
        }
    }


    private List<Field> getParentFields(Class<?> cls){
        if(cls != null && !Object.class.equals(cls)) {
            List<Field> ret = this.getMetadataAdapter().getFields(cls);
            ret.addAll(getParentFields(cls.getSuperclass()));
            return ret;
        }
        return new ArrayList<>();
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

