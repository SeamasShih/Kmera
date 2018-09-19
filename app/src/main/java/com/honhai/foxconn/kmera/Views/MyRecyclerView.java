package com.honhai.foxconn.kmera.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

public class MyRecyclerView extends RecyclerView {

    private int width;
    private int height;

    public MyRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);

        width = MeasureSpec.getSize(widthSpec);
        height = MeasureSpec.getSize(heightSpec);

        setMeasuredDimension(width, width / 6);
    }
}
