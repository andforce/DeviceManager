package com.andforce.device.packagemanager;

import android.content.Context;
import android.content.IIntentReceiver;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

public class PackageManagerHelper {
    private static final String TAG = "PackageManagerHelper";
    private final Context mContext;
    private final PackageManager mPackageManager;
    private final PackageInstaller mPackageInstaller;

    public static final int ACTION_TYPE_UNKNOWN = -1;
    public static final int ACTION_TYPE_INSTALL = 1;
    public static final int ACTION_TYPE_UNINSTALL = 2;

    private int mActionType = ACTION_TYPE_UNKNOWN;

    private Listener mActionListener;
    private PackageInstaller.Session mSession = null;

    public interface Listener {
        void onActionFinished(int actionType, boolean success);
    }

    public PackageManagerHelper(Context context) {
        mContext = context.getApplicationContext();
        mPackageManager = context.getPackageManager();
        mPackageInstaller = mPackageManager.getPackageInstaller();
    }

    public void registerListener(Listener listener) {
        mPackageInstaller.registerSessionCallback(deleteCallback, mContext.getMainThreadHandler());
        mActionListener = listener;
    }

    public void unregisterListener() {
        mActionListener = null;
        mPackageInstaller.unregisterSessionCallback(deleteCallback);
    }

    private final PackageInstaller.SessionCallback deleteCallback = new PackageInstaller.SessionCallback() {
        @Override
        public void onCreated(int sessionId) {
            Log.d(TAG, "onCreated: " + sessionId);
        }

        @Override
        public void onBadgingChanged(int sessionId) {
            Log.d(TAG, "onBadgingChanged: " + sessionId);
        }

        @Override
        public void onActiveChanged(int sessionId, boolean active) {
            Log.d(TAG, "onActiveChanged: " + sessionId + ", active: " + active);
        }

        @Override
        public void onProgressChanged(int sessionId, float progress) {
            Log.d(TAG, "onProgressChanged: " + sessionId + ", progress: " + progress);
        }

        @Override
        public void onFinished(int sessionId, boolean success) {
            Log.d(TAG, "onFinished: " + sessionId + ", success: " + success);
            if (mActionListener != null) {
                mActionListener.onActionFinished(mActionType, success);
            }
            if (mSession != null) {
                mSession.close();
                mSession = null;
            }
        }
    };

    public void deletePackage(String pkgName) {
        mActionType = ACTION_TYPE_UNINSTALL;

        Thread t = new Thread(() -> {
            int unInstallFlags = PackageManager.DELETE_ALL_USERS;

            final LocalIntentReceiver localReceiver = new LocalIntentReceiver();
            mPackageInstaller.uninstall(pkgName, unInstallFlags | PackageManager.DELETE_ALL_USERS, localReceiver.getIntentSender());

            Intent result = localReceiver.getResult();
            int status = result.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE);
            if (mActionListener != null) {
                mActionListener.onActionFinished(ACTION_TYPE_UNINSTALL, status == PackageInstaller.STATUS_SUCCESS);
            }
        });
        t.start();
    }

    public void installFromInputStream(InputStream inputStream) {
        mActionType = ACTION_TYPE_INSTALL;


        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                PackageInstaller.SessionParams.MODE_FULL_INSTALL);

        // 创建一个Session
        try {
            int sessionId = mPackageInstaller.createSession(params);
            // 建立和PackageManager的socket通道，Android中的通信不仅仅有Binder还有很多其它的
            mSession = mPackageInstaller.openSession(sessionId);
            processInputStream(inputStream, inputStream.available(), mSession);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (mSession == null) {
            return;
        }
        final LocalIntentReceiver localReceiver = new LocalIntentReceiver();
        mSession.commit(localReceiver.getIntentSender());
    }

    private boolean processInputStream(InputStream inputStream, long length, PackageInstaller.Session session) {
        InputStream in = null;
        OutputStream out = null;
        boolean success = false;
        try {
            out = session.openWrite("base.apk", 0, length);
            in = inputStream;
            int total = 0;
            int c;
            byte[] buffer = new byte[1024 * 1024];
            while ((c = in.read(buffer)) != -1) {
                total += c;
                out.write(buffer, 0, c);
            }
            session.fsync(out);
            Log.d(TAG, "streamed " + total + " bytes");
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != session) {
                session.close();
            }
            try {
                if (null != out) {
                    out.close();
                }
                if (null != in) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return success;
    }

    public void installFromAssert(String assetName) {
        Log.w(TAG, "installPackage pkg: " + assetName);

        Thread t = new Thread(() -> {
            try (InputStream is = mContext.getAssets().open(assetName)) {
                installFromInputStream(is);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        t.start();
    }

    public void installPackage(File apkFilePath) {
        try (InputStream in = new FileInputStream(apkFilePath)) {
            installFromInputStream(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class LocalIntentReceiver {
        private final SynchronousQueue<Intent> mResult = new SynchronousQueue<>();

        private IIntentSender.Stub mLocalSender = new IIntentSender.Stub() {
            @Override
            public void send(int code, Intent intent, String resolvedType,
                             IBinder whitelistToken, IIntentReceiver finishedReceiver,
                             String requiredPermission, Bundle options) {
                try {
                    mResult.offer(intent, 5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        public IntentSender getIntentSender() {
            return new IntentSender((IIntentSender) mLocalSender);
        }

        public Intent getResult() {
            try {
                return mResult.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
