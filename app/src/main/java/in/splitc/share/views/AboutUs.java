package in.splitc.share.views;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import in.splitc.share.R;
import in.splitc.share.utils.CommonLib;
import in.splitc.share.utils.TypefaceSpan;
import in.splitc.share.utils.ZWebView;

/**
 * Created by neo on 06/11/16.
 */
public class AboutUs extends AppCompatActivity {

    private int width;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aboutus);
        width = getWindowManager().getDefaultDisplay().getWidth();

        setUpActionBar();

        fixsizes();
        setListeners();

        ImageView img = (ImageView) findViewById(R.id.zomato_logo);
        img.getLayoutParams().width = width / 3;
        img.getLayoutParams().height = width / 3;
        // setting image
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher,
                    options);
            options.inSampleSize = CommonLib.calculateInSampleSize(options,
                    width, width);
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                    R.mipmap.ic_launcher, options);

            img.setImageBitmap(bitmap);

        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            img.setBackgroundColor(getResources().getColor(R.color.black));
        } catch (Exception e) {
            e.printStackTrace();
            img.setBackgroundColor(getResources().getColor(R.color.black));
        }
    }

    private void setUpActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getResources().getString(R.string.about_us));
    }

    void fixsizes() {

        width = getWindowManager().getDefaultDisplay().getWidth();

        // About us main page layouts
        // findViewById(R.id.home_logo).getLayoutParams().height = 3 * width /
        // 10;
        // findViewById(R.id.home_logo).getLayoutParams().width = 3 * width /
        // 10;
        findViewById(R.id.home_version).setPadding(width / 20, 0, 0, 0);
        findViewById(R.id.home_logo_container).setPadding(width / 20,
                width / 20, width / 20, width / 20);
        findViewById(R.id.about_us_body).setPadding(width / 20, 0, width / 20,
                width / 20);

        RelativeLayout.LayoutParams relativeParams2 = new RelativeLayout.LayoutParams(
                width, 9 * width / 80);
        relativeParams2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        findViewById(R.id.about_us_privacy_policy_container).setLayoutParams(
                relativeParams2);
        ((TextView) ((LinearLayout) findViewById(R.id.about_us_privacy_policy_container))
                .getChildAt(0)).setPadding(width / 20, 0, 0, 0);
        findViewById(R.id.about_us_privacy_policy).setPadding(width / 40, 0,
                width / 20, 0);

        RelativeLayout.LayoutParams relativeParams3 = new RelativeLayout.LayoutParams(
                width, 9 * width / 80);
        relativeParams3.addRule(RelativeLayout.ABOVE, R.id.separator3);
        findViewById(R.id.about_us_terms_conditions_container).setLayoutParams(
                relativeParams3);
        ((TextView) ((LinearLayout) findViewById(R.id.about_us_terms_conditions_container))
                .getChildAt(0)).setPadding(width / 20, 0, 0, 0);
        findViewById(R.id.about_us_terms_conditions).setPadding(width / 40, 0,
                width / 20, 0);
    }

    public void setListeners() {

        LinearLayout btnTermsAndConditons = (LinearLayout) findViewById(R.id.about_us_terms_conditions_container);
        btnTermsAndConditons.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent intent = new Intent(AboutUs.this, ZWebView.class);
                intent.putExtra("title",
                        getResources()
                                .getString(R.string.about_us_terms_of_use));
                intent.putExtra("url",
                        "https://www.splitc.in/terms/");
                startActivity(intent);
            }
        });

        LinearLayout btnPrivacyPolicy = (LinearLayout) findViewById(R.id.about_us_privacy_policy_container);
        btnPrivacyPolicy.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent intent = new Intent(AboutUs.this, ZWebView.class);
                intent.putExtra(
                        "title",
                        getResources().getString(
                                R.string.about_us_privacypolicy));
                intent.putExtra("url",
                        "https://www.splitc.in/privacy/");
                startActivity(intent);

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void actionBarSelected(View v) {

        switch (v.getId()) {

            case R.id.home_icon_container:
                onBackPressed();

            default:
                break;
        }

    }
}