package cn.bavelee.giaotone.adapter.entity;

import android.view.View;

public class ClickableTextEntity {
    private String text;
    private View.OnClickListener listener;

    public ClickableTextEntity(String text, View.OnClickListener listener) {
        this.text = text;
        this.listener = listener;
    }

    public View.OnClickListener getListener() {
        return listener;
    }

    public void setListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    public ClickableTextEntity(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
