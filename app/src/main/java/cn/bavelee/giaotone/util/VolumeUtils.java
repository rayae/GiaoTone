package cn.bavelee.giaotone.util;

import android.content.Context;
import android.media.AudioManager;

public class VolumeUtils {

    //保存所有音量
    public static int[] saveAllStreamVolume(Context context) {
        try {
            int[] volumes = new int[4];
            volumes[0] = getStreamVolume(context, AudioManager.STREAM_NOTIFICATION);
            volumes[1] = getStreamVolume(context, AudioManager.STREAM_RING);
            volumes[2] = getStreamVolume(context, AudioManager.STREAM_MUSIC);
            volumes[3] = getStreamVolume(context, AudioManager.STREAM_ALARM);
            return volumes;
        } catch (Exception ignore) {

        }
        return new int[]{0, 0, 0, 0};
    }

    public static void setMuteChargeSoundVolume(Context context, int volume) {
        setStreamVolume(context, AudioManager.STREAM_NOTIFICATION, volume);
        setStreamVolume(context, AudioManager.STREAM_RING, volume);
    }

    public static void restoreAllStreamVolume(Context context, int[] volumes) {
        try {
            setStreamVolume(context, AudioManager.STREAM_NOTIFICATION, volumes[0]);
            setStreamVolume(context, AudioManager.STREAM_RING, volumes[1]);
            setStreamVolume(context, AudioManager.STREAM_MUSIC, volumes[2]);
            setStreamVolume(context, AudioManager.STREAM_ALARM, volumes[3]);
        } catch (Exception ignore) {

        }
    }

    public static int getStreamVolume(Context context, int channel) {
        try {
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            return am.getStreamVolume(channel);
        } catch (Exception ignore) {

        }
        return 0;
    }

    public static int getMaxVolume(Context context, int channel) {
        try {
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            return am.getStreamMaxVolume(channel);
        } catch (Exception ignore) {

        }
        return 0;
    }

    public static void setStreamVolume(Context context, int channel, int volume) {
        try {
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            am.setStreamVolume(channel, volume, 0);
        } catch (Exception ignore) {

        }
    }
}
