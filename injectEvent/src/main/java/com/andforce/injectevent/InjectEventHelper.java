package com.andforce.injectevent;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.graphics.Path;
import android.os.IBinder;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.IWindowManager;
import android.view.MotionEvent;
import android.app.Instrumentation;

import java.util.ArrayList;

import kotlin.Pair;

// https://www.pocketmagic.net/injecting-events-programatically-on-android
public class InjectEventHelper {
    IBinder mWindowBinder = ServiceManager.getService(Context.WINDOW_SERVICE);
    IWindowManager mWndManager = IWindowManager.Stub.asInterface(mWindowBinder);
    Instrumentation m_Instrumentation = new Instrumentation();

    private Path path = new Path();
    private ArrayList<Pair<Float, Float>> points = new ArrayList<>();
    private long lastTimeStamp = 0L;

    private static final boolean USE_SYSTEM = true;

    private void injectPointerEvent(int action, float pozx, float pozy) {
        try {
            MotionEvent motionEvent = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), action, pozx, pozy, 0);
            m_Instrumentation.sendPointerSync(motionEvent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void injectTouchDown(AccessibilityService service, int screenW, int screenH, float fromRealX, float fromRealY) {
        if (USE_SYSTEM) {
            injectPointerEvent(MotionEvent.ACTION_DOWN, fromRealX, fromRealY);
            return;
        }
        lastTimeStamp = System.currentTimeMillis();
        path = new Path();
        points.clear();
        path.moveTo(fromRealX, fromRealY);
        points.add(new Pair<>(fromRealX, fromRealY));
        Log.d("AutoTouchService", "DOWN points: $points, path: $path");

    }

    public void injectTouchMove(AccessibilityService service, int screenW, int screenH, float fromRealX, float fromRealY) {
        if (USE_SYSTEM) {
            injectPointerEvent(MotionEvent.ACTION_MOVE, fromRealX, fromRealY);
            return;
        }
        path.lineTo(fromRealX, fromRealY);
        points.add(new Pair<>(fromRealX, fromRealY));
        Log.d("AutoTouchService", "MOVE points: $points, path: $path");
    }

    public void injectTouchUp(AccessibilityService service, int screenW, int screenH, float fromRealX, float fromRealY) {
        if (USE_SYSTEM) {
            injectPointerEvent(MotionEvent.ACTION_UP, fromRealX, fromRealY);
            return;
        }
        if (path == null || path.isEmpty()) {
            return;
        }
        path.lineTo(fromRealX, fromRealY);
        points.add(new Pair<>(fromRealX, fromRealY));

        long currentTime = System.currentTimeMillis();
        long duration = currentTime - lastTimeStamp;
        if (duration < 100) {
            duration = 100;
        } else if (duration > 300) {
            duration = 300;
        }

        dispatchMouseGesture(service, path, 0, duration);
        lastTimeStamp = 0;
        Log.d("AutoTouchService", "UP points: $points ,duration: $duration, path: $path");
    }


    private void dispatchMouseGesture(AccessibilityService service, Path path, long startTime, long duration) {
        GestureDescription gestureDescription = new GestureDescription.Builder()
                .addStroke(new GestureDescription.StrokeDescription(path, startTime, duration))
                .build();
        service.dispatchGesture(gestureDescription, null, null);
    }
}
