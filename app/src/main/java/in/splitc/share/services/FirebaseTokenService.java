package in.splitc.share.services;

import android.content.SharedPreferences;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import in.splitc.share.utils.CommonLib;
import in.splitc.share.utils.UploadManager;
import okhttp3.FormBody;

/**
 * Created by apoorvarora on 07/10/16.
 */
public class FirebaseTokenService extends FirebaseInstanceIdService {

    private static final String TAG = FirebaseTokenService.class.getName();
    private SharedPreferences prefs;

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = getSharedPreferences("application_settings", 0);
    }

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        CommonLib.ZLog(TAG, "Refreshed token: " + refreshedToken);
        prefs.edit().putString("regId", refreshedToken).commit();
        if (prefs.getInt("userId", 0) > 0)
            registerInBackground(refreshedToken);
    }

    private void registerInBackground(String regId) {
        if (regId != null && !regId.isEmpty()) {
            // Getting registration token
            String requestUrl = CommonLib.SERVER_URL + "user/registrationId";
            FormBody.Builder requestBuilder = new FormBody.Builder();
            requestBuilder.add("access_token", prefs.getString("access_token", ""));
            requestBuilder.add("client_id", CommonLib.CLIENT_ID);
            requestBuilder.add("app_type", CommonLib.APP_TYPE);
            requestBuilder.add("pushId", regId);
            UploadManager.postDataToServer(UploadManager.UPDATE_REG_ID, requestUrl, requestBuilder);
        }
    }

}
