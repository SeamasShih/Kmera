package com.honhai.foxconn.kmera.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class Gradient extends View {
    public Gradient(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        white = new Paint();
        white.setStyle(Paint.Style.STROKE);
        white.setStrokeWidth(2);
        white.setAntiAlias(true);
        white.setColor(Color.LTGRAY);

        green= new Paint();
        green.setStyle(Paint.Style.STROKE);
        green.setStrokeWidth(3);
        green.setAntiAlias(true);
        green.setColor(Color.rgb(30,200,30));

        yellow= new Paint();
        yellow.setStyle(Paint.Style.STROKE);
        yellow.setStrokeWidth(3);
        yellow.setAntiAlias(true);
        yellow.setColor(Color.rgb(200,200,30));
    }

    Paint white;
    Paint green;
    Paint yellow;
    int r = 200;

    public void setGradient(float[] values){

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int cx = canvas.getWidth()/2;
        int cy = canvas.getHeight()/2;
        canvas.drawCircle(cx,cy,r,white);
        canvas.drawLine(cx-400,cy,cx-r,cy,white);
        canvas.drawLine(cx+r,cy,cx+400,cy,white);
        canvas.drawLine(cx,cy-400,cx,cy-r,white);
        canvas.drawLine(cx,cy+r,cx,cy+400,white);

        canvas.drawLine(cx-400,cy,cx+400,cy,yellow);
    }
}
