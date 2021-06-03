package cn.bavelee.giaotone.adapter.binder;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.litepal.LitePal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bavelee.giaotone.R;
import cn.bavelee.giaotone.adapter.entity.ToneControlEntity;
import cn.bavelee.giaotone.model.SoundItem;
import cn.bavelee.giaotone.ui.dialog.ChangeEnabledTimeDialog;
import cn.bavelee.giaotone.ui.dialog.SelectSoundDialog;
import cn.bavelee.giaotone.util.PlayUtils;
import me.drakeet.multitype.ItemViewBinder;

public class ToneControlViewBinder extends ItemViewBinder<ToneControlEntity, ToneControlViewBinder.ToneControlHolder> {

    private Context context;
    private MediaPlayer mediaPlayer;
    private Handler handler;

    public static SoundItem getSoundItemBySoundId(int soundId) {
        SoundItem soundItem = LitePal.where("id = ?", String.valueOf(soundId)).findFirst(SoundItem.class);
        if (soundItem == null)
            return LitePal.findFirst(SoundItem.class);
        else return soundItem;
    }


    @NonNull
    @Override
    protected ToneControlHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return new ToneControlHolder(inflater.inflate(R.layout.item_tone_control, parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull final ToneControlHolder holder, @NonNull final ToneControlEntity item) {
        if (context == null)
            context = holder.itemView.getContext();
        final SoundItem sound = getSoundItemBySoundId(item.getSoundId());
        holder.tvTitle.setText(item.getTitle());
        holder.tvTitle.setTextColor(item.getColor());
        holder.tvCustomSound.setTextColor(item.getColor());
        holder.tvSoundFile.setTextColor(item.getColor());
        holder.tvSoundFile.setText(context.getString(R.string.sound_file, sound.getName()));
        // 调用setChecked之前设置为null防止自动调用回调接口
        holder.soundSwitch.setOnCheckedChangeListener(null);
        holder.soundSwitch.setChecked(item.isEnabled());
        holder.ivPlay.setImageResource(!item.isPlaying() ? R.drawable.ic_play : R.drawable.ic_stop);
        holder.soundSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setEnabled(isChecked);
            item.save();
        });
        holder.tvCustomSound.setOnClickListener(v -> SelectSoundDialog.newInstance(new SelectSoundDialog.OnSoundListDialogListener() {
            @Override
            public void onSelected(SoundItem SoundItem) {
                if (SoundItem != null) {
                    item.setSoundId(SoundItem.getId());
                    item.save();
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onDataChanged() {
                reload();
            }
        }).show(((AppCompatActivity) context).getSupportFragmentManager(), null));
        holder.ivPlay.setOnClickListener(v -> {
            if (!item.isPlaying()) {
                if (mediaPlayer != null) {
                    stopPlay();
                    for (Object obj : getAdapter().getItems()) {
                        if (obj instanceof ToneControlEntity) {
                            ((ToneControlEntity) obj).setPlaying(false);
                        }
                    }
                    notifyDataSetChanged();
                }
                mediaPlayer = PlayUtils.playSound(context, sound.getUrl(), mp -> {
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
        holder.tvModifyLevel.setVisibility(View.GONE);
        if (item.isBatteryLevelMode()) {
            holder.tvModifyLevel.setTextColor(item.getColor());
            holder.tvModifyLevel.setVisibility(View.VISIBLE);
            holder.tvModifyLevel.setOnClickListener(v -> showBatteryLevelDialog(item));
            holder.tvTitle.setText(context.getString(R.string.battery_level_title, item.getBatteryLevel()));
        }
        holder.itemView.setOnLongClickListener(v -> {
            AppCompatActivity activity = (AppCompatActivity) context;
            ChangeEnabledTimeDialog.newInstance(item).show(activity.getSupportFragmentManager(), null);
            return true;
        });
    }

    void reload() {
        for (Object item : getAdapter().getItems()) {
            if (item instanceof ToneControlEntity) {
                ((ToneControlEntity) item).refreshFromDatabase();
            }
        }
        notifyDataSetChanged();
    }

    void notifyDataSetChanged() {
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                getAdapter().notifyDataSetChanged();
            }
        });
    }

    private int selectedItem = -1;

    private void showBatteryLevelDialog(ToneControlEntity item) {
        selectedItem = -1;
        CharSequence[] cs = new CharSequence[99];
        int checkedItem = 0;
        int level = item.getBatteryLevel();
        for (int i = 1; i <= 99; i++) {
            cs[i - 1] = String.valueOf(i);
            if (level == i) checkedItem = i - 1;
        }
        if (context instanceof Activity && !((Activity) context).isFinishing())
            new AlertDialog.Builder(context)
                    .setTitle(R.string.change_level)
                    .setSingleChoiceItems(cs, checkedItem, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            selectedItem = which;
                        }
                    })
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (selectedItem != -1) {
                                item.setBatteryLevel(Integer.parseInt(cs[selectedItem].toString()));
                                item.save();
                                notifyDataSetChanged();
                            }
                            dialog.cancel();
                        }
                    })
                    .show();
    }


    private void stopPlay() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public static class ToneControlHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tvTitle)
        TextView tvTitle;
        @BindView(R.id.tvSoundFile)
        TextView tvSoundFile;
        @BindView(R.id.ivPlay)
        AppCompatImageView ivPlay;
        @BindView(R.id.tvCustomSound)
        AppCompatTextView tvCustomSound;
        @BindView(R.id.tvModifyLevel)
        AppCompatTextView tvModifyLevel;
        @BindView(R.id.soundSwitch)
        SwitchCompat soundSwitch;

        public ToneControlHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
