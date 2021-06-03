package cn.bavelee.giaotone.util;

import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import cn.bavelee.giaotone.Consts;

public class PlayUtils {

    public static int convertUsageToAudioChannel(int stream) {
        switch (stream) {
            case AudioAttributes.USAGE_NOTIFICATION:
                return AudioManager.STREAM_NOTIFICATION;
            case AudioAttributes.USAGE_NOTIFICATION_RINGTONE:
                return AudioManager.STREAM_RING;
            case AudioAttributes.USAGE_ALARM:
                return AudioManager.STREAM_ALARM;
        }
        return AudioManager.STREAM_MUSIC;
    }

    public static int getUsageChannel(String usage) {
        switch (usage) {
            case "notification":
                return AudioAttributes.USAGE_NOTIFICATION;
            case "ringtone":
                return AudioAttributes.USAGE_NOTIFICATION_RINGTONE;
            case "alarm":
                return AudioAttributes.USAGE_ALARM;
        }
        return AudioAttributes.USAGE_MEDIA;
    }

    public static MediaPlayer playSound(Context context, String uriPath, MediaPlayer.OnCompletionListener listener) {
        String usage = PrefsUtils.getString(Consts.KEY_AUDIO_USAGE, Consts.DEFAULT_AUDIO_USAGE);
        int usageChannel = getUsageChannel(usage);
        int audioChannel = convertUsageToAudioChannel(usageChannel);
        final int originVolume = VolumeUtils.getStreamVolume(context, audioChannel);
        final int usedVolume = (int) (VolumeUtils.getMaxVolume(context, audioChannel) * PrefsUtils.getInt(Consts.KEY_SOUND_STANDALONE_VOLUME, 0) / 100f);
        Logcat.d("【播放音频】初始音量=" + originVolume + " 独立设置的音量=" + usedVolume + " audioChannel=" + audioChannel + " usageChannel=" + usageChannel + " savedUsage=" + usage);
        if (usedVolume != 0) {
            VolumeUtils.setStreamVolume(context, audioChannel, usedVolume);
            Logcat.d("【播放音频】设置后音量变成 " + VolumeUtils.getStreamVolume(context, audioChannel));
        }
        try {
            StoppableMediaPlayer player = new StoppableMediaPlayer();
            player.setVolume(1f, 1f);
            if (uriPath.startsWith(ContentResolver.SCHEME_ANDROID_RESOURCE)) {
                Uri uri = Uri.parse(uriPath);
                Logcat.d("【播放音频】: play with resId=" + uri.toString());
                player.setDataSource(context, uri);
            } else {
                Logcat.d("【播放音频】: play with uri=" + uriPath);
                player.setDataSource(uriPath);
            }
            if (!Consts.DEFAULT_AUDIO_USAGE.equals(usage)) {
                Logcat.d("【播放音频】: 切换到声音通道=" + usage);
                player.setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(usageChannel)
                        .build());
            }
            player.setLooping(false);
            player.setOnPreparedListener(MediaPlayer::start);
            player.setOnStopListener(() -> {
                if (usedVolume != 0) {
                    VolumeUtils.setStreamVolume(context, audioChannel, originVolume);
                    Logcat.d("【播放音频】onStop 将音量还原到" + VolumeUtils.getStreamVolume(context, audioChannel));
                }
            });
            player.setOnErrorListener((mp, what, extra) -> {
                if (usedVolume != 0) {
                    VolumeUtils.setStreamVolume(context, audioChannel, originVolume);
                    Logcat.d("【播放音频】onError 将音量还原到" + VolumeUtils.getStreamVolume(context, audioChannel));
                }
                return false;
            });
            player.setOnCompletionListener(mp -> {
                if (usedVolume != 0) {
                    VolumeUtils.setStreamVolume(context, audioChannel, originVolume);
                    Logcat.d("【播放音频】onCompletion 将音量还原到" + VolumeUtils.getStreamVolume(context, audioChannel));
                }
                if (listener != null)
                    listener.onCompletion(mp);
            });
            player.prepareAsync();
            return player;
        } catch (Exception e) {
            e.printStackTrace();
            Logcat.d("【播放音频】出现异常", IOUtils.getExceptionStackInfo(e));
        }
        return null;
    }
}
