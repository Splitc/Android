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

import in.splitc.share.R;
import in.splitc.share.ZApplication;
import in.splitc.share.utils.ImageLoader;

/**
 * Created by apoorvarora on 10/10/16.
 */
public class UserProfileActivity extends AppCompatActivity {

    private Activity mContext;
    private boolean destroyed = false;
    private SharedPreferences prefs;
    private ZApplication zapp;
    private int width, height;
    private LayoutInflater inflater;

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

        loader.setImageFromUrlOrDisk(prefs.getString("profile_pic", ""), ((ImageView)findViewById(R.id.user_image)), "", width, height, false);
        loader.setImageFromUrlOrDisk(prefs.getString("profile_pic", ""), ((ImageView)findViewById(R.id.drawer_user_info_background_image)), "", width, height, false);
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
        super.onDestroy();
    }
}
