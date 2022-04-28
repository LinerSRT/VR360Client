package ru.liner.vr360client.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 27.04.2022, среда
 **/
public class Lists {
    public static <A, B> List<A> filter(List<A> objectList, Comparator<A, B> comparator) {
        List<A> filtered = new ArrayList<>();
        if (!objectList.isEmpty())
            for (int i = 0; i < objectList.size(); i++) {
                if (comparator.compare(objectList.get(i), comparator.other)) {
                    filtered.add(objectList.get(i));
                }
            }
        return filtered;
    }

    public static <A, B> int indexOf(List<A> objectList, Comparator<A, B> comparator){
        for (int i = 0; i < objectList.size(); i++) {
            if(comparator.compare(objectList.get(i), comparator.other))
                return i;
        }
        return -1;
    }

    public static <A, B> boolean contains(List<A> objectList, Comparator<A, B> comparator){
        for (int i = 0; i < objectList.size(); i++) {
            if(comparator.compare(objectList.get(i), comparator.other))
                return true;
        }
        return false;
    }
}
