package cn.bavelee.easy.appupdate;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HttpUtils {
    public static String getWebContent(String downloadUrl) {
        HttpURLConnection connection;
        StringBuilder acceptData = new StringBuilder();
        try {
            URL url = new URL(downloadUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.connect();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                String line;
                while ((line = bufferedReader.readLine()) != null) { //不为空进行操作
                    acceptData.append(line).append("\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return acceptData.toString();
    }
}
