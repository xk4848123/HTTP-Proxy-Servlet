package com.zary.sniffer.agent.runtime;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class PluginReflectUtil {

    public static void setValue(Object obj, Class<?> objClazz, String fieldName, Object finalValue) throws NoSuchFieldException, IllegalAccessException {
        Field field = objClazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, finalValue);
    }

    public static Object execute(String className, String methodName, Class<?> returnClazz, Object obj, Object... args) {
        try {
            Class<?> executeClass = Class.forName(className);
            Method executeMethod = null;
            try {
                executeMethod = executeClass.getDeclaredMethod(methodName, returnClazz);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                Method[] methods = executeClass.getDeclaredMethods();
                for (Method method : methods) {
                    if (method.getName().equals(methodName)) {
                        executeMethod = method;
                        break;
                    }
                }
            }
            if (executeMethod == null) {
                return null;
            }
            executeMethod.setAccessible(true);
            Object result = executeMethod.invoke(obj, args);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
