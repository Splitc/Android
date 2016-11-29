package in.splitc.share.views;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;

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

    // Notification flags
    private boolean fromNotification = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

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

        // register receiver
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mNotificationReceived, new IntentFilter(CommonLib.LOCAL_CHAT_BROADCAST));

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

    private BroadcastReceiver mNotificationReceived = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getExtras() != null && intent.getExtras().containsKey("message")) {
//                MessageDetails message = (MessageDetails) intent.getExtras().getSerializable("message");
//
//                if (message != null) {
//
//                    // check if this is your chat only
//                    if ( (userId == message.getSender() || CommonLib.listContainsNumber(message.getTo(), userId))
//                            && ((message.getChatType() != null && message.getChatType().equals(CommonLib.CHAT_TYPE_CREATE))
//                            || (chatId != 0 && chatId == message.getChatId()))) {
//                        // my notifications shall be removed from the tray
//                        in.cloudtech.vyom.utils.NotificationManager.getInstance(ChatActivity.this).cancelAll();
//                    } else
//                        return;
//
//                        messages.add(message);
//                        mAdapter.notifyDataSetChanged();
//                        mMessageRecyclerView.scrollToPosition(messages.size() - 1);
//                        mMessageRecyclerView.getLayoutManager().scrollToPosition(messages.size() - 1);
//                }
            }
        }
    };


    @Override
    public void onDestroy() {
        destroyed = true;
        if (zProgressDialog != null && zProgressDialog.isShowing())
            zProgressDialog.dismiss();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mNotificationReceived);
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

    @Override
    public void onBackPressed() {

        try {
            CommonLib.hideKeyboard(ChatActivity.this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (fromNotification) {
            Intent upIntent = new Intent(getApplicationContext(), HomeActivity.class);
            if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                TaskStackBuilder.create(this).addNextIntentWithParentStack(upIntent).startActivities();
            } else {
                upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(upIntent);
                finish();
            }
        } else {
            super.onBackPressed();
        }
    }

}