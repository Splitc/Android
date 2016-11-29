package in.splitc.share.views;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Session;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import in.splitc.share.R;
import in.splitc.share.ZApplication;
import in.splitc.share.services.FirebaseMessageService;
import in.splitc.share.services.FirebaseTokenService;
import in.splitc.share.utils.CommonLib;
import in.splitc.share.utils.UploadManager;
import in.splitc.share.utils.UploadManagerCallback;
import in.splitc.share.utils.facebook.FacebookConnect;
import in.splitc.share.utils.facebook.FacebookConnectCallback;
import okhttp3.FormBody;

/**
 * Created by apoorvarora on 10/10/16.
 */
public class SplashActivity extends AppCompatActivity implements FacebookConnectCallback, UploadManagerCallback {

    private int width;
    private int height;
    private SharedPreferences prefs;
    private ProgressDialog z_ProgressDialog;
    private boolean dismissDialog = false;
    private String APPLICATION_ID;
    private boolean destroyed = false;
    private Location loc;
    private String error_responseCode = "";
    private String error_exception = "";
    private String error_stackTrace = "";
    private ZApplication zapp;
    private Activity context;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private ImageView imgBg;
    private View background;
    private ViewPager mViewPager;
    private boolean firstBackground = true;
    private boolean hasSwipedPager = false;
    private View description;
    Animation animation1, animation2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        prefs = getSharedPreferences("application_settings", 0);
        context = this;
        zapp = (ZApplication) getApplication();
        APPLICATION_ID = prefs.getString("app_id", "");

        width = getWindowManager().getDefaultDisplay().getWidth();
        height = getWindowManager().getDefaultDisplay().getHeight();

