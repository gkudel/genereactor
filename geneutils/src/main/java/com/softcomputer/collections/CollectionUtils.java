package com.softcomputer.collections;

import com.google.common.base.Optional;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class CollectionUtils {
    private  CollectionUtils() {
    }

    public static <T> Optional<T> first(Collection<T> collection, Predict<T> predict){
        Validate.notNull(collection, "collection");
        Validate.notNull(predict, "predict");
        for (T element : collection) {
            if(predict.predict(element)) return Optional.of(element);
        }
        return Optional.absent();
    }

    public static <T> List<T> predict(Collection<T> collection, Predict<T> predict) {
        Validate.notNull(collection, "collection");
        Validate.notNull(predict, "predict");
        List<T> ret = new ArrayList<T>();
        for (T element : collection) {
            if(predict.predict(element)) {
                ret.add(element);
            }
        }
        return ret;
    }

    public static <T> boolean isNotEmpty(Collection<T> collection) {
        return isNotEmpty(collection, new Predict<T>() {
            public boolean predict(T element) {
                return true;
            }
        });
    }

    public static <T> boolean isNotEmpty(Collection<T> collection, Predict<T> predict) {
        Validate.notNull(collection, "collection");
        Validate.notNull(predict, "predict");
        if(collection != null) {
            for (T element : collection) {
                if(predict.predict(element)) return true;
            }
        }
        return false;
    }

    public interface Predict<T> {
        boolean predict(T element);
    }
}
