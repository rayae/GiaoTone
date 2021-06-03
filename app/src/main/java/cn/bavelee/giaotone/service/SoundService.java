package cn.bavelee.giaotone.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;

import java.util.Timer;
import java.util.TimerTask;

import cn.bavelee.giaotone.App;
import cn.bavelee.giaotone.Consts;
import cn.bavelee.giaotone.receiver.ReStarter;
import cn.bavelee.giaotone.receiver.ToneReceiver;
import cn.bavelee.giaotone.util.Logcat;
import cn.bavelee.giaotone.util.PrefsUtils;

public class SoundService extends BaseService {

    public static final String SELF_RESTART = "cn.bavelee.giaotone.self_restart";
    private ToneReceiver mToneReceiver;
    private PowerManager mPowerManager;
    private boolean mListenedScreenOff;
    private Timer mTimer;

    public static void stop(Context context) {
        App.post(() -> {
            try {
                Logcat.d("停止【音频服务】");
                Intent service = new Intent(context, SoundService.class);
                context.stopService(service);
            } catch (Exception ignore) {

            }
        });
    }

    public static void start(Context context) {
        start(context, false);
    }

    public static void start(Context context, boolean isSelfReStart) {
        App.post(() -> {
            Logcat.d("启动【音频服务】");
            Intent service = new Intent(context, SoundService.class);
            service.putExtra(SELF_RESTART, isSelfReStart);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(service);
            } else context.startService(service);
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logcat.d("【音频服务】onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        clearListener();
        showForegroundNotification();
        if (intent != null && intent.getBooleanExtra(SELF_RESTART, false)) {
            Logcat.d("【音频服务】已完成自动重启");
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_BATTERY_LOW);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(ToneReceiver.ACTION_CHECK_RECEIVER_ALIVE);
        filter.addAction(ToneReceiver.ACTION_SCREEN_OFF);
        mToneReceiver = new ToneReceiver();
        registerReceiver(mToneReceiver, filter);

        if (PrefsUtils.getBoolean(Consts.KEY_USE_FLOATING_SERVICE, false))
            showFloating();
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Logcat.d("【音频服务】 运行中");
            }
        }, 0, 1000 * 60 * 3);
        if (PrefsUtils.getBoolean(Consts.KEY_RAPID_CHECK_SCREEN_OFF, false)) {
            Logcat.d("【快速检测锁屏状态】 运行中");
            mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (mPowerManager != null && !mPowerManager.isInteractive()) {
                        if (!mListenedScreenOff) {
                            mListenedScreenOff = true;
                            sendBroadcast(new Intent(ToneReceiver.ACTION_SCREEN_OFF));
                        }
                    } else mListenedScreenOff = false;
                }
            }, 0, 50);
        }
        return Service.START_STICKY;
    }

    private void showFloating() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            return;
        }
        Logcat.d("【流氓后台】 显示 1x1 悬浮窗");
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        View view = new View(this);
        view.setBackgroundColor(Color.TRANSPARENT);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        layoutParams.format = PixelFormat.TRANSPARENT;
        layoutParams.width = 1;
        layoutParams.height = 1;
        layoutParams.x = 0;
        layoutParams.y = 0;
        try {
            windowManager.addView(view, layoutParams);
            Logcat.d("【流氓后台】 悬浮窗已显示");
        } catch (Exception e) {
            Logcat.d("【流氓后台】 悬浮窗创建失败" + e.getMessage());
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        restart();
        super.onTaskRemoved(rootIntent);
    }

    private void restart() {
        clearListener();
        Logcat.d("【音频服务】 onTaskRemoved 1秒后重启");
        App.postDelayed(() -> ReStarter.sendReStartSignal(SoundService.this), 1000);
    }

    private void clearListener() {
        try {
            if (mToneReceiver != null)
                unregisterReceiver(mToneReceiver);
            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
            }

        } catch (Exception ignore) {

        }
    }

    @Override
    public void onDestroy() {
        Logcat.d("【音频服务】onDestroy");
        clearListener();
        restart();
        super.onDestroy();
    }
}