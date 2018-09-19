package com.honhai.foxconn.kmera.Views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

public class SnapView extends View {
    public SnapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        white = new Paint();
        white.setColor(Color.LTGRAY);
        white.setAntiAlias(true);
        white.setStyle(Paint.Style.FILL);

        black = new Paint();
        black.setColor(Color.DKGRAY);
        black.setAntiAlias(true);
        black.setStyle(Paint.Style.FILL);

        c = new Point();

        path = new Path();
    }

    private Resources resources = this.getResources();
    private DisplayMetrics dm = resources.getDisplayMetrics();
    private int screenWidth = dm.widthPixels;
    private int screenHeight = dm.heightPixels;
    Paint white;
    Paint black;
    Point c;
    Path path;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(screenWidth/5,screenWidth/5);
        c.set(getWidth()/2,getHeight()/2);
        setPath();
    }

    private void setPath() {
        int w = getWidth()/2;
        int h = getHeight()*7/19;
        int a = 10;
        path.reset();
        path.addRoundRect(-w/2,-h/2,w/2,h/2,a,a, Path.Direction.CCW);
        path.addCircle(0,0,w/4, Path.Direction.CW);
        path.addCircle(0,0,w/6, Path.Direction.CCW);
        path.moveTo(-h/3,-h/2);
        path.lineTo(h/3,-h/2);
        path.lineTo(h/3-a,-h/2-a*3/2);
        path.lineTo(-h/3+a,-h/2-a*3/2);
        path.close();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(c.x,c.y);
        canvas.drawCircle(0,0,c.x,white);
        canvas.drawPath(path,black);
    }
}
