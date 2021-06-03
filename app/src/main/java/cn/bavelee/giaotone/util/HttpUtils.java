package cn.bavelee.giaotone.util;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONObject;

public class HttpUtils {
    private static RequestQueue mQueue;

    // 多类型 json 请求封装
    public static <T> void requestData(final Context context, String url, final Class<T> clazz, final OnData<T> onData) {
        if (mQueue == null) {
            mQueue = Volley.newRequestQueue(context.getApplicationContext());
        }

        CharsetJsonRequest request = new CharsetJsonRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    onData.processData(new Gson().fromJson(response.toString(), clazz));
                } catch (Exception ignore) {

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Logcat.d("网络请求出错", IOUtils.getExceptionStackInfo(error));
                onData.onError(error.toString());
            }
        });
        mQueue.add(request);
    }

    public interface OnData<T> {
        void processData(T data);

        void onError(String msg);
    }
}
