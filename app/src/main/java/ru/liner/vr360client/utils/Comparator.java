package ru.liner.vr360client.utils;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 27.04.2022, среда
 **/
public abstract class Comparator<A, B> {
    public final B other;
    public Comparator(B other) {
        this.other = other;
    }
    public abstract boolean compare(A one, B other);
}