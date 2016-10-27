package com.application.splitc.views;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.application.splitc.R;
import com.application.splitc.ZApplication;
import com.application.splitc.utils.CommonLib;
import com.application.splitc.utils.TypefaceSpan;
import com.application.splitc.utils.UploadManager;
import com.application.splitc.utils.UploadManagerCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

/**
 * Created by neo on 16/10/16.
 */
public class FeedbackActivity extends AppCompatActivity implements UploadManagerCallback{

    int screenWidth;
    int screenHeight;

    private ZApplication zapp;
    private SharedPreferences prefs;

    final Context context = this;

    private TextView feedbackEmailText;
    private final int EMAIL_FEEDBACK = 1500;
    private View actionBarCustomView;
    private boolean destroyed = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_feedback);
        UploadManager.addCallback(this);
        Display display = getWindowManager().getDefaultDisplay();
        screenWidth = display.getWidth();

        feedbackEmailText = (TextView) findViewById(R.id.feedback_email);
        feedbackEmailText.setTextColor(getResources().getColor(R.color.black));
        feedbackEmailText.setText(getFeedbackEmailSpannableText(), TextView.BufferType.SPANNABLE);
        feedbackEmailText.setMovementMethod(LinkMovementMethod.getInstance());
        feedbackEmailText.setPadding(0, screenWidth / 20, 0, 0);

        zapp = (ZApplication) getApplication();
        prefs = getSharedPreferences("application_settings", 0);

        screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        setUpActionBar();
        fixSizes();
    }

    private void setUpActionBar() {
        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);

        LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        actionBarCustomView = inflator.inflate(R.layout.white_action_bar, null);
        actionBarCustomView.findViewById(R.id.home_icon_container).setVisibility(View.VISIBLE);
        actionBar.setCustomView(actionBarCustomView);

        SpannableString s = new SpannableString(getString(R.string.feedback_title));
        s.setSpan(
                new TypefaceSpan(getApplicationContext(), CommonLib.FONT_BOLD,
                        getResources().getColor(R.color.white), getResources().getDimension(R.dimen.size16)),
                0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        TextView title = (TextView) actionBarCustomView.findViewById(R.id.title);

        actionBarCustomView.findViewById(R.id.title).setPadding(screenWidth / 40, 0, screenWidth / 40, 0);
        title.setText(s);
    }

    @Override
    public void onDestroy() {
        destroyed = true;
        UploadManager.removeCallback(this);
        super.onDestroy();
    }

    public void actionBarSelected(View v) {

        switch (v.getId()) {

            case R.id.home_icon_container:
                onBackPressed();

            default:
                break;
        }

    }

    private SpannableString getFeedbackEmailSpannableText() {

        String feedbackText = getResources().getString(R.string.feedback_email);
        String email = "hello@zapplon.com";
        SpannableString ss = new SpannableString(feedbackText);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                feedbackEmailText.setEnabled(false);
                sendFeedbackEmail();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
                ds.setTypeface(CommonLib.getTypeface(getApplicationContext(), CommonLib.FONT_LIGHT));
                ds.setTextSize(getResources().getDimension(R.dimen.size12));
                ds.setColor(getResources().getColor(R.color.z_red_feedback));
            }
        };

        if (feedbackText.indexOf(email) > -1)
            ss.setSpan(clickableSpan, feedbackText.indexOf(email), feedbackText.indexOf(email) + email.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return ss;
    }

    private void sendFeedbackEmail() {
        Intent i = new Intent(Intent.ACTION_SEND);

        i.setType("application/octet-stream");
        i.putExtra(Intent.EXTRA_EMAIL, new String[] { "hellp@zapplon.com" });
        i.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.feedback_email_subject));

        try {
            final String LogString = new String("App Version  : " + CommonLib.VERSION_STRING + "\n" + "Connection   : "
                    + "\n" + "Identifier   : " + prefs.getString("app_id", "") + "\n"
                    + /* "Location     : " + zapp.lat + " , " + zapp.lon + */"\n" + "User Id     	: "
                    + prefs.getInt("uid", 0) + "\n" + "User Agent   : "
                    + "&device=" + Build.MANUFACTURER + ", "
                    + Build.BRAND + ", " + Build.MODEL);

            FileOutputStream fOut = openFileOutput("log.txt", MODE_WORLD_READABLE);
            File file = getFileStreamPath("log.txt");
            Uri uri = Uri.fromFile(file);
            OutputStreamWriter osw = new OutputStreamWriter(fOut);
            osw.write(LogString);
            osw.flush();
            osw.close();
            i.putExtra(Intent.EXTRA_STREAM, uri);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            startActivityForResult(Intent.createChooser(i, getResources().getString(R.string.send_mail)),
                    EMAIL_FEEDBACK);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(context, getResources().getString(R.string.no_email_clients),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EMAIL_FEEDBACK) {
            deleteFile("log.txt");
            feedbackEmailText.setEnabled(true);
            // onBackPressed();
        }
    }

    private void fixSizes() {

        if (actionBarCustomView != null) {
            actionBarCustomView.findViewById(R.id.tick_container).setVisibility(View.GONE);
        }

        findViewById(R.id.submit_button).getLayoutParams().height = screenWidth / 10;
        findViewById(R.id.submit_button).setEnabled(false);
        findViewById(R.id.submit_button).setClickable(false);

        findViewById(R.id.feedback_content).getLayoutParams().height = screenHeight / 2;
        EditText feedbackContent = (EditText) findViewById(R.id.feedback_content);
        feedbackContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equals("") || s.toString().trim().length() < 1) {
                    actionBarCustomView.findViewById(R.id.tick_container).setVisibility(View.GONE);
					/*
					 * findViewById(R.id.submit_button).setEnabled(false);
					 * findViewById(R.id.submit_button).setClickable(false);
					 * findViewById(R.id.submit_button).setBackgroundColor(
					 * getResources().getColor(R.color.zhl_dark));
					 */
                } else {/*
						 * findViewById(R.id.submit_button).setEnabled(true);
						 * findViewById(R.id.submit_button).setClickable(true);
						 * findViewById(R.id.submit_button).
						 * setBackgroundDrawable(getResources().getDrawable(R.
						 * drawable.greenbuttonfeedback));
						 */
                    actionBarCustomView.findViewById(R.id.tick_container).setVisibility(View.VISIBLE);
                }
            }
        });

        feedbackContent.setPadding(screenWidth / 40, screenWidth / 40, screenWidth / 40, 0);
        findViewById(R.id.feedback_container).setPadding(screenWidth / 20, screenWidth / 20, screenWidth / 20,
                screenWidth / 20);
    }

    private String getLogString() {
        String LogString = new String("App Version  : " + CommonLib.VERSION_STRING + "\n" + "Connection   : "
                + "\n" + "Identifier   : " + prefs.getString("app_id", "") + "\n"
                + /* "Location     : " + zapp.lat + " , " + zapp.lon + */ "\n" + "User Id      : "
                + prefs.getInt("uid", 0) + "\n" + "User Agent   : "
                + "&device=" + Build.MANUFACTURER + ","
                + Build.BRAND + "," + Build.MODEL);

        return LogString;
    }

    @Override
    public void onBackPressed() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(findViewById(R.id.feedback_content).getWindowToken(), 0);
        finish();
    }

    public void submit(View v) {

        if (!((EditText) findViewById(R.id.feedback_content)).getText().toString().equals("")) {
            String message = ((EditText) findViewById(R.id.feedback_content)).getText().toString();
            String LogString = new String("App Version  : " + CommonLib.VERSION_STRING + "\n" + "Connection   : "
                    + "\n" + "Identifier   : " + prefs.getString("app_id", "") + "\n"
                    + "Location     : " + zapp.lat + " , " + zapp.lon + "\n" + "User Id      : "
                    + prefs.getInt("uid", 0) + "\n" + "User Agent   : "
                    + "&device=" + Build.MANUFACTURER + ","
                    + Build.BRAND + "," + Build.MODEL);
//            UploadManager.sendFeedback(message, LogString);
        }
    }

    public void proceed(View v) {
        if (!((EditText) findViewById(R.id.feedback_content)).getText().toString().equals("")
                && ((EditText) findViewById(R.id.feedback_content)).getText().toString().trim().length() > 0) {
            String message = ((EditText) findViewById(R.id.feedback_content)).getText().toString();
            String LogString = new String("App Version  : " + CommonLib.VERSION_STRING + "\n" + "Connection   : "
                    + "\n" + "Identifier   : " + prefs.getString("app_id", "") + "\n"
                    + "Location     : " + zapp.lat + " , " + zapp.lon + "\n" + "User Id      : "
                    + prefs.getInt("uid", 0) + "\n" + "User Agent   : "
                    + "&device=" + Build.MANUFACTURER + ","
                    + Build.BRAND + "," + Build.MODEL);

//            UploadManager.sendFeedback(message, LogString);
        }
    }

    public void goBack(View view) {
        onBackPressed();
    }

    @Override
    public void uploadStarted(int requestType, Object data) {

    }

    @Override
    public void uploadFinished(int requestType, Object data, boolean status, String errorMessage) {
//        if (requestType == CommonLib.SEND_FEEDBACK) {
//            if (!destroyed && status) {
//                Toast.makeText(FeedbackActivity.this, "Hey! Thanks for your feeback, means a lot to us!",
//                        Toast.LENGTH_LONG).show();
//                onBackPressed();
//            }
//        }
    }
}
