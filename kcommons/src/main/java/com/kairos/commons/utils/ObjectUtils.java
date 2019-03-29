package com.kairos.commons.utils;

import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
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

    public static <T> T isNullOrElse(T object,T elseObject){
        return Optional.ofNullable(object).orElse(elseObject);
    }

    public static <T> boolean isNotNull(T object){
        return Optional.ofNullable(object).isPresent();
    }

    public static <E> HashSet<E> newHashSet(E... elements) {
        HashSet<E> set = new HashSet<>(elements.length);
        Collections.addAll(set, elements);
        return set;
    }

    //Due to UnsupportedMethodException on calling add method of Arrays.asList
    public static <E> List<E> newArrayList(E... elements) {
        List<E> list = new ArrayList<>(elements.length);
        Collections.addAll(list, elements);
        return list;
    }

    public static float getHoursByMinutes(int hour){
        if(hour==0){
            throw new RuntimeException("Hour should not be 0");
        }
        int hours = hour / 60; //since both are ints, you get an int
        int minutes = hour % 60;
        return Float.valueOf(hours+"."+minutes);
    }

    private void test(){
        /*ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();
        Set<ConstraintViolation<List<Searching>> violations = validator.validate(searchingList);*/
    }



}
