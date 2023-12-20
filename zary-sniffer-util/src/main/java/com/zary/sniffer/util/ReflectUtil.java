package com.zary.sniffer.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectUtil {

    public static Field getDeclareFieldWithParent(Object object, String fieldName) {
        Field field = null;
        Class<?> clazz = object.getClass();
        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                field = clazz.getDeclaredField(fieldName);//找不到抛异常
                return field;
            } catch (Exception e) {
                //do nothing
            }
        }
        return null;
    }


    public static Method getDeclareMethodWithParent(Object object, String methodName) {
        Method method = null;
        Class<?> clazz = object.getClass();
        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                method = clazz.getDeclaredMethod(methodName);//找不到抛异常
                return method;
            } catch (Exception e) {
                //do nothing
            }
        }
        return null;
    }

    public static Method getDeclareMethodWithParent(Object object, String methodName, Class<?> paramTypes) {
        Method method = null;
        Class<?> clazz = object.getClass();
        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                method = clazz.getDeclaredMethod(methodName, paramTypes);//找不到抛异常
                return method;
            } catch (Exception e) {
                //do nothing
            }
        }
        return null;
    }

    public static Object execute(String className, String methodName, Class<?> returnClazz, Object obj, Object... args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> executeClass = Class.forName(className);
        Method executeMethod = executeClass.getDeclaredMethod(methodName, returnClazz);
        executeMethod.setAccessible(true);
        Object result = executeMethod.invoke(obj, args);
        return result;
    }

    public static void setValue(Object obj, Class<?> objClazz, String fieldName, Object finalValue) throws NoSuchFieldException, IllegalAccessException {
        Field field = objClazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, finalValue);
    }

}
