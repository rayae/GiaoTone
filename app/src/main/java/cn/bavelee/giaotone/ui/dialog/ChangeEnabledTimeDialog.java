package cn.bavelee.giaotone.ui.dialog;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bavelee.giaotone.R;
import cn.bavelee.giaotone.adapter.entity.ToneControlEntity;

public class ChangeEnabledTimeDialog extends DialogFragment {

    @BindView(R.id.cancel)
    AppCompatTextView cancel;
    @BindView(R.id.enabledSwitch)
    SwitchCompat enabledSwitch;
    @BindView(R.id.tvStartTime)
    TextView tvStartTime;
    @BindView(R.id.tvEndTime)
    TextView tvEndTime;
    private ToneControlEntity entity;

    public static ChangeEnabledTimeDialog newInstance(ToneControlEntity entity) {
        ChangeEnabledTimeDialog fragment = new ChangeEnabledTimeDialog();
        fragment.entity = entity;
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        Window mWindow = getDialog().getWindow();
        WindowManager.LayoutParams mLayoutParams = mWindow.getAttributes();
        mLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        mLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        mLayoutParams.gravity = Gravity.CENTER;
        mWindow.setAttributes(mLayoutParams);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_change_enabled_time, null);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tvStartTime.setText(entity.getStartTime());
        tvEndTime.setText(entity.getEndTime());
        enabledSwitch.setChecked(entity.isTimedEnabled());
        cancel.setOnClickListener(v -> {
            entity.save();
            dismissAllowingStateLoss();
        });
        enabledSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> entity.setTimedEnabled(isChecked));
        ((View) tvStartTime.getParent()).setOnClickListener(v -> showTimePicker(true));
        ((View) tvEndTime.getParent()).setOnClickListener(v -> showTimePicker(false));
        super.onViewCreated(view, savedInstanceState);
    }

    private void showTimePicker(boolean isStartTime) {
        String[] ss = isStartTime ? entity.getStartTime().split(":") : entity.getEndTime().split(":");
        int hourOfDay = Integer.parseInt(ss[0]);
        int minutes = Integer.parseInt(ss[1]);
        if (!getActivity().isFinishing())
            new TimePickerDialog(getActivity(), (view, hourOfDay1, minute) -> {
                String now = String.format(Locale.CHINESE, "%02d:%02d", hourOfDay1, minute);
                if (isStartTime)
                    entity.setStartTime(now);
                else entity.setEndTime(now);
                tvStartTime.setText(entity.getStartTime());
                tvEndTime.setText(entity.getEndTime());
            }, hourOfDay, minutes, true).show();
    }

    public void show(FragmentManager manager, String tag) {
        try {
            Field mDismissed = this.getClass().getSuperclass().getDeclaredField("mDismissed");
            Field mShownByMe = this.getClass().getSuperclass().getDeclaredField("mShownByMe");
            mDismissed.setAccessible(true);
            mShownByMe.setAccessible(true);
            mDismissed.setBoolean(this, false);
            mShownByMe.setBoolean(this, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(this, tag);
        ft.commitAllowingStateLoss();
    }
}
