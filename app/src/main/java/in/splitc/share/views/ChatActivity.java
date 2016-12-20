package in.splitc.share.views;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.splitc.share.R;
import in.splitc.share.ZApplication;
import in.splitc.share.adapters.ChatAdapter;
import in.splitc.share.data.Message;
import in.splitc.share.data.User;
import in.splitc.share.db.MessagesDBWrapper;
import in.splitc.share.utils.CommonLib;
import in.splitc.share.utils.ImageLoader;
import in.splitc.share.utils.UploadManager;
import in.splitc.share.utils.UploadManagerCallback;
import okhttp3.FormBody;

/**
 * Created by neo on 06/11/16.
 */
public class ChatActivity extends AppCompatActivity implements UploadManagerCallback {

    private Activity mContext;
    private boolean destroyed = false;
    private SharedPreferences prefs;
    private ZApplication zapp;
    private int width, height;
    private LayoutInflater inflater;

    private User user;
    private int userId;
    private ImageLoader loader;

    // Notification flags
    private boolean fromNotification = false;

    private List<Message> messages = new ArrayList<Message>();
    private RecyclerView recyclerView;
    private ChatAdapter mAdapter;


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
        UploadManager.addCallback(this);

        if (getIntent() != null ) {
            if (getIntent().hasExtra("user")) {
                user = (User) getIntent().getSerializableExtra("user");
                userId = user.getUserId();
            }
            else if (getIntent().hasExtra("userId")) {
                userId = getIntent().getIntExtra("userId", 0);
                fetchUserDetails();
            }
        } else
            finish();

        recyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new ChatAdapter(messages, ChatActivity.this);
        recyclerView.setAdapter(mAdapter);

        // register receiver
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mNotificationReceived, new IntentFilter(CommonLib.LOCAL_CHAT_BROADCAST));

        setUpActionBar();

        setListeners();

        new FetchChats().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);

    }

    private void fetchUserDetails() {
        FormBody.Builder requestBuilder = new FormBody.Builder();
        requestBuilder.add("access_token", prefs.getString("access_token", ""));
        requestBuilder.add("client_id", CommonLib.CLIENT_ID);
        requestBuilder.add("app_type", CommonLib.APP_TYPE);
        requestBuilder.add("userId", userId+"");
        String url = CommonLib.SERVER_URL + "user/details";
        UploadManager.postDataToServer(UploadManager.USER_DETAILS, url, requestBuilder);
    }

    private void setUpActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        if (user != null) {
            ((TextView)findViewById(R.id.titleTextView)).setText(user.getUserName());
            if (user.getProfilePic() != null)
                loader.setImageFromUrlOrDisk(user.getProfilePic(), (ImageView) findViewById(R.id.imageView), "", width, width, false);
        }
    }

    private void setListeners() {

        ((TextView)findViewById(R.id.messageEditText)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    findViewById(R.id.sendButton).setEnabled(true);
                } else {
                    findViewById(R.id.sendButton).setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


        findViewById(R.id.sendButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = ((TextView)findViewById(R.id.messageEditText)).getText().toString();

                if (message.length() < 1) {
                    return;
                }

                FormBody.Builder requestBuilder = new FormBody.Builder();
                requestBuilder.add("access_token", prefs.getString("access_token", ""));
                requestBuilder.add("client_id", CommonLib.CLIENT_ID);
                requestBuilder.add("app_type", CommonLib.APP_TYPE);
                requestBuilder.add("message", message);
                requestBuilder.add("userId", userId+"");
                String url = CommonLib.SERVER_URL + "message/send";
                UploadManager.postDataToServer(UploadManager.SEND_MESSAGE, url, requestBuilder);

                Message messageObj = new Message();
                messageObj.setSender(prefs.getInt("userId", 0));
                messageObj.setTo(userId);
                messageObj.setMessage(message);
                if (user != null) {
                    messageObj.setUserId(user.getUserId());
                    messageObj.setUserName(user.getUserName());
                    messageObj.setProfilePic(user.getProfilePic());
                }
                messageObj.setTimestamp(System.currentTimeMillis()/1000);
                MessagesDBWrapper.addMessage(messageObj, System.currentTimeMillis()/1000, prefs.getInt("userId", 0));

                messages.add(messageObj);
                mAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1);

                ((TextView)findViewById(R.id.messageEditText)).setText("");
            }
        });

        final View activityRootView = findViewById(R.id.chat_root);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                if (heightDiff > 100 && recyclerView != null && mAdapter != null && mAdapter.getItemCount() > 0) {
                    recyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
                }
            }
        });
    }

    @Override
    public void uploadStarted(int requestType, Object data) {

    }

    @Override
    public void uploadFinished(int requestType, Object data, boolean status, String errorMessage) {
        if (requestType == UploadManager.SEND_MESSAGE) {

        } else if (requestType == UploadManager.USER_DETAILS) {
            if (status && data instanceof Object[] && ((Object[])data)[0] instanceof User) {
                User user = (User)((Object[])data)[0];
                if (user.getUserId() == userId) {
                    this.user = user;
                    setUpActionBar();
                }
            }
        }
    }

    private class FetchChats extends AsyncTask<Object, Void, Object> {

        // execute the api
        @Override
        protected Object doInBackground(Object... params) {
            try {
                return MessagesDBWrapper.getMessages(userId);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            if (destroyed)
                return;

            if (result != null && result instanceof ArrayList<?>) {
                messages.clear();
                messages.addAll((ArrayList<Message>)result);
                mAdapter.notifyDataSetChanged();

                if (messages.size() == 0) {
                    ((EditText)findViewById(R.id.messageEditText)).requestFocus();
                    try {
                        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(((EditText) findViewById(R.id.messageEditText)), InputMethodManager.SHOW_FORCED);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    recyclerView.smoothScrollToPosition(messages.size() - 1);
                }

            } else {
                // something went wrong with the DB connection
            }
        }
    }

    private BroadcastReceiver mNotificationReceived = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getExtras() != null && intent.getExtras().containsKey("message")) {
                Message message = (Message) intent.getExtras().getSerializable("message");

                if (message != null) {

                    // check if this is your chat only
                    if (userId == message.getSender() || message.getTo() == userId) {
                        // my notifications shall be removed from the tray
//                        NotificationManager.getInstance(ChatActivity.this).cancelAll();
                    } else
                        return;

                    messages.add(message);
                    mAdapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(messages.size() - 1);
                    recyclerView.getLayoutManager().scrollToPosition(messages.size() - 1);
                }
            }
        }
    };


    @Override
    public void onDestroy() {
        destroyed = true;
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mNotificationReceived);
        UploadManager.removeCallback(this);
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