package cn.bavelee.giaotone.util;

import android.media.MediaPlayer;

public class StoppableMediaPlayer extends MediaPlayer {

    private OnStopListener listener;

    public void setOnStopListener(OnStopListener listener) {
        this.listener = listener;
    }

    @Override
    public void stop() throws IllegalStateException {
        if (listener != null) listener.onStop();
        super.stop();
    }

    public interface OnStopListener {
        void onStop();
    }
}
