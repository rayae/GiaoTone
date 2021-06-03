package cn.bavelee.giaotone.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import cn.bavelee.giaotone.Consts;
import cn.bavelee.giaotone.R;

import static android.content.Context.POWER_SERVICE;

public class IntentUtils {


    public static boolean isServiceRunning(Context context, String className) {
        boolean isRunning = false;
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningServiceInfo> runningServices = am.getRunningServices(Integer.MAX_VALUE);
            for (ActivityManager.RunningServiceInfo runningServiceInfo : runningServices) {
                String name = runningServiceInfo.service.getClassName();
                if (name.equals(className)) {
                    isRunning = true;
                }
            }
        } catch (Exception ignore) {

        }
        return isRunning;
    }

    public static void launchBrowser(Context context, String url) {
        if (TextUtils.isEmpty(url) || url.length() < 5) {
            return;
        }
        try {
            Intent in = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.parse(url);
            in.setData(uri);
            context.startActivity(in);
        } catch (Exception ignore) {
            Toast.makeText(context, R.string.no_browser, Toast.LENGTH_SHORT).show();
        }
    }

    public static void gotoBatteryOptimization(Context context) {
        if (IntentUtils.isBatteryOptimizationIgnored(context)) {
            Toast.makeText(context, R.string.has_ignored_battery_optimization, Toast.LENGTH_SHORT).show();
        } else {
            try {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                context.startActivity(intent);
            } catch (Exception ignore) {
                Toast.makeText(context, R.string.has_ignored_battery_optimization, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static boolean isBatteryOptimizationIgnored(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            return powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
        }
        return true;
    }

    public static void LaunchSettingCanDrawOverlays(Fragment context) {
        int sdkInt = Build.VERSION.SDK_INT;
        if (sdkInt >= Build.VERSION_CODES.O) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            context.startActivityForResult(intent, Consts.REQUEST_CODE_FLOATING_SERVICE_PERMISSION);
        } else if (sdkInt >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + context.getActivity().getPackageName()));
            context.startActivityForResult(intent, Consts.REQUEST_CODE_FLOATING_SERVICE_PERMISSION);
        }
    }

    // 请求忽略电池优化
    public static void requestIgnoreBatteryOptimization(final Context context) {
        if (!isBatteryOptimizationIgnored(context))
            try {
                if (context instanceof Activity && !((Activity) context).isFinishing())
                    new AlertDialog.Builder(context)
                            .setMessage(R.string.request_ignore_battery_optimization)
                            .setPositiveButton(R.string.okay, (dialog, which) -> {
                                dialog.cancel();
                                gotoBatteryOptimization(context);
                            })
                            .setCancelable(false)
                            .show();
            } catch (Exception ignore) {

            }

    }

    // 跳转微信扫一扫
    public static void launchWeChatScanner(Context context) {
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.tencent.mm");
            if (intent == null) return;
            intent.putExtra("LauncherUI.From.Scaner.Shortcut", true);
            intent.setAction("android.intent.action.VIEW");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

        } catch (Exception ignored) {
        }
    }

    // 跳转支付宝扫一扫
    public static void LaunchAlipayScanner(Context context) {
        try {
            Uri uri = Uri.parse("alipayqr://platformapi/startapp?saId=10000007");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
        } catch (Exception ignored) {
        }
    }

    // 跳转自启动界面
    public static void launchSystemAutoStartManager(Context context) {
        Intent intent = new Intent();
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName componentName = null;
            switch (Build.MANUFACTURER) {
                case "Xiaomi":
                    componentName = new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity");
                    break;
                case "Letv":
                    intent.setAction("com.letv.android.permissionautoboot");
                    break;
                case "samsung":
                    componentName = new ComponentName("com.samsung.android.sm_cn", "com.samsung.android.sm.ui.ram.AutoRunActivity");
                    break;
                case "HUAWEI":
                    componentName = ComponentName.unflattenFromString("com.huawei.systemmanager/.startupmgr.ui.StartupNormalAppListActivity");//跳自启动管理
                    break;
                case "vivo":
                    componentName = ComponentName.unflattenFromString("com.iqoo.secure/.safeguard.PurviewTabActivity");
                    break;
                case "Meizu":
                    componentName = ComponentName.unflattenFromString("com.meizu.safe/.permission.PermissionMainActivity");
                    break;
                case "OPPO":
                    componentName = ComponentName.unflattenFromString("com.oppo.safe/.permission.startup.StartupAppListActivity");
                    break;
                case "ulong":
                    componentName = new ComponentName("com.yulong.android.coolsafe", ".ui.activity.autorun.AutoRunListActivity");
                    break;
                default:
                    intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                    intent.setData(Uri.fromParts("package", context.getPackageName(), null));
                    break;
            }
            intent.setComponent(componentName);
            context.startActivity(intent);
        } catch (Exception e) {
            intent = new Intent(Settings.ACTION_SETTINGS);
            context.startActivity(intent);
        }
    }
}