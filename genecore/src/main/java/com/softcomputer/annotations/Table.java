package com.softcomputer.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface Table {
    String name() default "";
    String where() default "";
}
