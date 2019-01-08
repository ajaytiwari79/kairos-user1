package com.kairos.commons.utils;

import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author pradeep
 * @date - 8/6/18
 */

public class ObjectUtils {


    public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor)
    {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    public static boolean isCollectionEmpty(@Nullable Collection<?> collection){
        return (collection == null || collection.isEmpty());
    }

    public static boolean isCollectionNotEmpty(@Nullable Collection<?> collection){
        return !(collection == null || collection.isEmpty());
    }
    public static boolean isEmpty(@Nullable Map<?, ?> map) {
        return (map == null || map.isEmpty());
    }
    public static boolean isNotEmpty(@Nullable Map<?, ?> map) {
        return !isEmpty(map);
    }

    public static <T> boolean isNull(T object){
        return !Optional.ofNullable(object).isPresent();
    }

    public static <T> boolean isNotNull(T object){
        return Optional.ofNullable(object).isPresent();
    }

    public static <E> HashSet<E> newHashSet(E... elements) {
        HashSet<E> set = new HashSet<>(elements.length);
        Collections.addAll(set, elements);
        return set;
    }

}
