package cn.bavelee.giaotone;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;

import org.litepal.LitePal;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Stack;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import cn.bavelee.giaotone.receiver.BootReceiver;
import cn.bavelee.giaotone.receiver.ReStarter;
import cn.bavelee.giaotone.service.DaemonService;
import cn.bavelee.giaotone.service.SoundService;
import cn.bavelee.giaotone.ui.launcher.HiddenLauncher;
import cn.bavelee.giaotone.ui.launcher.NormalLauncher;
import cn.bavelee.giaotone.util.DBUtils;
import cn.bavelee.giaotone.util.IOUtils;
import cn.bavelee.giaotone.util.Logcat;
import cn.bavelee.giaotone.util.PrefsUtils;

public class App extends Application implements Application.ActivityLifecycleCallbacks {

    private static Stack<Activity> sActivities = new Stack<>();
    private static WeakReference<Context> sContext;
    private static Handler sHandler;
    private static PowerManager.WakeLock sWakeLock;

    public static Context getContext() {
        return sContext.get();
    }


    @Override
    public void onCreate() {
        super.onCreate();
        sContext = new WeakReference<>(this);
        sHandler = new Handler(Looper.getMainLooper());
        Logcat.init(this);

        LitePal.initialize(this);
        // prefs
        PrefsUtils.init(this);
        // 数据库
        DBUtils.init(this);
        // 信任所有 HTTPS 连接
        IOUtils.enableAllSSLHandshake();

        registerActivityLifecycleCallbacks(this);

        initService();

        initReceivers();

        initPrefs();
    }

    private void initReceivers() {
        registerReceiver(new BootReceiver(), new IntentFilter(Intent.ACTION_BOOT_COMPLETED));
        registerReceiver(new ReStarter(), new IntentFilter(ReStarter.ACTION_RESTART));
    }

    private void initPrefs() {
        boolean isGrated;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            isGrated = Settings.canDrawOverlays(this);
            Logcat.d("检查【悬浮窗】权限：" + isGrated);
            PrefsUtils.edit().putBoolean(Consts.KEY_USE_FLOATING_SERVICE, isGrated).apply();
        }
        //修改后台隐藏窗台
        isGrated = isHiddenLauncherEnabled(this);
        PrefsUtils.edit()
                .putBoolean(Consts.KEY_EXCLUDE_FROM_RECENTS, isGrated).apply();
        Logcat.d("检查【后台隐藏】权限：" + isGrated);
        Consts.DEFAULT_MAIN_SERVER = getString(R.string.default_server);
        updateServer(PrefsUtils.getString(Consts.KEY_MAIN_SERVER, Consts.DEFAULT_MAIN_SERVER));

        String channel = String.valueOf(PrefsUtils.getString(Consts.KEY_AUDIO_USAGE, Consts.DEFAULT_AUDIO_USAGE));
        String[] supportedChannels = getResources().getStringArray(R.array.audio_usages_values);
        boolean isValid = false;
        for (String s : supportedChannels)
            if (channel.equals(s)) {
                isValid = true;
                break;
            }
        if (!isValid) {
            Logcat.d("【声音通道】版本不匹配，重置声音通道策略");
            PrefsUtils.edit().putString(Consts.KEY_AUDIO_USAGE, Consts.DEFAULT_AUDIO_USAGE).apply();
        }

    }

    synchronized public static void keepWakeLock() {
        if (sWakeLock == null) {
            PowerManager mgr = (PowerManager) sContext.get().getSystemService(Context.POWER_SERVICE);
            sWakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, App.class.getCanonicalName());
            sWakeLock.setReferenceCounted(true);
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis((System.currentTimeMillis()));
            int hour = c.get(Calendar.HOUR_OF_DAY);
            if (hour >= 23 || hour <= 6) {
                sWakeLock.acquire(5000);
            } else {
                sWakeLock.acquire(300000);
            }
            Logcat.d("请求 Wake Lock");
        }
    }

    synchronized public static void releaseWakeLock() {
        if (sWakeLock != null) {
            if (sWakeLock.isHeld()) {
                sWakeLock.release();
                Logcat.d("释放 Wake Lock");
            }
            sWakeLock = null;
        }
    }

    public static void initService() {
        SoundService.stop(sContext.get());
        SoundService.start(sContext.get());
        DaemonService.stop(sContext.get());
        DaemonService.start(sContext.get());
    }

    public static void killAllActivity() {
        for (Activity activity : sActivities)
            activity.finishAndRemoveTask();
    }

    public static void restartApp() {
        try {
            Intent service = new Intent(sContext.get(), SoundService.class);
            sContext.get().stopService(service);
            PackageManager pm = sContext.get().getPackageManager();
            Intent intent = pm.getLaunchIntentForPackage(sContext.get().getPackageName());
            if (intent == null) return;
            App.killAllActivity();
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            sContext.get().startActivity(intent);
        } catch (Exception ignore) {

        }
    }
    public static void updateServer(String server) {
        Logcat.d("主服务器地址设置为：" + server);
        Consts.SERVER = server;
        Consts.URL_SOUND_LIBRARY = server + "/data.json";
    }

    public static void post(Runnable runnable) {
        if (sHandler != null && runnable != null)
            sHandler.post(runnable);
    }

    public static void postDelayed(Runnable runnable, long delayMillis) {
        if (sHandler != null && runnable != null)
            sHandler.postDelayed(runnable, delayMillis);
    }

    public static boolean isHiddenLauncherEnabled(Context context) {
        PackageManager pm = context.getPackageManager();
        return pm.getComponentEnabledSetting(new ComponentName(context, HiddenLauncher.class)) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
    }

    public static void enableHiddenLauncher(Activity context, boolean isHiddenEnabled) {
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(context, NormalLauncher.class),
                isHiddenEnabled ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED : PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(new ComponentName(context, HiddenLauncher.class),
                !isHiddenEnabled ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED : PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        restartApp();
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        sActivities.add(activity);
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        sActivities.remove(activity);
    }
}
