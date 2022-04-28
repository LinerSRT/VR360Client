package ru.liner.vr360client.recycler.genericadapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 21.11.2021
 **/
@SuppressLint("NotifyDataSetChanged")
@SuppressWarnings({"rawtypes", "unchecked", "UnusedReturnValue", "unused"})
public class GenericAdapter extends RecyclerView.Adapter<GenericAdapter.ViewHolder> {
    private final List<Object> dataList;
    private final List<GenericData> genericDataList;
    private final RecyclerView recyclerView;

    public GenericAdapter(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        dataList = new ArrayList<>();
        genericDataList = new ArrayList<>();
    }

    public GenericAdapter register(@LayoutRes int layoutID, Class binderClass, Class dataClass) {
        return register(new GenericData(layoutID, binderClass, dataClass));
    }

    public GenericAdapter register(GenericData genericData) {
        genericDataList.add(genericData);
        return this;
    }

    public void unregister(@LayoutRes int layoutID, Class binderClass, Class dataClass){
        unregister(new GenericData(layoutID, binderClass, dataClass));
    }

    public void unregister(GenericData genericData){
        if(genericDataList.contains(genericData)){
            genericDataList.remove(genericData);
            List<Object> data = new ArrayList<>();
            for(Object currentData:dataList){
                if(!currentData.getClass().getSimpleName().equals(genericData.getDataClass().getSimpleName()))
                    data.add(currentData);
            }
            set(data);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        GenericData genericData = GenericData.getByLayoutRes(genericDataList, viewType);
        if (genericData != null) {
            View view = LayoutInflater.from(parent.getContext()).inflate(genericData.getLayoutRes(), parent, false);
            GenericBinder genericBinder = genericData.createBinder(view);
            if (genericBinder != null) {
                return new ViewHolder(view, genericBinder, genericData);
            } else {
                throw new RuntimeException("Generic binder for data type: " + genericData.getDataClass().getSimpleName() + " is NULL! Did you register this binder?");
            }
        }
        throw new RuntimeException("Generic data for view type: " + viewType + " is NULL!");
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindData(recyclerView, holder, dataList.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        Object data = dataList.get(position);
        if (GenericData.contain(genericDataList, data)) {
            GenericData genericData = GenericData.getByData(genericDataList, data);
            if (genericData != null) {
                return genericData.getLayoutRes();
            } else {
                throw new RuntimeException("Type for: " + data.getClass().getSimpleName() + " not registered! Did you register this binder?");
            }
        } else {
            throw new RuntimeException("Type for: " + data.getClass().getSimpleName() + " not registered! Did you register this binder?");
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }


    public int indexOf(@NonNull Object o) {
        return dataList.indexOf(o);
    }

    public void remove(int index) {
        dataList.remove(index);
        notifyItemRemoved(index);
    }

    public void insert(int index, @NonNull Object o) {
        dataList.add(index, o);
        notifyItemInserted(index);
    }

    public void add(@NonNull Object item) {
        dataList.add(item);
        notifyItemInserted(dataList.size() - 1);
    }

    public void add(@NonNull Object... items) {
        int position = dataList.size();
        dataList.addAll(Arrays.asList(items));
        notifyItemInserted(position);
    }

    public void add(@NonNull Collection<?> items) {
        int position = dataList.size();
        dataList.addAll(items);
        notifyItemInserted(position);
    }

    public void set(@NonNull Object... items) {
        dataList.clear();
        dataList.addAll(Arrays.asList(items));
        notifyDataSetChanged();
    }

    public void set(@NonNull Collection<?> items) {
        dataList.clear();
        dataList.addAll(items);
        notifyDataSetChanged();
    }

    public void swap(int from, int to) {
        if (from < to) {
            for (int i = from; i < to; i++)
                Collections.swap(dataList, i, i + 1);
        } else {
            for (int i = from; i > to; i--)
                Collections.swap(dataList, i, i - 1);
        }
        notifyItemMoved(from, to);
    }

    public void shuffle() {
        Collections.shuffle(dataList);
        notifyDataSetChanged();
    }

    public void clear() {
        dataList.clear();
        notifyDataSetChanged();
    }

    @NonNull
    public <T> T get(int i) {
        return (T) dataList.get(i);
    }

    public <T> List<T> getItemsList() {
        return (List<T>) dataList;
    }


    public static class ViewHolder<T> extends RecyclerView.ViewHolder {
        @NonNull
        private final GenericBinder binder;
        private final GenericData genericData;

        public ViewHolder(@NonNull View itemView, @NonNull GenericBinder<T> binder, GenericData genericData) {
            super(itemView);
            this.binder = binder;
            this.genericData = genericData;
            this.binder.declareViews();
        }

        public void bindData(RecyclerView recyclerView, ViewHolder<T> viewHolder, T data) {
            binder.bindData(recyclerView,viewHolder, data);
        }

        @NonNull
        public GenericBinder<T> getBinder() {
            return binder;
        }

        public GenericData getGenericData() {
            return genericData;
        }
    }
}
