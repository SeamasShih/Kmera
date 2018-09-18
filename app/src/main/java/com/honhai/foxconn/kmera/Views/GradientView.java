package com.honhai.foxconn.kmera.Views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.honhai.foxconn.kmera.Tools.DirectionVerifier;

public class GradientView extends View {

    private final String TAG = "GradientView";

    private float[] values = new float[3];
    private float currentAngle = 0;
    private int rotation;
    private int lineLength;
    private int r;
    private int cx;
    private int cy;
    private ValueAnimator angleAnimator;
    private Paint white;
    private Paint green;
    private Paint yellow;

    public GradientView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        white = new Paint();
        white.setStyle(Paint.Style.STROKE);
        white.setStrokeWidth(2);
        white.setAntiAlias(true);
        white.setColor(Color.LTGRAY);

        green = new Paint();
        green.setStyle(Paint.Style.STROKE);
        green.setStrokeWidth(3);
        green.setAntiAlias(true);
        green.setColor(Color.rgb(30, 200, 30));

        yellow = new Paint();
        yellow.setStyle(Paint.Style.STROKE);
        yellow.setStrokeWidth(3);
        yellow.setAntiAlias(true);
        yellow.setColor(Color.argb(90, 200, 200, 30));

        long animDuration = 240L;
        angleAnimator = new ValueAnimator();
        angleAnimator.setDuration(animDuration);
        angleAnimator.addUpdateListener(animation -> {
            currentAngle = (float) animation.getAnimatedValue();
            postInvalidate();
        });
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        cx = getWidth() / 2;
        cy = getHeight() / 2;
        r = getWidth() / 12;
        lineLength = Math.round(r * 1.5f);
    }

    public void setGradient(float a, float p, float r) {
        this.values[0] = a;
        this.values[1] = p;
        this.values[2] = r;

        if (angleAnimator.isRunning()) {
            angleAnimator.cancel();
        }
        angleAnimator.setFloatValues(currentAngle, values[1] + 90 + (rotation - 90) % 180);
        angleAnimator.start();
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.translate(cx, cy);
        canvas.drawCircle(0, 0, r, white);

        canvas.drawLine(-lineLength, 0, lineLength, 0, yellow);

        if (rotation == 90 &&
                (90 - Math.abs(values[1]) < 3 || Math.abs(values[2]) < 6 || 180 - Math.abs(values[2]) < 6)) {
            drawCurrentHorizonLine(canvas, green);
        } else if (rotation == 90 && values[2] > 0) {
            canvas.rotate(-currentAngle);
            drawCurrentHorizonLine(canvas, white);
            canvas.rotate(currentAngle);
        } else if (rotation == 90 && values[2] < 0) {
            canvas.rotate(currentAngle);
            drawCurrentHorizonLine(canvas, white);
            canvas.rotate(-currentAngle);
        } else if (rotation == 180 && values[1] > 0) {
            canvas.rotate(-currentAngle);
            if (values[1] < 3f)
                drawCurrentHorizonLine(canvas, green);
            else
                drawCurrentHorizonLine(canvas, white);
            canvas.rotate(currentAngle);
        } else if (rotation == 180 && values[1] < 0) {
            canvas.rotate(-currentAngle);
            if (values[1] > -3f)
                drawCurrentHorizonLine(canvas, green);
            else
                drawCurrentHorizonLine(canvas, white);
            canvas.rotate(currentAngle);
        } else if (rotation == 270 &&
                (90 - Math.abs(values[1]) < 3 || Math.abs(values[2]) < 6 || 180 - Math.abs(values[2]) < 6)) {
            drawCurrentHorizonLine(canvas, green);
        } else if (rotation == 270 && values[2] > 0) {
            canvas.rotate(-currentAngle);
            drawCurrentHorizonLine(canvas, white);
            canvas.rotate(currentAngle);
        } else if (rotation == 270 && values[2] < 0) {
            canvas.rotate(currentAngle);
            drawCurrentHorizonLine(canvas, white);
            canvas.rotate(-currentAngle);
        } else if (rotation == 360 && values[1] > 0) {
            canvas.rotate(currentAngle);
            if (values[1] < 3f)
                drawCurrentHorizonLine(canvas, green);
            else
                drawCurrentHorizonLine(canvas, white);
            canvas.rotate(-currentAngle);
        } else if (rotation == 360 && values[1] < 0) {
            canvas.rotate(currentAngle);
            if (values[1] > -3f)
                drawCurrentHorizonLine(canvas, green);
            else
                drawCurrentHorizonLine(canvas, white);
            canvas.rotate(-currentAngle);
        }

        if (rotation == 90 || rotation == 270) {
            canvas.drawLine(0, 0, 0, r * values[1] / 90, yellow);
        } else if (rotation == 180 || rotation == 360) {
            canvas.drawLine(0, 0, 0, r * values[2] / 90, yellow);
        }
    }

    private void drawCurrentHorizonLine(Canvas canvas, Paint paint) {
        canvas.drawLine(-lineLength, 0, -r, 0, paint);
        canvas.drawLine(r, 0, lineLength, 0, paint);
    }
}
