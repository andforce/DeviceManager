package com.andforce.device.applock;

import android.app.ActivityManagerNative;
import android.app.IActivityController;
import android.content.Intent;
import android.os.RemoteException;

public class AppLockActivityController {

    public void forceStopPackage(String packageName) {
        try {
            ActivityManagerNative.getDefault().forceStopPackage(packageName, -1);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    public void setActivityController(AppLauncherManager.Action action) {
//        try {
//            //ActivityManagerNative.getDefault().setActivityController(new ActivityController(action), true);
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
    }

    static class ActivityController extends IActivityController.Stub {

        private AppLauncherManager.Action action;
        public ActivityController(AppLauncherManager.Action action) {
            super();
            this.action = action;
        }

        @Override
        public boolean activityStarting(Intent intent, String pkg) throws RemoteException {
            if (action != null) {
                return action.allowLaunch(intent, pkg);
            }
            return false;
        }

        @Override
        public boolean activityResuming(String pkg) throws RemoteException {
            return true;
        }

        @Override
        public boolean appCrashed(String processName, int pid, String shortMsg, String longMsg, long timeMillis, String stackTrace) throws RemoteException {
            return true;
        }

        @Override
        public int appEarlyNotResponding(String processName, int pid, String annotation) throws RemoteException {
            return 0;
        }

        @Override
        public int appNotResponding(String processName, int pid, String processStats) throws RemoteException {
            return 0;
        }

        @Override
        public int systemNotResponding(String msg) throws RemoteException {
            return 0;
        }
    }
}
