package cn.bavelee.giaotone.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cn.bavelee.giaotone.service.SoundService;
import cn.bavelee.giaotone.util.Logcat;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Logcat.d("接收到开机广播");
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()))
            SoundService.start(context);
    }
}