package cn.bavelee.giaotone.adapter;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.LitePal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bavelee.giaotone.Consts;
import cn.bavelee.giaotone.R;
import cn.bavelee.giaotone.adapter.entity.ToneControlEntity;
import cn.bavelee.giaotone.model.OnlineSoundList;
import cn.bavelee.giaotone.model.SoundItem;
import cn.bavelee.giaotone.ui.MainActivity;
import cn.bavelee.giaotone.ui.dialog.SelectSoundDialog;
import cn.bavelee.giaotone.util.DownloadUtils;
import cn.bavelee.giaotone.util.IOUtils;
import cn.bavelee.giaotone.util.Logcat;
import cn.bavelee.giaotone.util.PlayUtils;

public class SoundListAdapter extends RecyclerView.Adapter<SoundListAdapter.ViewHolder> {
    private LayoutInflater inflater;
    private MediaPlayer mediaPlayer;
    private boolean isOnlineMode;
    private boolean isSearchMode;
    private boolean isDownloading;
    private List<SoundItem> items;
    private SelectSoundDialog.OnSoundListDialogListener callback;
    private AppCompatActivity activity;

    public static SoundListAdapter newInstance(LayoutInflater inflater, List<SoundItem> items, SelectSoundDialog.OnSoundListDialogListener callback) {
        SoundListAdapter adapter = new SoundListAdapter(inflater, false);
        adapter.setItems((items));
        adapter.callback = callback;
        return adapter;
    }

    public static SoundListAdapter newInstance(AppCompatActivity activity, List<SoundItem> items) {
        SoundListAdapter adapter = new SoundListAdapter(activity.getLayoutInflater(), false, true);
        adapter.activity = activity;
        adapter.setItems(items);
        return adapter;
    }

    public static SoundListAdapter newInstance(LayoutInflater inflater, List<OnlineSoundList.Sound> list) {
        SoundListAdapter adapter = new SoundListAdapter(inflater, true);
        List<SoundItem> items = new ArrayList<>();
        for (OnlineSoundList.Sound sound : list) {
            SoundItem item = new SoundItem(sound.getName(), sound.getUrl(), false, true);
            item.setCategory(sound.getCategory());
            items.add(item);
        }
        adapter.setItems(items);
        return adapter;
    }

    public SoundListAdapter(LayoutInflater inflater, boolean isOnlineMode) {
        this.inflater = inflater;
        this.isOnlineMode = isOnlineMode;
    }

    public SoundListAdapter(LayoutInflater inflater, boolean isOnlineMode, boolean isSearchMode) {
        this.inflater = inflater;
        this.isOnlineMode = isOnlineMode;
        this.isSearchMode = isSearchMode;
    }

    public void setItems(List<SoundItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_online_sound, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final SoundItem item = items.get(position);
        final Context context = holder.itemView.getContext();
        holder.tvName.setText(item.getName());
        holder.ivPlay.setImageResource(item.isPlaying() ? R.drawable.ic_stop : R.drawable.ic_play);
        if (!isSearchMode) {
            holder.tvCategory.setText(item.getCategory());
            holder.tvCategory.setVisibility(isOnlineMode ? View.VISIBLE : View.GONE);
            holder.ivDownload.setVisibility(isOnlineMode ? View.VISIBLE : View.GONE);
            holder.ivDelete.setVisibility((isOnlineMode || item.isResId()) ? View.GONE : View.VISIBLE);
            holder.tvApplySound.setVisibility(isOnlineMode ? View.GONE : View.VISIBLE);
            holder.tvApplySound.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.onSelected(item);
                }
            });
            holder.ivDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (item.delete() == 1) {
                        Toast.makeText(context, context.getString(R.string.deleted, item.getName()), Toast.LENGTH_SHORT).show();
                        items.remove(item);
                        notifyItemRemoved(position);
                        new File((item.getUrl())).delete();
                        //若id的sound被应用的，则修改对应的到默认1的sound
                        int soundId = LitePal.findFirst(SoundItem.class).getId();
                        for (ToneControlEntity entity : LitePal.where("soundId = ?", String.valueOf(item.getId())).find(ToneControlEntity.class)) {
                            entity.setSoundId(soundId);
                        }
                        callback.onDataChanged();
                    } else {
                        Toast.makeText(context, context.getString(R.string.delete_failure, item.getName()), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            holder.ivDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isDownloading) {
                        Toast.makeText(context, R.string.only_can_download_one_file, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    isDownloading = true;
                    holder.ivDownload.setVisibility(View.GONE);
                    String fileName = System.currentTimeMillis() + ".xmp3";
                    final File file = IOUtils.getCacheFile(v.getContext(), fileName, Consts.AUDIO_SUB_DIR);
                    Logcat.d("下载链接 : " + item.getUrl());
                    DownloadUtils.download(context, item.getUrl(), Uri.fromFile(file), item.getName(), item.getName(), new DownloadUtils.OnDownloadFile() {
                        @Override
                        public void downloadSuccess() {
                            Logcat.d("已下载到 :" + file.getPath());
                            holder.ivDownload.setVisibility(View.GONE);
                            new SoundItem(item.getName(), file.getPath(), false, false).save();
                            Toast.makeText(context, context.getString(R.string.download_sound_success, item.getName()), Toast.LENGTH_SHORT).show();
                            isDownloading = false;
                        }

                        @Override
                        public void downloadFailure() {
                            isDownloading = false;
                            holder.ivDownload.setVisibility(View.VISIBLE);
                            Toast.makeText(context, context.getString(R.string.download_sound_failure), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } else {
            holder.ivDownload.setVisibility(View.GONE);
            holder.ivDelete.setVisibility(View.GONE);
            holder.tvCategory.setVisibility(View.VISIBLE);
            holder.tvApplySound.setVisibility(View.VISIBLE);
            holder.tvApplySound.setText(R.string.import_sound);
            holder.tvCategory.setText(item.getUrl());
            holder.tvApplySound.setOnClickListener(v -> {
                MainActivity.showEditDialog(activity, Uri.parse(item.getUrl()));
            });
        }
        holder.ivPlay.setOnClickListener(v -> {
            if (!item.isPlaying()) {
                if (mediaPlayer != null) {
                    stopPlay();
                    for (SoundItem i : items) {
                        i.setPlaying(false);
                    }
                    notifyDataSetChanged();
                }
                mediaPlayer = PlayUtils.playSound(context, item.getUrl(), mp -> {
                    stopPlay();
                    item.setPlaying(false);
                    notifyDataSetChanged();
                });
                item.setPlaying(mediaPlayer != null);
            } else {
                stopPlay();
                item.setPlaying(false);
            }
            notifyDataSetChanged();
        });
    }

    private void stopPlay() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static
    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tvName)
        TextView tvName;
        @BindView(R.id.tvCategory)
        TextView tvCategory;
        @BindView(R.id.ivDownload)
        AppCompatImageView ivDownload;
        @BindView(R.id.ivPlay)
        AppCompatImageView ivPlay;
        @BindView(R.id.ivDelete)
        AppCompatImageView ivDelete;
        @BindView(R.id.tvApplySound)
        AppCompatTextView tvApplySound;

        public ViewHolder(View rootView) {
            super(rootView);
            ButterKnife.bind(this, rootView);
        }

    }

}
