package com.andforce.daemon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent startIntent = new Intent(context, DaemonService.class);
            context.startService(startIntent);
        }
    }
}
