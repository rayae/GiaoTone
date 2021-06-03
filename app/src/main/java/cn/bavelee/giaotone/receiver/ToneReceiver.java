package cn.bavelee.giaotone.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.BatteryManager;
import android.os.CountDownTimer;
import android.util.SparseBooleanArray;

import org.litepal.LitePal;

import java.util.List;

import cn.bavelee.giaotone.App;
import cn.bavelee.giaotone.Consts;
import cn.bavelee.giaotone.adapter.binder.ToneControlViewBinder;
import cn.bavelee.giaotone.adapter.entity.ToneControlEntity;
import cn.bavelee.giaotone.util.Logcat;
import cn.bavelee.giaotone.util.PlayUtils;
import cn.bavelee.giaotone.util.PrefsUtils;
import cn.bavelee.giaotone.util.TimeUtils;
import cn.bavelee.giaotone.util.VolumeUtils;

public class ToneReceiver extends BroadcastReceiver {

    public static final String ACTION_CHECK_RECEIVER_ALIVE = "cn.bavelee.giaotone.ACTION_CHECK_RECEIVER_ALIVE";
    public static final String ACTION_SCREEN_OFF = "cn.bavelee.giaotone.ACTION_SCREEN_OFF";
    public static final String ACTION_CHECK_RECEIVER_ALIVE_RESPONSE = "cn.bavelee.giaotone.ACTION_CHECK_RECEIVER_ALIVE_RESPONSE";

