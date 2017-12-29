package com.softcomputer;

import com.google.common.base.Optional;
import com.softcomputer.annotationprocessor.orm.Mapper;
import com.softcomputer.annotationprocessor.orm.Mappers;
import com.softcomputer.annotationprocessor.reflections.Reflections;
import com.softcomputer.model.Order;
import com.softcomputer.model.Test;
import com.softcomputer.ngs.model.TestNgs;

/**
 * Hello world!
 *
 */
public class App {
    public static void main( String[] args ) {
        Mapper<TestNgs> factory = Mappers.get(TestNgs.class);
        Reflections reflections = Reflections.collect();
        Optional<String> queryTestNgs = reflections.getQuery(TestNgs.class);
        Optional<String> queryRawTestNgs = reflections.getRawQuery(TestNgs.class);
        Optional<String> queryOrderTestNgs = reflections.getQuery(TestNgs.class, Order.class);
    }
}
