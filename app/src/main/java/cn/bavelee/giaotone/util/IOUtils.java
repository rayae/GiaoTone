package cn.bavelee.giaotone.util;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class IOUtils {

    public static String getUriRealFileName(Context context, Uri uri) {
        if (uri == null) return null;
        String result = null;
        try {
            if (uri.getScheme() != null && uri.getScheme().equals("content")) {
                try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                }
            }
            if (result == null) {
                result = uri.getPath();
                if (result == null) return null;
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        } catch (Exception ignore) {
            return null;
        }
        return result;
    }

    public static File getCacheFile(Context context, String fileName, String subDir) {
        String state = Environment.getExternalStorageState();
        String baseDir = null;
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File baseDirFile = context.getExternalFilesDir(null);
            if (baseDirFile == null) {
                baseDir = context.getFilesDir().getAbsolutePath();
            } else {
                baseDir = baseDirFile.getAbsolutePath();
            }
        } else {
            baseDir = context.getFilesDir().getAbsolutePath();
        }
        if (subDir != null) {
            File sub = new File(baseDir, subDir);
            sub.mkdirs();
            return new File(sub, fileName);
        }
        return new File(baseDir, fileName);
    }

    public static File getCacheFile(Context context, String fileName) {
        return getCacheFile(context, fileName, null);
    }

    public static void saveImageToGallery(Context context, Bitmap bmp, File file) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 60, fos);
            fos.flush();
            fos.close();
            MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), file.getName(), null);
            Uri uri = Uri.fromFile(file);
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("TrustAllX509TrustManager")
    public static void enableAllSSLHandshake() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {

                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {

                }
            }};
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

        } catch (Exception ignored) {

        }
    }

    public static String getExceptionStackInfo(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    public static boolean writeInputStreamToCache(InputStream is, File file) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int byteRead;
            while (-1 != (byteRead = is.read(buffer))) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
            fileOutputStream.flush();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static Uri convertResIdToUri(Context context, int resId) {
        Resources r = context.getResources();
        Uri uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                + r.getResourcePackageName(resId) + "/"
                + r.getResourceTypeName(resId) + "/"
                + r.getResourceEntryName(resId));
        return uri;
    }
}
