package com.softcomputer.annotationprocessor.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
    String name() default "";
    Class<?> foreignEntity() default void.class;
    String selectQuery() default "";
    String whereClause() default " = ?";
    boolean isUpdated() default true;
    boolean isPrimaryKey() default false;
}
