package com.honhai.foxconn.kmera.Tools;

public class Mathematics {
    public static float toTheta(float px , float py , float qx , float qy){
        float dx = px-qx;
        float dy = py-qy;
        if (dx == 0){
            if (dy == 0)
                return 0;
            return dy > 0 ? 90 : -90;
        }
        return (float) Math.toDegrees(Math.atan(dy/dx));
    }

    public static boolean isInner(int target, int centre) {
        return isInner(target,centre,45);
    }

    public static boolean isInner(int target, int centre , int tolerance) {
        return Math.abs((target - centre) % 360) < tolerance;
    }
}
