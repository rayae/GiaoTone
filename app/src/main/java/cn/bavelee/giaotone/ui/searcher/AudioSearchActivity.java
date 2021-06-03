package cn.bavelee.giaotone.ui.searcher;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import cn.bavelee.giaotone.R;
import cn.bavelee.giaotone.adapter.SoundListAdapter;
import cn.bavelee.giaotone.model.SoundItem;

public class AudioSearchActivity extends AppCompatActivity implements AudioFileSearchTask.Callback {
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    SoundListAdapter adapter;
    @BindView(R.id.progressLayout)
    LinearLayout progressLayout;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.tv1)
    TextView tv1;
    private MenuItem menuFilter;
    private AudioFileSearchTask mTask;
    private String textFilter;
    private List<SoundItem> items = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_search);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);
        progressLayout.setVisibility(View.GONE);
        adapter = SoundListAdapter.newInstance(this, items);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        mTask = new AudioFileSearchTask(this);
        mTask.start();
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
            if (mTask != null)
                mTask.setStopped(true);
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
        List<SoundItem> list = new ArrayList<>();
        if (filter != null) {
            for (SoundItem si : items) {
                if (si.getName().contains(filter))
                    list.add(si);
            }
        } else {
            list.addAll(items);
        }
        textFilter = filter;
        adapter.setItems(list);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        if (mTask != null)
            mTask.setStopped(true);
        super.onBackPressed();
    }

    @Override
    public void onStarted() {
        progressLayout.setVisibility(View.VISIBLE);
        items.clear();
        if (menuFilter != null)
            menuFilter.setVisible(false);
        if (adapter != null)
            adapter.notifyDataSetChanged();
    }

    @Override
    public void onSearched(SoundItem item) {
        items.add(item);
        tv1.setText(getString(R.string.searching_count, items.size()));
        if (adapter != null)
            adapter.notifyItemInserted(items.size() - 1);
    }

    @Override
    public void onSearchFinished(List<SoundItem> items) {
        progressBar.setVisibility(View.GONE);
        if (menuFilter != null)
            menuFilter.setVisible(true);
    }
}
