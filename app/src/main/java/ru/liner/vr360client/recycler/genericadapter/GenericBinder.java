package ru.liner.vr360client.recycler.genericadapter;

import android.content.Context;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 21.11.2021
 **/
public abstract class GenericBinder<T> {
    private View view;
    protected Context context;

    public GenericBinder() {
    }

    public void setView(@NonNull View view) {
        this.view = view;
        this.context = view.getContext();
    }

    public <V extends View> V find(@IdRes int id) {
        return view.findViewById(id);
    }

    @NonNull
    public View getView() {
        return view;
    }

    public abstract void declareViews();

    public abstract void bindData(RecyclerView recyclerView, GenericAdapter.ViewHolder<T> viewHolder, T data);
}
