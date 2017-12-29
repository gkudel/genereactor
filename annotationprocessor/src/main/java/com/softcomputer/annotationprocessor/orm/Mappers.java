package com.softcomputer.annotationprocessor.orm;

import com.softcomputer.annotationprocessor.MapperAnnotationProcessor;

import java.util.ArrayList;
import java.util.List;

public final class Mappers {

    private Mappers(){
    }

    public static <TEntity> Mapper<TEntity> get(Class<TEntity> clazz) {
        try {
            List<ClassLoader> classLoaders = new ArrayList<ClassLoader>( 3 );
            classLoaders.add( clazz.getClassLoader() );

            if ( Thread.currentThread().getContextClassLoader() != null ) {
                classLoaders.add( Thread.currentThread().getContextClassLoader() );
            }

            classLoaders.add( Mappers.class.getClassLoader() );

            return get( clazz, classLoaders );
        }
        catch ( ClassNotFoundException e ) {
            throw new RuntimeException( e );
        }
    }

    private static <TEntity> Mapper<TEntity> get(Class<TEntity> factoryType, Iterable<ClassLoader> classLoaders) throws ClassNotFoundException {

        for ( ClassLoader classLoader : classLoaders ) {
            Mapper<TEntity> factory = doGetFactory( factoryType, classLoader );
            if ( factory != null ) {
                return factory;
            }
        }

        throw new ClassNotFoundException("Cannot find implementation for " + factoryType.getName() );
    }

    private static <TEntity> Mapper<TEntity> doGetFactory(Class<TEntity> clazz, ClassLoader classLoader) {
        try {
            @SuppressWarnings("unchecked")
            Mapper<TEntity> factory = (Mapper<TEntity>) classLoader.loadClass( clazz.getName()   + MapperAnnotationProcessor.NAME_POSTFIX).newInstance();
            return factory;
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException( e );
        }
        catch (InstantiationException e) {
            throw new RuntimeException( e );
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException( e );
        }
    }
}
