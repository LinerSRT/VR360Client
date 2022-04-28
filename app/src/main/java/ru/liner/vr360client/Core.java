package ru.liner.vr360client;

import android.app.Application;
import android.content.Context;

import ru.liner.vr360client.utils.PM;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 22.04.2022, пятница
 **/
public class Core extends Application {
    private static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        PM.init(this);
    }

    public static Context getContext() {
        return context;
    }
}
