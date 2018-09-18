package com.honhai.foxconn.kmera.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.List;

public class FuncSelectView extends View {

    private final String TAG = "FuncSelectView";
    private final String COLOR_BLACK_900 = "#212121";

    private List<String> functionList;
    private int functionCount = 5;

    public FuncSelectView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        setMeasuredDimension(200, 50);
        Log.d(TAG, "onMeasure: setMeasuredDimension " + getWidth());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.BLUE);
        canvas.drawCircle(getWidth()/2,getHeight()/2,200,new Paint());
    }
}
