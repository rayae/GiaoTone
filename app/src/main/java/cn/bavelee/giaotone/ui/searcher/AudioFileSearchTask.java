package cn.bavelee.giaotone.ui.searcher;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.bavelee.giaotone.App;
import cn.bavelee.giaotone.model.SoundItem;
import cn.bavelee.giaotone.util.Logcat;

public class AudioFileSearchTask implements Runnable {

    private Callback callback;
    private HashMap<String, SoundItem> itemMap = new HashMap<>();
    private boolean isStopped = false;

    public AudioFileSearchTask(Callback callback) {
        this.callback = callback;
    }

    public void setStopped(boolean stopped) {
        isStopped = stopped;
    }

    public void start() {
        new Thread(this).start();
    }

    public void walk(File root) {
        if (isStopped) return;
        File[] list = root.listFiles();
        if (list != null)
            for (File f : list) {
                if (isStopped) return;
                if (f.isDirectory()) {
                    walk(f);
                } else {
                    String name = f.getName();
                    if (name.endsWith("mp3") || name.endsWith("ogg") || name.endsWith("wma") || name.endsWith("avi") || name.endsWith("mp4") || name.endsWith("rm")
                            || name.endsWith("aac") || name.endsWith("wav") || name.endsWith("dts") || name.endsWith("ac3") || name.endsWith("m4a") || name.endsWith("flac")) {
                        App.post(() -> {
                            if (callback != null) {
                                SoundItem item = new SoundItem(f.getName(), f.getAbsolutePath());
                                if (!itemMap.containsKey(item.getUrl())) {
                                    itemMap.put(item.getUrl(), item);
                                    callback.onSearched(item);
                                }
                            }
                        });
                    }
                }
            }
    }

    @Override
    public void run() {
        App.post(() -> {
            if (callback != null) callback.onStarted();
        });
        List<File> searchList = new ArrayList<>();
        String env = System.getenv("EXTERNAL_STORAGE");
        if (env == null) {
            env = Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        Logcat.d("EXTERNAL_STORAGE=%s", env);
        File sdcard = new File(env);
        searchList.add(new File(sdcard, "Android/data/com.tencent.mobileqq/Tencent/QQfile_recv"));
        searchList.add(new File(sdcard, "Android/data/com.tencent.mm/MicroMsg/Download"));
        searchList.add(new File(sdcard, "Tencent/QQfile_recv/"));
        searchList.add(new File(sdcard, "Download"));
        searchList.add(new File(sdcard, "Downloads"));
        searchList.add(sdcard);

        for (File file : searchList) {
            Logcat.d("Walking : %s", file.getAbsolutePath());
            walk(file);
        }
        if (isStopped) return;
        App.post(() -> {
            List<SoundItem> items = new ArrayList<>(itemMap.values());
            if (callback != null) callback.onSearchFinished(items);
        });
    }

    public interface Callback {
        void onStarted();

        void onSearched(SoundItem item);

        void onSearchFinished(List<SoundItem> items);
    }
}
