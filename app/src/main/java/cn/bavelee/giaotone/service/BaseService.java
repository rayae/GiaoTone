package cn.bavelee.giaotone.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import cn.bavelee.giaotone.R;
import cn.bavelee.giaotone.util.Logcat;

public class BaseService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        showForegroundNotification();
    }

    protected void showForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Logcat.d("【应用常驻内存】，发送【正在运行】的通知");
            String CHANNEL_ID = "keep_in_background";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    getString(R.string.app_in_running),
                    NotificationManager.IMPORTANCE_MIN);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle(getString(R.string.app_in_running))
                    .setContentText(getString(R.string.app_in_running))
                    .setChannelId(CHANNEL_ID)
                    .build();

            startForeground(1, notification);
        }
    }
}
