package cn.bavelee.giaotone.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bavelee.giaotone.Consts;
import cn.bavelee.giaotone.R;
import cn.bavelee.giaotone.adapter.SoundListAdapter;
import cn.bavelee.giaotone.model.OnlineSoundList;
import cn.bavelee.giaotone.util.HttpUtils;

public class OnlineActivity extends AppCompatActivity {

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    private OnlineSoundList onlineSoundList;
    private String textFilter;
    private MenuItem menuFilter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online);
        ButterKnife.bind(this);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        HttpUtils.requestData(this, Consts.URL_SOUND_LIBRARY, OnlineSoundList.class, new HttpUtils.OnData<OnlineSoundList>() {
            @Override
            public void processData(OnlineSoundList data) {
                onlineSoundList = data;
                menuFilter.setVisible(true);
                showData(data.getList());
            }

            @Override
            public void onError(String msg) {
                Toast.makeText(OnlineActivity.this, getString(R.string.request_data_failure, msg), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.filter, menu);
        menuFilter = menu.findItem(R.id.action_filter);
        menuFilter.setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        if (item.getItemId() == R.id.action_filter) {
            final AppCompatEditText editText = new AppCompatEditText(this);
            if (textFilter != null)
                editText.setText(textFilter);
            editText.setSingleLine();
            editText.setTypeface(Typeface.MONOSPACE);
            editText.setHint("输入要筛选的文件名称，比如：抖音");
            if (!isFinishing())
                new AlertDialog.Builder(this)
                        .setView(editText)
                        .setTitle("输入筛选的名称")
                        .setCancelable(false)
                        .setNeutralButton("不筛选", (dialog, which) -> refresh(null))
                        .setPositiveButton("筛选", (dialog, which) -> {
                            String s = editText.getText().toString();
                            refresh(TextUtils.isEmpty(s) ? null : s);
                        }).show();
        }
        return super.onOptionsItemSelected(item);
    }

    void refresh(String filter) {
        List<OnlineSoundList.Sound> list = new ArrayList<>();
        if (filter != null) {
            for (OnlineSoundList.Sound sound : onlineSoundList.getList()) {
                if (sound.getName().contains(filter) || sound.getCategory().contains(filter))
                    list.add(sound);
            }
        } else {
            list.addAll(onlineSoundList.getList());
        }
        textFilter = filter;
        showData(list);
    }

    void showData(List<OnlineSoundList.Sound> list) {
        if (recyclerView.getLayoutManager() == null) {
            LinearLayoutManager ll = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(ll);
        }
        recyclerView.setAdapter(SoundListAdapter.newInstance(getLayoutInflater(), list));

    }
}