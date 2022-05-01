package com.belka.jmxhttp.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClassUtils {

    public static Object newInstance(Class<?> clazz, String value) {
        if (clazz.equals(Integer.class) || clazz.equals(int.class)) {
            return Integer.parseInt(value);
        }
        if (clazz.equals(String.class)) {
            return value;
        }
        if (clazz.equals(Long.class) || clazz.equals(long.class)) {
            return Long.valueOf(value);
        }
        if (clazz.equals(Short.class) || clazz.equals(short.class)) {
            return Short.valueOf(value);
        }
        if (clazz.equals(Float.class) || clazz.equals(float.class)) {
            return Float.valueOf(value);
        }
        if (clazz.equals(Double.class) || clazz.equals(double.class)) {
            return Double.valueOf(value);
        }
        if (clazz.equals(BigDecimal.class)) {
            return BigDecimal.valueOf(Double.valueOf(value));
        }
        if (clazz.equals(Byte.class) || clazz.equals(byte.class)) {
            return Byte.valueOf(value);
        }
        if (clazz.equals(Character.class) || clazz.equals(byte.class)) {
            if (value.length() > 1) {
                throw new IllegalArgumentException("char argument is not correct");
            }
            return Character.valueOf(value.charAt(0));
        }
        if (clazz.equals(Boolean.class) || clazz.equals(boolean.class)) {
            return Boolean.valueOf(value);
        }
        throw new IllegalArgumentException("incorrect argument type with class " + clazz.getClass().getName());
    }

    public static Class<?> parseType(String className) {
        switch (className) {
            case "boolean":
                return boolean.class;
            case "byte":
                return byte.class;
            case "short":
                return short.class;
            case "int":
                return int.class;
            case "long":
                return long.class;
            case "float":
                return float.class;
            case "double":
                return double.class;
            case "char":
                return char.class;
            case "void":
                return void.class;
            default:
                String fqn = className.contains(".") ? className : "java.lang.".concat(className);
                try {
                    return Class.forName(fqn);
                } catch (ClassNotFoundException ex) {
                    throw new IllegalArgumentException("Class not found: " + fqn);
                }
        }
    }
}
