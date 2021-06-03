package cn.bavelee.easy.appupdate;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;

public class AppUpdate {

    public static final int PARSE_FAILURE = 1001;
    public static final int SUCCESS = 1000;

    private int colorBg = -1, colorText = -1;
    private String textTitle, textNotUpdate, textUpdateNow, updateUrl;
    private UpdateDataParser parser;
    private Context context;
    private UpdateCallback callback;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == SUCCESS) {
                final UpdateData data = (UpdateData) msg.obj;
                if (!compareVerCode(data.getVerCode())) {
                    if (callback != null)
                        callback.onNoUpdate();
                    return true;
                }
                View dialogView = LayoutInflater.from(context).inflate(R.layout.layout_dialog_app_update, null);
                TextView tvTitle = dialogView.findViewById(R.id.tvTitle);
                TextView tvMessage = dialogView.findViewById(R.id.tvMessage);
                Button btnNot = dialogView.findViewById(R.id.btnNotUpdate);
                TextView btnUpdate = dialogView.findViewById(R.id.btnUpdateNow);
                if (textTitle != null)
                    tvTitle.setText(textTitle);
                if (textNotUpdate != null)
                    btnNot.setText(textNotUpdate);
                if (textUpdateNow != null)
                    btnUpdate.setText(textUpdateNow);
                if (colorBg != -1) {
                    tvTitle.setBackgroundColor(colorBg);
                    btnNot.setBackgroundColor(colorBg);
                    btnUpdate.setBackgroundColor(colorBg);
                }
                if (colorText != -1) {
                    tvTitle.setTextColor(colorText);
                    btnNot.setTextColor(colorText);
                    btnUpdate.setTextColor(colorText);
                }
                tvMessage.setText(data.getContent());
                btnNot.setVisibility(data.isIsForceUpdate() ? View.GONE : View.VISIBLE);
                final AlertDialog dialog = new AlertDialog.Builder(context)
                        .setCancelable(false)
                        .setView(dialogView)
                        .show();
                btnNot.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (dialog != null)
                            dialog.cancel();
                    }
                });
                btnUpdate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (callback != null)
                            callback.onUpdate(data);
                        if (dialog != null)
                            dialog.cancel();
                    }
                });
            } else {
                if (callback != null)
                    callback.onError(msg.obj == null ? "" : msg.obj.toString());
            }
            return true;
        }
    });

    private boolean compareVerCode(int verCode) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode < verCode;
        } catch (Exception ignore) {
            return false;
        }
    }

    private AppUpdate(Context context) {
        this.context = context;
    }

    private void check(UpdateCallback callback) {
        this.callback = callback;
        if (updateUrl == null) {
            handler.sendMessage(handler.obtainMessage(PARSE_FAILURE, "updateUrl not set"));
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                UpdateData data = null;
                String response = HttpUtils.getWebContent(updateUrl);
                if (parser != null) {
                    data = parser.parseData(response);
                } else {
                    try {
                        data = new UpdateData();
                        JSONObject json = new JSONObject(response);
                        data.setContent(json.optString("content", "null"));
                        data.setVerCode(json.optInt("verCode", -1));
                        data.setVerName(json.optString("verName", "null"));
                        data.setIsForceUpdate(json.optBoolean("isForceUpdate", false));
                        data.setDownloadUrl(json.optString("downloadUrl", "null"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        handler.sendMessage(handler.obtainMessage(PARSE_FAILURE, e.toString()));
                        return;
                    }
                }
                handler.sendMessage(handler.obtainMessage(SUCCESS, data));
            }
        }).start();
    }

    public static class Builder {
        AppUpdate mUpdate;

        public Builder(Context context) {
            mUpdate = new AppUpdate(context);
        }

        public void checkUpdate(UpdateCallback callback) {
            mUpdate.check(callback);
        }

        public Builder setColorBg(int colorBg) {
            mUpdate.colorBg = colorBg;
            return this;
        }

        public Builder setColorText(int colorText) {
            mUpdate.colorText = colorText;
            return this;
        }

        public Builder setTextTitle(String textTitle) {
            mUpdate.textTitle = textTitle;
            return this;
        }

        public Builder setTextNotUpdate(String textNotUpdate) {
            mUpdate.textNotUpdate = textNotUpdate;
            return this;
        }

        public Builder setTextUpdateNow(String textUpdateNow) {
            mUpdate.textUpdateNow = textUpdateNow;
            return this;
        }

        public Builder setUpdateUrl(String updateUrl) {
            mUpdate.updateUrl = updateUrl;
            return this;
        }

        public Builder setParser(UpdateDataParser parser) {
            mUpdate.parser = parser;
            return this;
        }

    }


}