    private MediaPlayer mPlayer;
    private long mAvoidMistakeTouchLastPlugTime = 0;
    private boolean mConnectorRePlugIn = false;
    private boolean mAvoidAccidentalPlugOutActive = false;
    private SparseBooleanArray batteryLevelNotified = new SparseBooleanArray();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null) return;
        if (ACTION_CHECK_RECEIVER_ALIVE.equals(intent.getAction())) {
            Logcat.d("【电量监测】接收到 存活检测广播");
            context.sendBroadcast(new Intent(ACTION_CHECK_RECEIVER_ALIVE_RESPONSE));
            return;
        }
        List<ToneControlEntity> enabledControls = LitePal.where("isEnabled = ?", "1").find(ToneControlEntity.class);
        //初始化电量级别监控状态
        if (enabledControls.size() != 0 && batteryLevelNotified.size() == 0) {
            for (ToneControlEntity control : enabledControls) {
                batteryLevelNotified.put(control.getId(), true);
            }
        }
        if (checkCurrentTimeDisabled()) {
            Logcat.d("【电量监测】当前时间被禁用(全局开关)");
            return;
        }
        try {
            List<ToneControlEntity> currentControls = LitePal.where("filterAction = ? and isEnabled = ?", intent.getAction(), "1").find(ToneControlEntity.class);
            for (ToneControlEntity control : currentControls) {
                if (control.isTimedEnabled() && !TimeUtils.isCurrentTimeAvailable(control.getStartTime(), control.getEndTime())) {
                    Logcat.d("【电量监测】当前时间被禁用(单个开关)");
                    continue;
                }
                if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                    performBatteryLevel(context, control, intent);
                } else {
                    if ((Intent.ACTION_POWER_CONNECTED.equals(intent.getAction()) || Intent.ACTION_POWER_DISCONNECTED.equals(intent.getAction()))) {
                        if (avoidMistakeTouch())
                            return;
                        if (avoidAccidentalPlugOut(context, control, intent.getAction()))
                            return;
                    }
                    checkAndPlay(context, control);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //电量检测
    private void performBatteryLevel(Context context, ToneControlEntity control, Intent intent) {
        //判断当前电量检测是否需要播报
        int key = control.getId();
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        if (level == control.getBatteryLevel()) {
            if (!batteryLevelNotified.get(key)) {
                Logcat.d("【电量监测】" + control.getTitle() + " => " + level);
                checkAndPlay(context, control);
            }
            batteryLevelNotified.put(key, true);
        } else {
            batteryLevelNotified.put(key, false);
        }
    }


    //检测当前时间是否被禁用
    private boolean checkCurrentTimeDisabled() {
        if (PrefsUtils.getBoolean(Consts.KEY_CUSTOM_TIME_ENABLED, Consts.DEFAULT_CUSTOM_TIME_ENABLED)) {
            String start = PrefsUtils.getString(Consts.KEY_CUSTOM_TIME_START, Consts.DEFAULT_AVAILABLE_TIME);
            String end = PrefsUtils.getString(Consts.KEY_CUSTOM_TIME_END, Consts.DEFAULT_AVAILABLE_TIME);
            return !TimeUtils.isCurrentTimeAvailable(start, end);
        }
        return false;
    }

    //防断冲
    private boolean avoidAccidentalPlugOut(Context context, ToneControlEntity control, String action) {
        long mistakeTime = 0L;
        try {
            mistakeTime = Long.parseLong(PrefsUtils.getString(Consts.KEY_AVOID_ACCIDENTAL_PLUG_OUT, Consts.DEFAULT_AVOID_TIME_NONE));
            if (mistakeTime > 0L) {
                if (Intent.ACTION_POWER_DISCONNECTED.equals(action)) {
                    mConnectorRePlugIn = false;
                    mAvoidAccidentalPlugOutActive = true;
                    Logcat.d("【电量监测】防断冲模式触发");
                } else {
                    mConnectorRePlugIn = true;
                    Logcat.d("【电量监测】防断冲模式再次触发(禁止播放)");
                }
                if (!mAvoidAccidentalPlugOutActive) return false;
                long finalMistakeTime = mistakeTime;
                App.postDelayed(() -> {
                    if (!mConnectorRePlugIn) {
                        Logcat.d("%d 毫秒内没有重新插入充电器，播放提示音", finalMistakeTime);
                        checkAndPlay(context, control);
                    }
                    mAvoidAccidentalPlugOutActive = false;
                }, mistakeTime);
                return true;
            }
        } catch (Exception ignore) {

        }
        return false;
    }

    //防误触
    private boolean avoidMistakeTouch() {
        long mistakeTime = 0L;
        try {
            mistakeTime = Long.parseLong(PrefsUtils.getString(Consts.KEY_AVOID_MISTAKE_TOUCH, Consts.DEFAULT_AVOID_TIME_NONE));
            if (mistakeTime > 0L) {
                if (System.currentTimeMillis() - mAvoidMistakeTouchLastPlugTime < mistakeTime) {
                    //距离上次播放时间小于设置时间，则不播放
                    Logcat.d("【电量监测】禁止播放(防误触模式触发)");
                    return true;
                }
                mAvoidMistakeTouchLastPlugTime = System.currentTimeMillis();
            }
        } catch (Exception ignore) {
        }
        return false;
    }

    private void checkAndPlay(Context context, ToneControlEntity control) {
        playSound(context, ToneControlViewBinder.getSoundItemBySoundId(control.getSoundId()).getUrl());
    }


    private void playSound(final Context context, String uri) {
        int[] volumes = VolumeUtils.saveAllStreamVolume(context);
        boolean isEnabledMute = PrefsUtils.getBoolean(Consts.KEY_MUTE_WHILE_PLAYING, Consts.DEFAULT_MUTE_WHILE_PLAYING);
        try {
            stopPlay();
            //短暂关闭音量，实现关闭系统充电提示音效果
            if (isEnabledMute) {
                Logcat.d("隐藏系统充电音");
                VolumeUtils.setMuteChargeSoundVolume(context, 0);
                new CountDownTimer(300, 10) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                    }

                    @Override
                    public void onFinish() {
                        VolumeUtils.restoreAllStreamVolume(context, volumes);
                        mPlayer = PlayUtils.playSound(context, uri, null);
                    }
                }.start();
            } else {
                mPlayer = PlayUtils.playSound(context, uri, null);
            }
        } catch (Exception ignore) {
            if (isEnabledMute) {
                Logcat.d("隐藏系统充电音 => 还原音量");
                VolumeUtils.restoreAllStreamVolume(context, volumes);
            }
        }
    }


    private void stopPlay() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }
}