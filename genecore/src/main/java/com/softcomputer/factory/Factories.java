package com.softcomputer.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public final class Factories {

    private Factories(){
    }

    public static <TEntity> Factory<TEntity> get(Class<TEntity> clazz) {
        try {
            List<ClassLoader> classLoaders = new ArrayList<ClassLoader>( 3 );
            classLoaders.add( clazz.getClassLoader() );

            if ( Thread.currentThread().getContextClassLoader() != null ) {
                classLoaders.add( Thread.currentThread().getContextClassLoader() );
            }

            classLoaders.add( Factories.class.getClassLoader() );

            return get( clazz, classLoaders );
        }
        catch ( ClassNotFoundException e ) {
            throw new RuntimeException( e );
        }
    }

    private static <TEntity> Factory<TEntity> get(Class<TEntity> factoryType, Iterable<ClassLoader> classLoaders) throws ClassNotFoundException {

        for ( ClassLoader classLoader : classLoaders ) {
            Factory<TEntity> factory = doGetFactory( factoryType, classLoader );
            if ( factory != null ) {
                return factory;
            }
        }

        throw new ClassNotFoundException("Cannot find implementation for " + factoryType.getName() );
    }

    private static <TEntity> Factory<TEntity> doGetFactory(Class<TEntity> clazz, ClassLoader classLoader) {
        try {
            @SuppressWarnings("unchecked")
            Factory<TEntity> factory = (Factory<TEntity>) classLoader.loadClass( clazz.getName()   + "Factory" ).newInstance();
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
