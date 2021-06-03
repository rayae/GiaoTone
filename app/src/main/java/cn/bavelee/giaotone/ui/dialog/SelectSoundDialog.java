package cn.bavelee.giaotone.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.litepal.LitePal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.bavelee.giaotone.R;
import cn.bavelee.giaotone.adapter.SoundListAdapter;
import cn.bavelee.giaotone.model.SoundItem;
import cn.bavelee.giaotone.ui.dialog.base.BaseDialog;

public class SelectSoundDialog extends BaseDialog {

    public static SelectSoundDialog newInstance(OnSoundListDialogListener callback) {
        SelectSoundDialog fragment = new SelectSoundDialog();
        fragment.callback = callback;
        return fragment;
    }


    private OnSoundListDialogListener callback;
    private boolean isCalledCallback = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dialog_select_sound, null, false);
        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!isCalledCallback)
            invokeCallback(null);
    }


    private OnSoundListDialogListener mCallback = new OnSoundListDialogListener() {
        @Override
        public void onSelected(SoundItem item) {
            invokeCallback(item);
            dismissAllowingStateLoss();
        }

        @Override
        public void onDataChanged() {
            if (callback != null)
                callback.onDataChanged();
        }
    };

    private void invokeCallback(SoundItem item) {
        if (callback != null) {
            isCalledCallback = true;
            callback.onSelected(item);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        LinearLayoutManager lm = new LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false);
        RecyclerView mRecyclerView = view.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(lm);
        mRecyclerView.setAdapter(SoundListAdapter.newInstance(getLayoutInflater(), LitePal.findAll(SoundItem.class), mCallback));
        view.findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                invokeCallback(null);
                dismissAllowingStateLoss();
            }
        });
        TextView title = view.findViewById(R.id.tvTitle);
        title.setText("修改音频");
        super.onViewCreated(view, savedInstanceState);
    }

    public interface OnSoundListDialogListener {
        void onSelected(SoundItem item);

        void onDataChanged();
    }
}
