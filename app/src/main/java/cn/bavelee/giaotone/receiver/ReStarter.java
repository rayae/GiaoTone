package cn.bavelee.giaotone.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cn.bavelee.giaotone.service.SoundService;
import cn.bavelee.giaotone.util.Logcat;

public class ReStarter extends BroadcastReceiver {
    public static final String ACTION_RESTART = "cn.bavelee.giaotone.restart_service";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_RESTART.equals(intent.getAction())) {
            Logcat.d("【ReStarter】重启服务");
            SoundService.start(context, true);
        }
    }

    public static void sendReStartSignal(Context context) {
        try {
            Intent intent = new Intent(ACTION_RESTART);
            intent.setPackage(context.getPackageName());
            context.sendBroadcast(intent);
        } catch (Exception e) {
            Logcat.d("【ReStarter】发送重启信号失败...");
        }
    }
}
