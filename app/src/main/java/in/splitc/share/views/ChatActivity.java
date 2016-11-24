package in.splitc.share.views;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import in.splitc.share.R;
import in.splitc.share.ZApplication;
import in.splitc.share.data.Feed;
import in.splitc.share.utils.CommonLib;
import in.splitc.share.utils.ImageLoader;

/**
 * Created by neo on 06/11/16.
 */
public class ChatActivity extends AppCompatActivity  {

    private Activity mContext;
    private boolean destroyed = false;
    private SharedPreferences prefs;
    private ZApplication zapp;
    private int width, height;
    private LayoutInflater inflater;
    private ProgressDialog zProgressDialog;

    private Feed ride;
    private ImageLoader loader;
    private String startAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accept_ride);

        prefs = getSharedPreferences("application_settings", 0);
        zapp = (ZApplication) getApplication();
        mContext = this;
        width = getWindowManager().getDefaultDisplay().getWidth();
        height = getWindowManager().getDefaultDisplay().getHeight();
        inflater = LayoutInflater.from(mContext);
        loader = new ImageLoader(this, zapp);

        if (getIntent() != null ) {
            if (getIntent().hasExtra("feedItem"))
                ride = (Feed) getIntent().getSerializableExtra("feedItem");
            if (getIntent().hasExtra("startAddress"))
                startAddress = getIntent().getStringExtra("startAddress");
        } else
            finish();

        setUpActionBar();

        setListeners();
    }

    private void setUpActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(ride.getFromAddress() + " to " + ride.getToAddress());
    }

    private void setListeners() {

    }

    @Override
    public void onDestroy() {
        destroyed = true;
        if (zProgressDialog != null && zProgressDialog.isShowing())
            zProgressDialog.dismiss();
        super.onDestroy();
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

}