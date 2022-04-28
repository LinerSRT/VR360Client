package ru.liner.vr360client.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 22.04.2022, пятница
 **/
public class ListTypeToken <T> implements ParameterizedType {
    private final Class<?> clazz;

    public ListTypeToken(Class<T> wrapper) {
        this.clazz = wrapper;
    }

    @Override
    public Type[] getActualTypeArguments() {
        return new Type[]{clazz};
    }

    @Override
    public Type getRawType() {
        return List.class;
    }

    @Override
    public Type getOwnerType() {
        return null;
    }
}