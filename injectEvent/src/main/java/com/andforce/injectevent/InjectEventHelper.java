package com.andforce.injectevent;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.app.Instrumentation;
import android.graphics.Path;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayList;

import kotlin.Pair;

// https://www.pocketmagic.net/injecting-events-programatically-on-android
public class InjectEventHelper {

    private InjectEventHelper() {
        super();
    }

    private static InjectEventHelper mHelper;

    public static InjectEventHelper getInstance() {
        if (mHelper == null) {
            synchronized (InjectEventHelper.class) {
                if (mHelper == null) {
                    mHelper = new InjectEventHelper();
                }
            }
        }
        return mHelper;
    }
    private final Instrumentation mInstrumentation = new Instrumentation();

    private Path mPath = new Path();
    private ArrayList<Pair<Float, Float>> mPoints = new ArrayList<>();
    private long mLastTimeStamp = 0L;

    private void injectPointerEvent(int action, float pozx, float pozy) {
        try {
            MotionEvent motionEvent = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), action, pozx, pozy, 0);
            mInstrumentation.sendPointerSync(motionEvent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void injectTouchDown(AccessibilityService service, int screenW, int screenH, float fromRealX, float fromRealY) {
        mLastTimeStamp = System.currentTimeMillis();
        mPath = new Path();
        mPoints.clear();
        mPath.moveTo(fromRealX, fromRealY);
        mPoints.add(new Pair<>(fromRealX, fromRealY));
        Log.d("AutoTouchService", "DOWN points: $points, path: $path");

    }

    public void injectTouchDownSystem(float fromRealX, float fromRealY) {
        injectPointerEvent(MotionEvent.ACTION_DOWN, fromRealX, fromRealY);
    }

    public void injectTouchMove(AccessibilityService service, int screenW, int screenH, float fromRealX, float fromRealY) {
        mPath.lineTo(fromRealX, fromRealY);
        mPoints.add(new Pair<>(fromRealX, fromRealY));
        Log.d("AutoTouchService", "MOVE points: $points, path: $path");
    }

    public void injectTouchMoveSystem(float fromRealX, float fromRealY) {
        injectPointerEvent(MotionEvent.ACTION_MOVE, fromRealX, fromRealY);
    }

    public void injectTouchUp(AccessibilityService service, int screenW, int screenH, float fromRealX, float fromRealY) {
        if (mPath == null || mPath.isEmpty()) {
            return;
        }
        mPath.lineTo(fromRealX, fromRealY);
        mPoints.add(new Pair<>(fromRealX, fromRealY));

        long currentTime = System.currentTimeMillis();
        long duration = currentTime - mLastTimeStamp;
        if (duration < 100) {
            duration = 100;
        } else if (duration > 300) {
            duration = 300;
        }

        dispatchMouseGesture(service, mPath, 0, duration);
        mLastTimeStamp = 0;
        Log.d("AutoTouchService", "UP points: $points ,duration: $duration, path: $path");
    }

    public void injectTouchUpSystem(float fromRealX, float fromRealY) {
        injectPointerEvent(MotionEvent.ACTION_UP, fromRealX, fromRealY);
    }


    private void dispatchMouseGesture(AccessibilityService service, Path path, long startTime, long duration) {
        GestureDescription gestureDescription = new GestureDescription.Builder()
                .addStroke(new GestureDescription.StrokeDescription(path, startTime, duration))
                .build();
        service.dispatchGesture(gestureDescription, null, null);
    }
}
