package com.andforce.daemon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompleteReceiver extends BroadcastReceiver {
    private static final String TAG = "BootCompleteReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent startIntent = new Intent(context, DaemonService.class);
            context.startService(startIntent);

            Log.i(TAG, "start SocketEventService");
            Intent socketServiceIntent = new Intent();
            socketServiceIntent.setPackage(context.getPackageName());
            socketServiceIntent.setAction("com.andforce.socket.SocketEventService");
            context.startService(socketServiceIntent);
        }
    }
}
