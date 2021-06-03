package cn.bavelee.giaotone.ui.launcher;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;
import cn.bavelee.giaotone.ui.MainActivity;

public class NormalLauncher extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.start(this);
        finish();
    }
}
