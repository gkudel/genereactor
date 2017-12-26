package com.softcomputer.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface MetaData {
    Class<?> type() default void.class;
}
