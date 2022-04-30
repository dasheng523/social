package com.mengxinya.ys.sql;

import org.springframework.beans.BeanUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class ClassUtils {
    public static Object getObjFieldVal(Object proxy, String field) {
        try {
            if (isRecord(proxy.getClass())) {
                return proxy.getClass().getDeclaredMethod(field).invoke(proxy);
            }
            else {
                return proxy.getClass().getDeclaredMethod("get" + SqlUtils.captureName(field)).invoke(proxy);
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new DataRepositoryException("获取字段值失败", e);
        }
    }

    public static void setObjFieldVal(Object proxy, String field, Object val) {
        try {
            proxy.getClass()
                    .getDeclaredMethod(
                            "set" + SqlUtils.captureName(field),
                            proxy.getClass().getDeclaredField(field).getType()
                    )
                    .invoke(proxy, val);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | NoSuchFieldException e) {
            throw new DataRepositoryException("设置字段值失败", e);
        }
    }

    public static <T> T initObject(Class<T> tClass, Map<String, Object> dataMap) {
        if (isRecord(tClass)) {
            Constructor<?>[] constructors = tClass.getConstructors();
            Constructor<?> constructor = constructors.length == 1 ? constructors[0] : null;
            if (constructor == null) {
                throw new DataRepositoryException("tClass的构造方法有问题");
            }
            Object[] params = new Object[dataMap.size()];
            Field[] methods = tClass.getDeclaredFields();
            for (int i = 0; i < params.length; i++) {
                params[i] = dataMap.get(methods[i].getName());
            }
            try {
                return tClass.cast(constructor.newInstance(params));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new DataRepositoryException("初始化tClass失败", e);
            }
        }
        else {
            T mappedObject = BeanUtils.instantiateClass(tClass);
            for (String key : dataMap.keySet()) {
                setObjFieldVal(mappedObject, key, dataMap.get(key));
            }
            return mappedObject;
        }
    }

    public static boolean isRecord(Class<?> cls) {
        return cls.getSuperclass().getName().equals("java.lang.Record");
    }
}
