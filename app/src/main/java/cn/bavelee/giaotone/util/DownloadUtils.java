package cn.bavelee.giaotone.util;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;

public class DownloadUtils {

    // 调用系统 DownloadManger 的下载器
    public static void download(Context context, String url, Uri uri, String title, String description, final OnDownloadFile onDownloadFile) {
        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setAllowedOverRoaming(false);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
            request.setTitle(title);
            request.setDescription(description);
            request.setVisibleInDownloadsUi(true);
            request.setDestinationUri(uri);
            final DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            final long downloadId = downloadManager.enqueue(request);
            context.registerReceiver(new BroadcastReceiver() {
                                         @Override
                                         public void onReceive(Context context, Intent intent) {
                                             DownloadManager.Query query = new DownloadManager.Query();
                                             query.setFilterById(downloadId);
                                             Cursor c = downloadManager.query(query);
                                             if (c.moveToFirst()) {
                                                 int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                                                 switch (status) {
                                                     case DownloadManager.STATUS_SUCCESSFUL:
                                                         onDownloadFile.downloadSuccess();
                                                         break;
                                                     case DownloadManager.STATUS_FAILED:
                                                         onDownloadFile.downloadFailure();
                                                         break;
                                                 }
                                             }
                                             c.close();
                                             context.unregisterReceiver(this);
                                         }
                                     },
                    new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        } catch (Exception ignore) {

        }

    }

    public interface OnDownloadFile {
        void downloadSuccess();

        void downloadFailure();
    }

}