        background = findViewById(R.id.baatna_background_frame);
        background.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));

        //stuffs
        mViewPager = (ViewPager) findViewById(R.id.tour_view_pager);
        mViewPager.setOffscreenPageLimit(4);

        TourPagerAdapter mTourPagerAdpater = new TourPagerAdapter();
        ((ViewPager) mViewPager).setAdapter(mTourPagerAdpater);

        fixSizes();
        animate();
        UploadManager.addCallback(this);
        updateDotsContainer();
        setListeners();

        findViewById(R.id.skip_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mViewPager != null) {
                    hasSwipedPager = true;
                    mViewPager.setCurrentItem(4, true);
                }
            }
        });

        if(prefs.getInt("userId", 0) == 0)
            findViewById(R.id.skip_container).setVisibility(View.VISIBLE);

        startTimer();
    }

    /**
     * Fetch the FCM registration ID and send it to the server
     * */
    private void registerInBackground() {
        // ID to be sent only when the user is logged in
        if (prefs.getInt("userId", 0) > 0) {
            String regId = prefs.getString("regId", "");
            // ID is already present
            if (!regId.isEmpty()) {
                String requestUrl = CommonLib.SERVER_URL + "user/registrationId";
                FormBody.Builder requestBuilder = new FormBody.Builder();
                requestBuilder.add("access_token", prefs.getString("access_token", ""));
                requestBuilder.add("client_id", CommonLib.CLIENT_ID);
                requestBuilder.add("app_type", CommonLib.APP_TYPE);
                requestBuilder.add("pushId", regId);
                UploadManager.postDataToServer(UploadManager.UPDATE_REG_ID, requestUrl, requestBuilder);
            } else {
                // User logged out or onTokenRefresh not called, initiate the service
                CommonLib.ZLog("token", "registration false");
                Intent intent = new Intent(SplashActivity.this, FirebaseTokenService.class);
                startService(intent);
            }
        }
    }

    private void setListeners() {
        ((ViewPager) mViewPager).setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            int position = ((ViewPager) mViewPager).getOffscreenPageLimit();

            @Override
            public void onPageSelected(int arg0) {

                final int pos = arg0;
                LinearLayout dotsContainer = (LinearLayout) findViewById(R.id.tour_dots);

                int index = 5;
                for (int count = 0; count < index; count++) {
                    ImageView dots = (ImageView) dotsContainer.getChildAt(count);

                    if (count == arg0)
                        dots.setImageResource(R.drawable.tour_image_dots_selected);
                    else
                        dots.setImageResource(R.drawable.tour_image_dots_unselected);
                }

                if (arg0 == 0 || arg0 == 1 || arg0 == 2) {
                    findViewById(R.id.signup_container).setVisibility(View.INVISIBLE);
                    if(prefs.getInt("userId", 0) == 0)
                        findViewById(R.id.skip_container).setVisibility(View.VISIBLE);
                    else
                        findViewById(R.id.skip_container).setVisibility(View.GONE);

                    if(arg0 == 0 && !firstBackground) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(!destroyed && pos == 0 && !firstBackground) {
                                    if (imgBg == null) {
                                        imgBg = (ImageView) findViewById(R.id.baatna_background_img);
                                        imgBg.getLayoutParams().width = width;
                                        imgBg.getLayoutParams().height = height;
                                        try {
                                            Bitmap bgBitmap = CommonLib.getBitmap(context, R.drawable.bg2, width, height);
                                            imgBg.setImageBitmap(bgBitmap);
                                        } catch (OutOfMemoryError e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    imgBg.setVisibility(View.GONE);
                                    background.setVisibility(View.VISIBLE);
                                    firstBackground = true;
                                }
                            }
                        }, 500);

                    } else if(firstBackground && arg0 != 0) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(!destroyed && firstBackground && pos != 0) {
                                    if (imgBg == null) {
                                        imgBg = (ImageView) findViewById(R.id.baatna_background_img);
                                        imgBg.getLayoutParams().width = width;
                                        imgBg.getLayoutParams().height = height;
                                        try {
                                            Bitmap bgBitmap = CommonLib.getBitmap(context, R.drawable.bg2, width, height);
                                            imgBg.setImageBitmap(bgBitmap);
                                        } catch (OutOfMemoryError e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    imgBg.setVisibility(View.VISIBLE);
                                    background.setVisibility(View.GONE);
                                    firstBackground = false;
                                }
                            }
                        }, 500);

                    }
                } else if (arg0 == 3 || arg0 == 4) {
                    findViewById(R.id.signup_container).setVisibility(View.VISIBLE);
                    findViewById(R.id.skip_container).setVisibility(View.GONE);
                    if(firstBackground && mViewPager.getCurrentItem() != 0) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(!destroyed && firstBackground && pos != 0) {
                                    if (imgBg == null) {
                                        imgBg = (ImageView) findViewById(R.id.baatna_background_img);
                                        imgBg.getLayoutParams().width = width;
                                        imgBg.getLayoutParams().height = height;
                                        try {
                                            Bitmap bgBitmap = CommonLib.getBitmap(context, R.drawable.bg2, width, height);
                                            imgBg.setImageBitmap(bgBitmap);
                                        } catch (OutOfMemoryError e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    imgBg.setVisibility(View.VISIBLE);
                                    background.setVisibility(View.GONE);
                                    firstBackground = false;
                                }
                            }
                        }, 500);

                    }
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });

        ((ViewPager) mViewPager).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hasSwipedPager = true;
                return false;
            }
        });
    }

    private void animate() {
        final View tourDots = findViewById(R.id.tour_dots);

        if(mViewPager != null && mViewPager.getCurrentItem() == 0 && mViewPager.getChildAt(mViewPager.getCurrentItem()) != null)
            description = mViewPager.getChildAt(mViewPager.getCurrentItem()).findViewById(R.id.description);

        tourDots.setVisibility(View.INVISIBLE);
        if(description != null)
            description.setVisibility(View.INVISIBLE);

        animation2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        animation2.setDuration(500);
        animation2.restrictDuration(700);
        animation2.scaleCurrentDuration(1);
        animation2.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }
        });

        animation1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up_center);
        animation1.setDuration(700);
        animation1.restrictDuration(700);
        animation1.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (prefs.getInt("userId", 0) == 0) {
                    tourDots.setVisibility(View.VISIBLE);
                    tourDots.startAnimation(animation2);
                } else {
                    navigateToHome();
                }
            }
        });
        animation1.scaleCurrentDuration(1);
        mViewPager.startAnimation(animation1);
    }

    private void fixSizes() {
        mViewPager.getLayoutParams().height = 2 * height / 3;
        ((RelativeLayout.LayoutParams)mViewPager.getLayoutParams()).setMargins(0, height / 7, 0, width / 20);
    }

    @Override
    public void response(Bundle bundle) {

        error_exception = "";
        error_responseCode = "";
        error_stackTrace = "";
        boolean regIdSent = false;

        if (bundle.containsKey("error_responseCode"))
            error_responseCode = bundle.getString("error_responseCode");

        if (bundle.containsKey("error_exception"))
            error_exception = bundle.getString("error_exception");

        if (bundle.containsKey("error_stackTrace"))
            error_stackTrace = bundle.getString("error_stackTrace");

        try {

            int status = bundle.getInt("status");

            if (status == 0) {

                if (!error_exception.equals("") || !error_responseCode.equals("") || !error_stackTrace.equals(""))
                    ;// BTODO
                // sendFailedLogsToServer();

                if (bundle.getString("errorMessage") != null) {
                    String errorMessage = bundle.getString("errorMessage");
                    Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.err_occurred, Toast.LENGTH_SHORT).show();
                }
                if (z_ProgressDialog != null && z_ProgressDialog.isShowing())
                    z_ProgressDialog.dismiss();
            } else {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("userId", bundle.getInt("userId"));
                if (bundle.containsKey("email"))
                    editor.putString("email", bundle.getString("email"));
                if (bundle.containsKey("description"))
                    editor.putString("description", bundle.getString("description"));
                if (bundle.containsKey("username"))
                    editor.putString("username", bundle.getString("username"));
                if (bundle.containsKey("thumbUrl"))
                    editor.putString("thumbUrl", bundle.getString("thumbUrl"));
                if (bundle.containsKey("profile_pic"))
                    editor.putString("profile_pic", bundle.getString("profile_pic"));
                if (bundle.containsKey("user_name"))
                    editor.putString("username", bundle.getString("username"));
                String token = bundle.getString("access_token");
                System.out.println(token);
                editor.putString("access_token", bundle.getString("access_token"));
                editor.putBoolean("verifiedUser", bundle.getBoolean("verifiedUser"));
                editor.commit();

                CommonLib.ZLog("login", "FACEBOOK");

                if (z_ProgressDialog != null && z_ProgressDialog.isShowing())
                    z_ProgressDialog.dismiss();
                navigateToHome();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void facebookAction(View view) {
        z_ProgressDialog = new ProgressDialog(SplashActivity.this,R.style.StyledDialog);
        z_ProgressDialog.setMessage(getResources().getString(R.string.verifying_creds));
        z_ProgressDialog.setCancelable(false);
        z_ProgressDialog.setIndeterminate(true);
        z_ProgressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        z_ProgressDialog.setCancelable(false);
        z_ProgressDialog.show();
        String regId = prefs.getString("registration_id", "");
        FacebookConnect facebookConnect = new FacebookConnect(SplashActivity.this, 1, APPLICATION_ID, true, regId, "");
        facebookConnect.execute();
        navigateToHome();
    }

    @Override
    protected void onDestroy() {
        destroyed = true;
        UploadManager.removeCallback(this);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        try {
            super.onActivityResult(requestCode, resultCode, intent);
            Session.getActiveSession().onActivityResult(this, requestCode, resultCode, intent);

        } catch (Exception w) {

            w.printStackTrace();

            try {
                com.facebook.Session fbSession = com.facebook.Session.getActiveSession();
                if (fbSession != null) {
                    fbSession.closeAndClearTokenInformation();
                }
                com.facebook.Session.setActiveSession(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (dismissDialog) {
            if (z_ProgressDialog != null) {
                z_ProgressDialog.dismiss();
            }
        }
    }

    @Override
    public void uploadStarted(int requestType, Object data) {
    }

    @Override
    public void uploadFinished(int requestType, Object data, boolean status, String errorMessage) {
        if (requestType == UploadManager.REGISTER) {
            if (destroyed)
                return;
            if (status) {
            }
        } else if (requestType == UploadManager.LOGIN) {
            if (destroyed)
                return;
            if (status) {
                JSONObject responseJSON = null;
                try {
                    responseJSON = new JSONObject(String.valueOf(data));
                    SharedPreferences.Editor editor = prefs.edit();
                    if (responseJSON.has("access_token")) {
                        editor.putString("access_token", responseJSON.getString("access_token"));
                    }
                    if (responseJSON.has("user_id") && responseJSON.get("user_id") instanceof Integer) {
                        editor.putInt("userId", responseJSON.getInt("userId"));
                    }
                    editor.commit();
                    navigateToHome();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class TourPagerAdapter extends PagerAdapter {

        View layout;

        protected View getView(){
            return layout;
        }
        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public Object instantiateItem(ViewGroup collection, int position) {

            RelativeLayout layout = (RelativeLayout) getLayoutInflater().inflate(R.layout.splash_screen_pager_snippet, null);
            this.layout = layout;

            if (position == 0) {

                ImageView tour_logo = (ImageView) layout.findViewById(R.id.baatna_logo);
                ImageView tour_text_logo = (ImageView) layout.findViewById(R.id.baatna_text);
                TextView tour_text= (TextView) layout.findViewById(R.id.description);

                tour_logo.setVisibility(View.VISIBLE);
                tour_text_logo.setVisibility(View.VISIBLE);

                if (prefs.getInt("userId", 0) == 0) {
                    tour_text.setVisibility(View.VISIBLE);
                } else {
                    tour_text.setVisibility(View.GONE);
                }

                // setting image
                try {

                    Bitmap logoBitmap = CommonLib.getBitmap(context, R.drawable.logo, width, width);
                    tour_logo.getLayoutParams().width = (int) ( 0.56 * width);
                    tour_logo.getLayoutParams().height = (int) (0.56 * width);
                    tour_logo.setImageBitmap(logoBitmap);

                    Bitmap splashTextBitmap = CommonLib.getBitmap(context, R.drawable.baatna_splash_text, width / 2, width / 10);
                    tour_text_logo.getLayoutParams().width = width / 2;
                    tour_text_logo.getLayoutParams().height = width / 10;
                    tour_text_logo.setImageBitmap(splashTextBitmap);

                    tour_text.setText(getResources().getString(R.string.splash_description_1));
                    int margin = (int) ((1.2 * 566 * width / 1266) - (0.43 * width));
                    if(margin > 0)
                        ((RelativeLayout.LayoutParams)tour_text.getLayoutParams()).setMargins(0, margin, 0, 0);

                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                    tour_logo.setBackgroundColor(getResources().getColor(R.color.transparent1));
                }
                if( mViewPager.getCurrentItem() == 0 )
                    findViewById(R.id.signup_container).setVisibility(View.INVISIBLE);

            } else if (position == 1) {

                ImageView tour_logo = (ImageView) layout.findViewById(R.id.baatna_logo);
                ImageView tour_text_logo = (ImageView) layout.findViewById(R.id.baatna_text);
                TextView tour_text= (TextView) layout.findViewById(R.id.description);
                tour_logo.setVisibility(View.VISIBLE);
                tour_text_logo.setVisibility(View.INVISIBLE);
                tour_text.setVisibility(View.VISIBLE);
                tour_text.setText(getResources().getString(R.string.splash_description_2));
                tour_text_logo.getLayoutParams().width = width / 2;
                tour_text_logo.getLayoutParams().height = width / 10;
                // setting image
                try {

                    Bitmap logoBitmap = CommonLib.getBitmap(context, R.drawable.tour_1, width, height);
                    tour_logo.getLayoutParams().width = 50 * width  / 89;//50
                    tour_logo.getLayoutParams().height = 39 * width / 89;//39, 89
                    tour_logo.setImageBitmap(logoBitmap);
                    int margin = (int) ((1.2 * 566 * width / 1266) - (39 * width / 89));
                    if(margin > 0)
                        ((RelativeLayout.LayoutParams)tour_text.getLayoutParams()).setMargins(0, margin, 0, 0);
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                    tour_logo.setBackgroundColor(getResources().getColor(R.color.transparent1));
                }
                if( mViewPager.getCurrentItem() == 1 )
                    findViewById(R.id.signup_container).setVisibility(View.INVISIBLE);

            } else if (position == 2) {

                ImageView tour_logo = (ImageView) layout.findViewById(R.id.baatna_logo);
                ImageView tour_text_logo = (ImageView) layout.findViewById(R.id.baatna_text);
                TextView tour_text= (TextView) layout.findViewById(R.id.description);
                tour_logo.setVisibility(View.VISIBLE);
                tour_text_logo.setVisibility(View.INVISIBLE);
                tour_text.setVisibility(View.VISIBLE);
                tour_text.setText(getResources().getString(R.string.splash_description_3));
                tour_text_logo.getLayoutParams().width = width / 2;
                tour_text_logo.getLayoutParams().height = width / 10;
                // setting image
                try {

                    Bitmap logoBitmap = CommonLib.getBitmap(context, R.drawable.tour_2,  width, height);
                    tour_logo.getLayoutParams().width = (int) (1.2 * 700 * width / 1266);//700
                    tour_logo.getLayoutParams().height = (int) (1.2 * 566 * width / 1266);//566
                    tour_logo.setImageBitmap(logoBitmap);
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                    tour_logo.setBackgroundColor(getResources().getColor(R.color.transparent1));
                }
                if( mViewPager.getCurrentItem() == 2 )
                    findViewById(R.id.signup_container).setVisibility(View.INVISIBLE);

            } else if (position == 3) {

                ImageView tour_logo = (ImageView) layout.findViewById(R.id.baatna_logo);
                ImageView tour_text_logo = (ImageView) layout.findViewById(R.id.baatna_text);
                TextView tour_text= (TextView) layout.findViewById(R.id.description);
                tour_logo.setVisibility(View.VISIBLE);
                tour_text_logo.setVisibility(View.INVISIBLE);
                tour_text.setVisibility(View.VISIBLE);
                tour_text.setText(getResources().getString(R.string.splash_description_4));
                tour_text_logo.getLayoutParams().width = width / 2;
                tour_text_logo.getLayoutParams().height = width / 10;
                // setting image
                try {

                    Bitmap logoBitmap = CommonLib.getBitmap(context, R.drawable.tour_3,  width, height);
                    tour_logo.getLayoutParams().width = 600 * width / 1147;//600
                    tour_logo.getLayoutParams().height = 547 * width / 1147;//547
                    tour_logo.setImageBitmap(logoBitmap);
                    int margin = (int) ((1.2 * 566 * width / 1266) - (547 * width / 1147));
                    if(margin > 0)
                        ((RelativeLayout.LayoutParams)tour_text.getLayoutParams()).setMargins(0, margin, 0, 0);
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                    tour_logo.setBackgroundColor(getResources().getColor(R.color.transparent1));
                }

                if( mViewPager.getCurrentItem() == 3 )
                    findViewById(R.id.signup_container).setVisibility(View.VISIBLE);

            } else if (position == 4) {

                ImageView tour_logo = (ImageView) layout.findViewById(R.id.baatna_logo);
                ImageView tour_text_logo = (ImageView) layout.findViewById(R.id.baatna_text);
                TextView tour_text= (TextView) layout.findViewById(R.id.description);
                tour_logo.setVisibility(View.VISIBLE);
                tour_text_logo.setVisibility(View.VISIBLE);
                tour_text.setVisibility(View.GONE);

                // setting image
                try {

                    Bitmap logoBitmap = CommonLib.getBitmap(context, R.drawable.logo, width / 2, width / 2);
                    tour_logo.getLayoutParams().width = (int) ( 0.56 * width);
                    tour_logo.getLayoutParams().height = (int) (0.56 * width);
                    tour_logo.setImageBitmap(logoBitmap);

                    Bitmap splashTextBitmap = CommonLib.getBitmap(context, R.drawable.baatna_splash_text, width / 2, width / 10);
                    tour_text_logo.getLayoutParams().width = width / 2;
                    tour_text_logo.getLayoutParams().height = width / 10;
                    tour_text_logo.setImageBitmap(splashTextBitmap);

                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                    tour_logo.setBackgroundColor(getResources().getColor(R.color.transparent1));
                }
                if( mViewPager.getCurrentItem() == 4 )
                    findViewById(R.id.signup_container).setVisibility(View.VISIBLE);

            }
            collection.addView(layout, 0);
            return layout;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((View) view);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return (view == object);
        }

        @Override
        public void finishUpdate(ViewGroup arg0) {
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(ViewGroup arg0) {
        }

    }

    private void updateDotsContainer() {

        LinearLayout dotsContainer = (LinearLayout) findViewById(R.id.tour_dots);
        dotsContainer.removeAllViews();

        int index = 5;

        for (int count = 0; count < index; count++) {
            ImageView dots = new ImageView(getApplicationContext());

            if (count == 0) {
                dots.setImageResource(R.drawable.tour_image_dots_selected);
                dots.setPadding(width / 80, 0, width / 80, 0);

            } else {
                dots.setImageResource(R.drawable.tour_image_dots_unselected);
                dots.setPadding(0, 0, width / 80, 0);
            }

            final int c = count;
            dots.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    try {
                        ((ViewPager) mViewPager).setCurrentItem(c);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            dotsContainer.addView(dots);
        }
    }

    public void navigateToHome() {
        if (prefs.getBoolean("play_service_check", true)) {
            Integer resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
            if (resultCode == ConnectionResult.SUCCESS) {
                prefs.edit().putBoolean("play_service_check", false).commit();
                if (prefs.getInt("userId", 0) != 0) {
                    registerInBackground();
                    Intent intent = new Intent(this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                }
            } else {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
                dialog.setCancelable(false);
                if (dialog != null) {
                    //This dialog will help the user update to the latest GooglePlayServices
                    dialog.show();
                }
            }
        } else {
            if (prefs.getInt("userId", 0) != 0) {
                registerInBackground();
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    int seconds = 12;
    Timer timer;
    private int mCurrentItem = 0;

    private void startTimer() {
        if (context == null || destroyed)
            return;

        timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!destroyed) {

                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!destroyed) {
                                seconds -= 1;
                                if (seconds <= 0) {
                                    seconds = 12;
                                    timer.cancel();
                                } else {
                                    mCurrentItem++;
                                    if(mCurrentItem < 5 && !hasSwipedPager)
                                        mViewPager.setCurrentItem(mCurrentItem);
                                }

                            }
                        }
                    });
                } else {
                    timer.cancel();
                }
            }
        }, 3000, 3000);
    }
}
