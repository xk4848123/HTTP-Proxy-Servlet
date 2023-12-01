package com.zary.sniffer.server.utils;

import ma.glasnost.orika.impl.DefaultMapperFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class BeanUtil {

    private static DefaultMapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();

    /**
     * 转换为指定类型
     *
     * @param <T>   目标类型
     * @param type  类型
     * @param value 值
     * @return 转换后的值
     */
    public static <T> T copyBean(Class<T> type, Object value) {
        return mapperFactory.getMapperFacade().map(value, type);
    }

    /**
     * 转换为Map
     *
     * @param value 值
     * @return 转换后的值
     */
    public static Map<String, Object> toMap(Object value) {
        return mapperFactory.getMapperFacade().map(value, Map.class);
    }

    /**
     * 转换值为List<T>类型
     *
     * @param <T>   目标类型
     * @param type  类型
     * @param value Iterable类型值
     * @return 转换后的值
     */
    public static <T, S> List<T> copyAsList(Class<T> type, Iterable<S> value) {
        return mapperFactory.getMapperFacade().mapAsList(value, type);
    }


    /**
     * 转换为List<T>类型
     *
     * @param <T>   目标类型
     * @param type  类型
     * @param value 数组类型值
     * @return 转换后的值
     */
    public static <T, S> List<T> copyAsList(Class<T> type, S[] value) {

        return mapperFactory.getMapperFacade().mapAsList(value, type);
    }

    /**
     * 转换为Set<T>类型
     *
     * @param <T>   目标类型
     * @param type  类型
     * @param value Iterable类型值
     * @return 转换后的值
     */
    public static <T, S> Set<T> copyAsSet(Class<T> type, Iterable<S> value) {
        return mapperFactory.getMapperFacade().mapAsSet(value, type);
    }

    /**
     * 转换为Set<T>类型
     *
     * @param <T>   目标类型
     * @param type  类型
     * @param value 数组类型值
     * @return 转换后的值
     */
    public static <T, S> Set<T> copyAsSet(Class<T> type, S[] value) {
        return mapperFactory.getMapperFacade().mapAsSet(value, type);
    }

}
