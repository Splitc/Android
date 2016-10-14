package com.application.splitc.views;

import android.animation.Animator;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.application.splitc.R;
import com.application.splitc.ZApplication;
import com.application.splitc.utils.CommonLib;
import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * Created by apoorvarora on 10/10/16.
 */
public class HomeActivity extends AppCompatActivity {

    private final String TAG = HomeActivity.class.getSimpleName();
    private DrawerLayout drawerLayout;
    private FrameLayout contentFrame;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TextView versionNumberTextView, logoutTextView;
    private FloatingActionButton fabButton;

    private Activity mContext;
    private boolean destroyed = false;
    private SharedPreferences prefs;
    private ZApplication vapp;
    private GoogleCloudMessaging gcm;
    private int width, height;

    private final int NEW_LOAD_CODE = 121;
    private boolean mFABVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        vapp = (ZApplication) getApplication();
        prefs = getSharedPreferences("application_settings", 0);
        mContext = this;
        width = getWindowManager().getDefaultDisplay().getWidth();
        height = getWindowManager().getDefaultDisplay().getHeight();

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        contentFrame = (FrameLayout) findViewById(R.id.contentFrame);
        navigationView = (NavigationView) findViewById(R.id.navigationView);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        versionNumberTextView = (TextView) findViewById(R.id.versionNumberTextView);
        logoutTextView = (TextView) findViewById(R.id.logoutTextView);
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
//                        setOrderHistoryFragment();
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

        versionNumberTextView.setText("v 1.0.0");

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
        super.onDestroy();
    }

    private void setupHomeFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        Fragment fragment = fragmentManager.findFragmentByTag("homeFragment");

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

}
