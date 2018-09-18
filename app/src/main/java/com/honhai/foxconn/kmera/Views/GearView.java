package com.honhai.foxconn.kmera.Views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Region;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.honhai.foxconn.kmera.Tools.Mathematics;

public class GearView extends View {
    public GearView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setClickable(true);

        white = new Paint();
        white.setStyle(Paint.Style.FILL);
        white.setAntiAlias(true);
        white.setColor(Color.argb(120,200,200,200));

        black = new Paint();
        black.setStyle(Paint.Style.STROKE);
        black.setStrokeWidth(3);
        black.setAntiAlias(true);
        black.setColor(Color.BLACK);

        c = new Point();
    }

    OnSpinListener mSpinListener;
    Paint white , black;
    Point c;
    Point touch;
    Point spin;
    int r;
    boolean isBig = true , isShift;
    float value , maxValue = 100 , minValue = 0;
    int tTheta = 0 , sTheta = 0 ,oTheta, theta = 0;
    Region circleB , circleS;
    Path pathB , pathS;
    int rate = 5;

    public void setPrecisionRate(int rate){
        this.rate = rate;
    }

    public void setMaxValue(float value){
        maxValue = value;
    }

    public void setMinValue(float value){
        minValue = value;
    }

    public float getValue(){
        return value;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        c.set(80,getHeight()-80);
        r = getWidth()/6;
        setRegion();
    }

    private void setRegion() {
        pathB = new Path();
        pathB.addCircle(c.x,c.y,r, Path.Direction.CCW);
        pathS = new Path();
        pathS.addCircle(c.x,c.y,r/2, Path.Direction.CCW);

        circleB = new Region();
        circleB.setPath(pathB,new Region(0,0,getWidth(),getHeight()));
        circleS = new Region();
        circleS.setPath(pathS,new Region(0,0,getWidth(),getHeight()));
    }

    public void setGear(boolean mode){
        isBig = mode;
        invalidate();
    }

    public boolean isBigGear(){
        return isBig;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isBig) {
            canvas.drawCircle(c.x, c.y, r, white);
            canvas.save();
            canvas.rotate(theta+90,c.x,c.y);
            canvas.drawLine(c.x-r/4, c.y, c.x-r, c.y , black);
            canvas.restore();

            //todo draw spin UI
        }
        else {
            canvas.drawCircle(c.x, c.y, r / 2, white);
            canvas.save();
            canvas.rotate(theta+90,c.x,c.y);
            canvas.drawLine(c.x-r/6, c.y, c.x-r/2, c.y , black);
            canvas.restore();

            //todo draw spin UI
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                isShift = false;
                if ( (isBig && circleB.contains((int)event.getX(),(int)event.getY())) ||
                        (!isBig && circleS.contains((int)event.getX(),(int)event.getY())) ){
                    isShift = true;
                    return super.onTouchEvent(event);
                }
                tTheta = (int) Mathematics.toTheta(event.getX(),event.getY(),c.x,c.y)+90;
                tTheta = tTheta > 120  ? 0 : tTheta;
                tTheta = tTheta > 90 ? 90 : tTheta;
                break;
            case MotionEvent.ACTION_MOVE:
                if (isShift)
                    return super.onTouchEvent(event);
                sTheta = (int) Mathematics.toTheta(event.getX(),event.getY(),c.x,c.y)+90;
                sTheta = sTheta > 120  ? 0 : sTheta;
                sTheta = sTheta > 90 ? 90 : sTheta;
//                Log.d("Seamas","Delta = " + (sTheta -tTheta));
                theta = (isBig ? (sTheta -tTheta) : (sTheta -tTheta)/rate ) + oTheta;
                theta = theta < 0  ? 0 : theta;
                theta = theta > 90 ? 90 : theta;
//                Log.d("Seamas","Theta = " + theta);
                value = theta* maxValue /90 + minValue;
                if (mSpinListener != null){
                    mSpinListener.onSpin(this);
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                oTheta = theta;
                if (isShift) {
                    setGear(!isBig);
                    Log.d("Seamas","QQ");
                    return super.onTouchEvent(event);
                }
                break;
        }
        return true;
    }

    public void setOnSpinListener(OnSpinListener l){
        if (l != null){
            mSpinListener = l;
        }
    }

    public interface OnSpinListener{
        void onSpin(GearView v);
    }
}