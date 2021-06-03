package cn.bavelee.giaotone.ui.dialog;

import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bavelee.giaotone.App;
import cn.bavelee.giaotone.R;
import cn.bavelee.giaotone.ui.dialog.base.BaseDialog;

public class MessageDialog extends BaseDialog {

    @BindView(R.id.tvMessage)
    AppCompatTextView tvMessage;
    @BindView(R.id.tvTitle)
    AppCompatTextView tvTitle;
    @BindView(R.id.btn1)
    AppCompatTextView btn1;
    @BindView(R.id.copy)
    AppCompatTextView copy;
    private boolean cancelable;
    private String message;
    private String title;
    private int colorTitle = -1;
    private int colorBtn = -1;

    public static MessageDialog newInstance(String title, String message, int colorTitle, int colorBtn, boolean cancelable) {
        MessageDialog dialog = new MessageDialog();
        dialog.message = message;
        dialog.title = title;
        dialog.colorBtn = colorBtn;
        dialog.colorTitle = colorTitle;
        dialog.cancelable = cancelable;
        return dialog;
    }

    public static MessageDialog newInstance(String title, String message) {
        return newInstance(title, message, App.getContext().getResources().getColor(R.color.colorPrimaryDark), App.getContext().getResources().getColor(R.color.colorPrimary), false);
    }

    public static MessageDialog newInstance(String title, String message, boolean cancelable) {
        return newInstance(title, message, App.getContext().getResources().getColor(R.color.colorPrimaryDark), App.getContext().getResources().getColor(R.color.colorPrimary), cancelable);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dialog_message, null, false);
        setCancelable(cancelable);
        ButterKnife.bind(this, root);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        btn1.setOnClickListener(v -> dismissAllowingStateLoss());
        tvTitle.setTextColor(colorTitle);
        btn1.setTextColor(colorBtn);
        tvMessage.setText(message);
        tvTitle.setText(title);
        copy.setVisibility(View.VISIBLE);
        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setText(message);
                Toast.makeText(getActivity(), R.string.copied, Toast.LENGTH_SHORT).show();
            }
        });
        super.onViewCreated(view, savedInstanceState);
    }
}

