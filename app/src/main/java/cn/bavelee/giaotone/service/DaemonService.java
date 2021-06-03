package cn.bavelee.giaotone.service;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.Nullable;
import cn.bavelee.giaotone.App;
import cn.bavelee.giaotone.receiver.ReStarter;
import cn.bavelee.giaotone.util.IntentUtils;
import cn.bavelee.giaotone.util.Logcat;

public class DaemonService extends BaseService {

    private static final long INTERVAL = 5000;
    private Timer timer;


    public static void stop(Context context) {
        App.post(() -> {
            try {
                Logcat.d("停止【守护服务】");
                Intent service = new Intent(context, DaemonService.class);
                context.stopService(service);
            } catch (Exception ignore) {

            }
        });
    }

    public static void start(Context context) {
        App.post(() -> {
            Logcat.d("启动【守护服务】");
            Intent service = new Intent(context, DaemonService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(service);
            } else context.startService(service);
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showForegroundNotification();
        App.keepWakeLock();
        Logcat.d("【守护进程】启动");
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!IntentUtils.isServiceRunning(DaemonService.this, SoundService.class.getCanonicalName())) {
                    Logcat.d("【守护进程】重启音频服务");
                    ReStarter.sendReStartSignal(DaemonService.this);
                }
            }
        }, 0, INTERVAL);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        App.keepWakeLock();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        App.releaseWakeLock();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        Logcat.d("【守护进程】退出");
        super.onDestroy();
    }
}
