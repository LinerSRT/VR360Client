package ru.liner.vr360client.recycler.genericadapter;

import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 21.11.2021
 **/
@SuppressWarnings("rawtypes")
public class GenericData {
    @LayoutRes
    private final int layoutRes;
    private final Class binderClass;
    private final Class dataClass;

    public GenericData(int layoutRes, Class binderClass, Class dataClass) {
        this.layoutRes = layoutRes;
        this.binderClass = binderClass;
        this.dataClass = dataClass;
    }

    @LayoutRes
    public int getLayoutRes() {
        return layoutRes;
    }

    public Class getBinderClass() {
        return binderClass;
    }

    public Class getDataClass() {
        return dataClass;
    }

    @Nullable
    public GenericBinder createBinder(@NonNull View view) {
        try {
            GenericBinder binder = (GenericBinder) getBinderClass().newInstance();
            binder.setView(view);
            return binder;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean contain(List<GenericData> list, Object data) {
        for (GenericData genericData : list) {
            if (genericData.getDataClass().getSimpleName().equals(data.getClass().getSimpleName()))
                return true;
        }
        return false;
    }

    @Nullable
    public static GenericData getByLayoutRes(List<GenericData> list, @LayoutRes int layoutRes) {
        for (GenericData genericData : list) {
            if (genericData.getLayoutRes() == layoutRes)
                return genericData;
        }
        return null;
    }
    @Nullable
    public static GenericData getByData(List<GenericData> list, Object data) {
        for (GenericData genericData : list) {
            if (genericData.getDataClass().getSimpleName().equals(data.getClass().getSimpleName()))
                return genericData;
        }
        return null;
    }
}
