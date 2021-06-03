package cn.bavelee.giaotone.ui.dialog;

import cn.bavelee.giaotone.App;
import cn.bavelee.giaotone.R;

public class ErrorDialog {

    public static MessageDialog newInstance(String message) {
        return MessageDialog.newInstance(App.getContext().getString(R.string.error_occur), message, App.getContext().getResources().getColor(R.color.colorError), App.getContext().getResources().getColor(R.color.colorAccent), false);
    }

}
