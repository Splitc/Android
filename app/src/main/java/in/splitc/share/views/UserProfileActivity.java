package in.splitc.share.views;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import in.splitc.share.R;
import in.splitc.share.ZApplication;
import in.splitc.share.data.User;
import in.splitc.share.utils.CommonLib;
import in.splitc.share.utils.ImageLoader;
import in.splitc.share.utils.UploadManager;
import in.splitc.share.utils.UploadManagerCallback;
import okhttp3.FormBody;

/**
 * Created by apoorvarora on 10/10/16.
 */
public class UserProfileActivity extends AppCompatActivity implements UploadManagerCallback {

    private Activity mContext;
    private boolean destroyed = false;
    private SharedPreferences prefs;
    private ZApplication zapp;
    private int width, height;
    private LayoutInflater inflater;
    private int userId;
    ImageLoader loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        prefs = getSharedPreferences("application_settings", 0);
        zapp = (ZApplication) getApplication();
        mContext = this;
        width = getWindowManager().getDefaultDisplay().getWidth();
        height = getWindowManager().getDefaultDisplay().getHeight();
        inflater = LayoutInflater.from(mContext);

        loader = new ImageLoader(mContext, zapp);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupActionBar();

        if ( getIntent() != null && getIntent().hasExtra("userId") && getIntent().getIntExtra("userId", 0) != prefs.getInt("userId", 0)) {
            userId = getIntent().getIntExtra("userId", 0);

            UploadManager.addCallback(this);
            //fetch profile details and open the page
            String url = CommonLib.SERVER_URL + "user/details";
            FormBody.Builder requestBuilder = new FormBody.Builder();
            requestBuilder.add("access_token", prefs.getString("access_token", ""));
            requestBuilder.add("client_id", CommonLib.CLIENT_ID);
            requestBuilder.add("app_type", CommonLib.APP_TYPE);
            requestBuilder.add("userId", getIntent().getIntExtra("userId", 0)+"");
            UploadManager.postDataToServer(UploadManager.USER_DETAILS, url, requestBuilder);
        } else {
            userId = prefs.getInt("userId", 0);
            ((TextView)findViewById(R.id.name)).setText(prefs.getString("username", ""));
            loader.setImageFromUrlOrDisk(prefs.getString("profile_pic", ""), ((ImageView) findViewById(R.id.user_image)), "", width, height, false);
            loader.setImageFromUrlOrDisk(prefs.getString("profile_pic", ""), ((ImageView) findViewById(R.id.drawer_user_info_background_image)), "", width, height, false);
        }
    }

    private void setupActionBar() {
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);

        if (Build.VERSION.SDK_INT > 20)
            actionBar.setElevation(0);

        LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarCustomView = inflator.inflate(R.layout.transparent_cross_action_bar, null);
        actionBarCustomView.findViewById(R.id.home_icon_container).setVisibility(View.VISIBLE);
        actionBar.setCustomView(actionBarCustomView);

        actionBarCustomView.findViewById(R.id.back_icon).setPadding(width / 20 + width / 80 + width / 100, 0, width / 20, 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void actionBarSelected(View v) {
        switch (v.getId()) {

            case R.id.home_icon_container:
                onBackPressed();
            default:
                break;
        }
    }

    @Override
    public void onDestroy() {
        if (loader != null)
            loader.setDestroyed(true);
        UploadManager.removeCallback(this);
        super.onDestroy();
    }

    @Override
    public void uploadStarted(int requestType, Object data) {

    }

    @Override
    public void uploadFinished(int requestType, Object data, boolean status, String errorMessage) {
        if (requestType == UploadManager.USER_DETAILS) {
            if (!destroyed) {
                if (status && data instanceof Object[] && ((Object[])data)[0] instanceof User) {
                    User user = (User)((Object[])data)[0];
                    if (user.getUserId() == userId) {
                        // update the details
                        loader.setImageFromUrlOrDisk(user.getProfilePic(), ((ImageView) findViewById(R.id.user_image)), "", width, height, false);
                        loader.setImageFromUrlOrDisk(user.getProfilePic(), ((ImageView) findViewById(R.id.drawer_user_info_background_image)), "", width, height, false);
                        ((TextView)findViewById(R.id.name)).setText(user.getUserName());
                    }
                }
            }
        }
    }
}
