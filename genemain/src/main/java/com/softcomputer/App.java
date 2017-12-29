package com.softcomputer;

import com.google.common.base.Optional;
import com.softcomputer.annotationprocessor.orm.Mapper;
import com.softcomputer.annotationprocessor.orm.Mappers;
import com.softcomputer.annotationprocessor.reflections.EntityScanner;
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
        Optional<String> queryRawTestNgs = reflections.getRawReadSql(TestNgs.class);
        Optional<String> queryTestNgs = reflections.getSql(TestNgs.class, EntityScanner.QueryId.READBYID );
        Optional<String> queryOrderTestNgs = reflections.getReadSql(TestNgs.class, Order.class);
        Optional<String> deleteTestNgs = reflections.getSql(TestNgs.class, EntityScanner.QueryId.DELETE );
    }
}
