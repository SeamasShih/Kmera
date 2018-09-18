package com.honhai.foxconn.kmera.Tools;

import android.support.annotation.Nullable;

public class DirectionVerifier {

    public static final int MASK_ORIENTATION = 0b11000000;
    public static final int MASK_DIRECTION = 0b111000;
    public static final int MASK_ROTATION = 0b111;
    public static final int MASK_ALL = 0xFFFFFFFF;

    public static final int DIRECTION_UP = 0b001000;
    public static final int DIRECTION_DOWN = 0b010000;
    public static final int DIRECTION_LEFT = 0b011000;
    public static final int DIRECTION_RIGHT = 0b100000;
    public static final int DIRECTION_FRONT = 0b101000;
    public static final int DIRECTION_BACK = 0b110000;

    public static final int ROTATION_NORMAL = 0b001;
    public static final int ROTATION_CLOCKWISE = 0b010;
    public static final int ROTATION_REVERSE = 0b011;
    public static final int ROTATION_ANTI_CLOCKWISE = 0b100;

    public static final int ORIENTATION_PORTRAIT = 0b01000000;
    public static final int ORIENTATION_LANDSCAPE = 0b10000000;
    public static final int ORIENTATION_UNKNOWN = 0b11000000;


    public static int getDirection(float[] orientations) {
        int a = (int) Math.toDegrees(orientations[0]);
        int p = (int) Math.toDegrees(orientations[1]);
        int r = (int) Math.toDegrees(orientations[2]);
        if (isInner(p, 0) && isInner(r, 0))
            return DIRECTION_UP;
        else if (isInner(p, 0) && isInner(r, 180))
            return DIRECTION_DOWN;
        else if (isInner(p, 90) || isInner(p, -90))
            return DIRECTION_FRONT;
        return ORIENTATION_UNKNOWN;
    }

    public static int getOrientation(float[] orientations) {
        return getOrientation(orientations, MASK_ALL);
    }

    public static int getOrientation(float[] orientations, int mask) {
        int a = (int) Math.toDegrees(orientations[0]);
        int p = (int) Math.toDegrees(orientations[1]);
        int r = (int) Math.toDegrees(orientations[2]);
        if (isInner(p, 0) && isInner(r, 90))
            return (ORIENTATION_LANDSCAPE | ROTATION_CLOCKWISE) & mask;
        else if (isInner(p, 0) && isInner(r, -90))
            return (ORIENTATION_LANDSCAPE | ROTATION_ANTI_CLOCKWISE) & mask;
        else if (isInner(p, 90))
            return (ORIENTATION_PORTRAIT | ROTATION_REVERSE) & mask;
        else
            return (ORIENTATION_PORTRAIT | ROTATION_NORMAL) & mask;
    }

    public static int mask(int value, int mask) {
        return value & mask;
    }

    private static boolean isInner(int target, int centre) {
        return Math.abs((target - centre) % 360) < 45;
    }

    public static boolean isInner(int target, int centre , int tolerance) {
        return Math.abs((target - centre) % 360) < tolerance;
    }
}
