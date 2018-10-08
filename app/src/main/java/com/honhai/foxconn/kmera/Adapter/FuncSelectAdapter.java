package com.honhai.foxconn.kmera.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.honhai.foxconn.kmera.R;
import com.honhai.foxconn.kmera.Tools.FuncSelectListener;

import java.util.LinkedHashMap;

public class FuncSelectAdapter extends RecyclerView.Adapter<FuncSelectAdapter.ViewHolder> {

    private final String TAG = "FuncSelectAdapter";
    private Context context;
    private LinkedHashMap<Integer, Integer> functionMap; // store image resource id
    private FuncSelectListener funcSelectListener;
    private int width;

    public FuncSelectAdapter(LinkedHashMap<Integer, Integer> map) {
        functionMap = map;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        context = viewGroup.getContext();
        funcSelectListener = (FuncSelectListener) context;
        View view = LayoutInflater.from(context).inflate(R.layout.my_holder, viewGroup, false);
        width = viewGroup.getWidth();
        return new FuncSelectAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        RequestOptions requestOptions = new RequestOptions().centerInside();
        Glide.with(context)
                .load(functionMap.get(position))
                .apply(requestOptions)
                .into(viewHolder.imageView);

        viewHolder.imageView.setOnClickListener(v -> funcSelectListener.OnSelected(position));
    }

    @Override
    public int getItemCount() {
        return functionMap.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.holder_imageView);
        }
    }
}
