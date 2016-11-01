package in.splitc.share.views;

import android.Manifest;
import android.animation.Animator;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import in.splitc.share.R;
import in.splitc.share.ZApplication;
import in.splitc.share.utils.CommonLib;
import in.splitc.share.utils.ImageLoader;
import in.splitc.share.utils.UploadManager;
import in.splitc.share.utils.UploadManagerCallback;
import in.splitc.share.utils.ZLocationCallback;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import okhttp3.FormBody;

/**
 * Created by apoorvarora on 10/10/16.
 */
public class HomeActivity extends AppCompatActivity implements UploadManagerCallback {

    private final String TAG = HomeActivity.class.getSimpleName();
    private DrawerLayout drawerLayout;
    private FrameLayout contentFrame;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private FloatingActionButton fabButton;

    private Activity mContext;
    private boolean destroyed = false;
    private SharedPreferences prefs;
    private ZApplication vapp;
    private GoogleCloudMessaging gcm;
    private int width, height;

    private final int NEW_LOAD_CODE = 121;
    private boolean mFABVisible = false;

    private ImageLoader loader;
    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        vapp = (ZApplication) getApplication();
        prefs = getSharedPreferences("application_settings", 0);
        mContext = this;
        width = getWindowManager().getDefaultDisplay().getWidth();
        height = getWindowManager().getDefaultDisplay().getHeight();
        loader = new ImageLoader(mContext, vapp);

        UploadManager.addCallback(this);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        contentFrame = (FrameLayout) findViewById(R.id.contentFrame);
        navigationView = (NavigationView) findViewById(R.id.navigationView);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        fabButton = (FloatingActionButton) findViewById(R.id.fabButton);

        setListeners();
        setupHomeFragment();
    }

    private void setListeners(){
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                scaleFAB(slideOffset);
            }

            @Override
            public void onDrawerOpened(View drawerView) {

            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        setSupportActionBar(toolbar);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                drawerLayout.closeDrawers();
                switch (menuItem.getItemId()) {
                    case R.id.nav_myloads:
                        setupHomeFragment();
                        break;
                    case R.id.nav_order_history:
                        setupMyRidesFragment();
                        break;
                }
                return true;
            }
        });

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.openDrawer, R.string.closeDrawer){
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        findViewById(R.id.logoutTextView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog logoutDialog;
                logoutDialog = new AlertDialog.Builder(mContext).setTitle(getResources().getString(R.string.logout))
                        .setMessage(getResources().getString(R.string.logout_confirm))
                        .setPositiveButton(getResources().getString(R.string.logout), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                drawerLayout.closeDrawers();

                                FormBody.Builder requestBuilder = new FormBody.Builder();
                                requestBuilder.add("access_token", prefs.getString("access_token", ""));
                                requestBuilder.add("client_id", CommonLib.CLIENT_ID);
                                requestBuilder.add("app_type", CommonLib.APP_TYPE);
                                String url = CommonLib.SERVER_URL + "auth/logout";
                                UploadManager.postDataToServer(UploadManager.LOGOUT, url, requestBuilder);

                                // clear all prefs
                                vapp.logout();

                                // let's start again
                                if (prefs.getInt("userId", 0) == 0) {
                                    Intent intent = new Intent(vapp, SplashActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        }).setNegativeButton(getResources().getString(R.string.dialog_cancel),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                        .create();
                logoutDialog.show();
            }
        });

        findViewById(R.id.about).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(HomeActivity.this, About);
            }
        });

        findViewById(R.id.feedback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, FeedbackActivity.class);
                startActivity(intent);
            }
        });

        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, NewRideActivity.class);
                startActivity(intent);
            }
        });

        View headerView = navigationView.getHeaderView(0);
        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new  Intent(mContext, UserProfileActivity.class);
                startActivity(intent);
            }
        });

        loader.setImageFromUrlOrDisk(prefs.getString("profile_pic", ""), ((ImageView)headerView.findViewById(R.id.user_image)), "", width, height, false);

        ((TextView)headerView.findViewById(R.id.textview_username)).setText("Howdy " + prefs.getString("username", "") + " !!!");
    }

    // Controls visibility/scale of FAB on drawer open/close
    private void scaleFAB(float input) {
        if (input < .7f) {

            if (fabButton.getVisibility() != View.VISIBLE)
                fabButton.setVisibility(View.VISIBLE);

            fabButton.setScaleX(1 - input);
            fabButton.setScaleY(1 - input);

        } else {

            if (fabButton.getScaleX() != 0)
                fabButton.setScaleX(0);

            if (fabButton.getScaleY() != 0)
                fabButton.setScaleY(0);

            if (fabButton.getVisibility() != View.GONE)
                fabButton.setVisibility(View.GONE);
        }

    }

    // makes FAB gone.
    public void hideFAB() {

        if (mFABVisible) {
            mFABVisible = false;

            ViewPropertyAnimator animator = fabButton.animate().scaleX(0).scaleY(0)
                    .setDuration(50).setStartDelay(0).setInterpolator(new AccelerateInterpolator());
            animator.setListener(new Animator.AnimatorListener() {

                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    fabButton.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }
            });
        }
    }

    @Override
    public void onDestroy(){
        destroyed = true;
        UploadManager.removeCallback(this);
        super.onDestroy();
    }

    private void setupHomeFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        fragment = fragmentManager.findFragmentByTag("homeFragment");

        if (fragment == null) {
            fragment = new HomeFragment();
            fragmentManager.beginTransaction()
                    .replace(R.id.contentFrame, fragment, "homeFragment")
                    .commitAllowingStateLoss();
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.drawer_menu_my_loads_string));
        }
    }

    private void setupMyRidesFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        fragment = fragmentManager.findFragmentByTag("myRidesFragment");

        if (fragment == null) {
            fragment = new MyRidesFragment();
            fragmentManager.beginTransaction()
                    .replace(R.id.contentFrame, fragment, "myRidesFragment")
                    .commitAllowingStateLoss();
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.drawer_menu_order_history_string));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CommonLib.REQUEST_CODE_START_LOCATION) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag("homeFragment");
            if (fragment != null) {
                fragment.onActivityResult(requestCode, resultCode, data);
            }
        } else if (requestCode == CommonLib.REQUEST_CODE_DROP_LOCATION) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag("homeFragment");
            if (fragment != null) {
                fragment.onActivityResult(requestCode, resultCode, data);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void uploadStarted(int requestType, Object data) {

    }

    @Override
    public void uploadFinished(int requestType, Object data, boolean status, String errorMessage) {
        if (requestType == UploadManager.LOGOUT) {
        }
    }

}
