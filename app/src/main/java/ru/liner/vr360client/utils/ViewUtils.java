package ru.liner.vr360client.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;

import java.util.Objects;

@SuppressWarnings("unchecked")
public class ViewUtils {

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int dpToPx(float dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(float px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToSp(float px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().scaledDensity);
    }

    public static int pxToSp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().scaledDensity);
    }

    public static int dpToSp(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().scaledDensity);
    }

    public static int dpToSp(float dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().scaledDensity);
    }


    public static void setMargins(@NonNull View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }

    public static <T extends View> T inflate(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent, @LayoutRes int layoutResId) {
        return (T) (inflater.inflate(layoutResId, parent, false));
    }

    public static <T extends View> T findById(@NonNull View parent, @IdRes int resId) {
        return (T) parent.findViewById(resId);
    }

    public static <T extends View> T findById(@NonNull Activity parent, @IdRes int resId) {
        return (T) parent.findViewById(resId);
    }

    public static int getStatusBarSize(@NonNull Context context) {
        final Resources resources = context.getResources();
        final int resourceId = resources.getIdentifier(
                "status_bar_height",
                "dimen",
                "android"
        );
        return resources.getDimensionPixelSize(resourceId);
    }


    public static int getNavigationBarSize(@NonNull Context context) {
        final Resources resources = context.getResources();
        final int resourceId = resources.getIdentifier(
                "navigation_bar_height",
                "dimen",
                "android"
        );
        return resources.getDimensionPixelSize(resourceId);
    }

    public static void setStatusBarBackground(@NonNull Activity activity, @DrawableRes int resourceID) {
        setStatusBarBackground(activity, Objects.requireNonNull(ContextCompat.getDrawable(activity, resourceID)));
    }

    public static void setStatusBarBackground(@NonNull Activity activity, @NonNull Drawable drawable) {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(activity.getResources().getColor(android.R.color.transparent));
        window.setNavigationBarColor(activity.getResources().getColor(android.R.color.transparent));
        window.setBackgroundDrawable(drawable);
    }

    public static void setStatusBarBackground(@NonNull Activity activity, @NonNull Bitmap bitmap) {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(activity.getResources().getColor(android.R.color.transparent));
        window.setNavigationBarColor(activity.getResources().getColor(android.R.color.transparent));
        window.setBackgroundDrawable(new BitmapDrawable(activity.getResources(), bitmap));
    }

    public static void setStatusBarColor(@NonNull Activity activity, @ColorInt int color) {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(color);
    }
    public static void setNavigationBarColor(@NonNull Activity activity, @ColorInt int color) {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setNavigationBarColor(color);
    }

    public static int getStatusBarColor(@NonNull Activity activity){
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        return window.getStatusBarColor();
    }

    public static void startActivityTransition(Activity activity, View view, Intent intent, String transitionName){
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, view, transitionName);
        activity.startActivity(intent, options.toBundle());
    }

}
