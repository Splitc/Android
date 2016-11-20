package in.splitc.share.views;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import in.splitc.share.R;
import in.splitc.share.ZApplication;
import in.splitc.share.adapters.GooglePlaceAutocompleteAdapter;
import in.splitc.share.data.Address;
import in.splitc.share.data.Ride;
import in.splitc.share.utils.CommonLib;
import in.splitc.share.utils.UploadManager;
import in.splitc.share.utils.UploadManagerCallback;
import okhttp3.FormBody;

/**
 * Created by neo on 06/11/16.
 */
public class FeedItemDetailActivity extends AppCompatActivity  {

    private Activity mContext;
    private boolean destroyed = false;
    private SharedPreferences prefs;
    private ZApplication zapp;
    private int width, height;
    private LayoutInflater inflater;
    private ProgressDialog zProgressDialog;

    private Ride ride;
    private Address startLocationObject;
    private AutoCompleteTextView startLocation, dropLocation;

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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.confirm_booking));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent() != null) {
            if (getIntent().hasExtra("ride"))
                ride = (Ride) getIntent().getSerializableExtra("ride");
        }

        View yourLocationContainer = findViewById(R.id.your_location_container);

        startLocation = (AutoCompleteTextView) yourLocationContainer.findViewById(R.id.start_location);
        yourLocationContainer.findViewById(R.id.dropLoc_container).setVisibility(View.GONE);

        startLocationObject = new Address();

        GooglePlaceAutocompleteAdapter adapter1 = new GooglePlaceAutocompleteAdapter(mContext, "regions", CommonLib.GOOGLE_PLACES_API_KEY);
        startLocation.setAdapter(adapter1);
        startLocation.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (adapterView != null && adapterView.getAdapter() != null) {
                    final Address item = (Address) adapterView.getAdapter().getItem(position);
                    startLocationObject = item;
                    startLocation.setText(item.getDisplayName());
                }
            }
        });

        setListeners();
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