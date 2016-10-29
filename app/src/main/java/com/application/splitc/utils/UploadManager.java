package com.application.splitc.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Debug;
import android.widget.Toast;

import com.application.splitc.ZApplication;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Hashtable;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by apoorvarora on 06/10/16.
 */
public class UploadManager {

    public static Hashtable<Integer, AsyncTask> asyncs = new Hashtable<Integer, AsyncTask>();
    public static Context context;
    private static SharedPreferences prefs;
    private static ArrayList<UploadManagerCallback> callbacks = new ArrayList<UploadManagerCallback>();
    private static ZApplication zapp;

    // Request tags
    public static final int LOGIN = 101;
    public static final int REGISTER = 102;
    public static final int UPDATE = 103;
    public static final int LOGOUT = 104;
    public static final int APP_CONFIG = 105;

    public static final String HEADER_KEY_TOKEN = "accesstoken";
    public static final String HEADER_KEY_USERID = "userid";

    public static void Initialize(Context context) {
        UploadManager.context = context;
        prefs = context.getSharedPreferences("application_settings", 0);

        if (context instanceof ZApplication) {
            zapp = (ZApplication) context;
        }
    }

    public static void addCallback(UploadManagerCallback callback) {
        if (!callbacks.contains(callback)) {
            callbacks.add(callback);
        }

        // this is here because its called from a lot of places.
        if ((double) Debug.getNativeHeapAllocatedSize() / Runtime.getRuntime().maxMemory() > .70) {
            if (zapp != null) {

                if (zapp.cache != null)
                    zapp.cache.clear();
            }
        }
    }

    public static void removeCallback(UploadManagerCallback callback) {
        if (callbacks.contains(callback)) {
            callbacks.remove(callback);
        }
    }

    public static void fetchDataFromServer(int requestType, String requestUrl) {
        for (UploadManagerCallback callback : callbacks) {
            callback.uploadStarted(requestType, requestUrl);
        }
        new FetchDataFromServer().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Object[]{requestType, requestUrl});
    }

    public static void postDataToServer(int requestType, String requestUrl, JSONObject requestObject) {
        for (UploadManagerCallback callback : callbacks) {
            callback.uploadStarted(requestType, requestUrl);
        }
        new PostDataToServer().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Object[]{requestType, requestUrl, requestObject});
    }

    public static void putDataToServer(int requestType, String requestUrl, JSONObject requestObject) {
        for (UploadManagerCallback callback : callbacks) {
            callback.uploadStarted(requestType, requestUrl);
        }
        new PutDataToServer().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Object[]{requestType, requestUrl, requestObject});
    }

    private static class FetchDataFromServer extends AsyncTask<Object, Void, Object[]> {

        private int requestType;
        private String requestUrl;

        @Override
        protected Object[] doInBackground(Object... params) {

            requestType = (Integer) params[0];
            requestUrl = (String) params[1];

            Object result[] = new Object[]{"", false, "Something went wrong"};

            String token = prefs.getString("access_token", "");
            int userId = prefs.getInt("userId", 0);

            Request
                    request = new Request.Builder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader(HEADER_KEY_TOKEN, token)
                    .addHeader(HEADER_KEY_USERID, userId + "")
                    .url(requestUrl)
                    .get()
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            Call call = okHttpClient.newCall(request);

            try {
                Response response = call.execute();
                return ParserJson.parseData(requestType, response.body().string());
            } catch (Exception e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(Object[] arg) {
            if (arg != null && arg.length == 3 && !(Boolean) arg[1])
                Toast.makeText(context, (String) arg[2], Toast.LENGTH_SHORT).show();

            for (UploadManagerCallback callback : callbacks) {
                callback.uploadFinished(requestType, arg, (boolean) arg[1], "");
            }
        }
    }


    private static class PostDataToServer extends AsyncTask<Object, Void, Object[]> {

        private int requestType;
        private String requestUrl;
        private JSONObject requestObject;

        @Override
        protected Object[] doInBackground(Object... params) {

            requestType = (Integer) params[0];
            requestUrl = (String) params[1];
            requestObject = (JSONObject) params[2];

            Object result[] = new Object[]{"", false, "Something went wrong"};

            String token = prefs.getString("access_token", "");
            int userId = prefs.getInt("userId", 0);
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestObject.toString());

            Request
                    request = new Request.Builder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader(HEADER_KEY_TOKEN, token)
                    .addHeader(HEADER_KEY_USERID, userId + "")
                    .url(requestUrl)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            Call call = okHttpClient.newCall(request);

            try {
                Response response = call.execute();
                return ParserJson.parseData(requestType, response.body().string());
            } catch (Exception e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(Object[] arg) {
            if (arg != null && arg.length == 3 && !(Boolean) arg[1])
                Toast.makeText(context, (String) arg[2], Toast.LENGTH_SHORT).show();

            for (UploadManagerCallback callback : callbacks) {
                callback.uploadFinished(requestType, arg, (boolean) arg[1], "");
            }
        }
    }

    private static class PutDataToServer extends AsyncTask<Object, Void, Object[]> {

        private int requestType;
        private String requestUrl;
        private JSONObject requestObject;

        @Override
        protected Object[] doInBackground(Object... params) {

            requestType = (Integer) params[0];
            requestUrl = (String) params[1];
            requestObject = (JSONObject) params[2];

            Object result[] = new Object[]{"", false, "Something went wrong"};

            String token = prefs.getString("access_token", "");
            int userId = prefs.getInt("userId", 0);
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestObject.toString());

            Request
                    request = new Request.Builder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader(HEADER_KEY_TOKEN, token)
                    .addHeader(HEADER_KEY_USERID, userId + "")
                    .url(requestUrl)
                    .put(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            Call call = okHttpClient.newCall(request);

            try {
                Response response = call.execute();
                return ParserJson.parseData(requestType, response.body().string());
            } catch (Exception e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(Object[] arg) {
            if (arg != null && arg.length == 3 && !(Boolean) arg[1])
                Toast.makeText(context, (String) arg[2], Toast.LENGTH_SHORT).show();

            for (UploadManagerCallback callback : callbacks) {
                callback.uploadFinished(requestType, arg, (boolean) arg[1], "");
            }
        }
    }

    public static void logout(String accessToken) {
        for (UploadManagerCallback callback : callbacks) {
            callback.uploadStarted(LOGOUT, "");
        }
        new Logout().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Object[]{accessToken});
    }

    private static class Logout extends AsyncTask<Object, Void, Object[]> {

        @Override
        protected Object[] doInBackground(Object... params) {

            Object result[] = new Object[]{"", false, "Something went wrong"};

            FormBody.Builder requestBuilder = new FormBody.Builder();
            requestBuilder.add("access_token", (String) params[0]);
            requestBuilder.add("client_id", CommonLib.CLIENT_ID);
            requestBuilder.add("app_type", CommonLib.APP_TYPE);

            RequestBody requestBody = requestBuilder.build();
            String url = CommonLib.SERVER_URL + "auth/logout?";
            Request
                    request = new Request.Builder()
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .url(url)
                    .put(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            Call call = okHttpClient.newCall(request);

            try {
                Response response = call.execute();
                return ParserJson.parse_GenericStringResponse(new JSONObject(response.body().string()));
            } catch (Exception e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(Object[] arg) {
            if (arg != null && arg.length == 3 && !(Boolean) arg[1])
                Toast.makeText(context, (String) arg[2], Toast.LENGTH_SHORT).show();

            for (UploadManagerCallback callback : callbacks) {
                callback.uploadFinished(LOGOUT, arg, (boolean) arg[1], "");
            }
        }
    }
}
