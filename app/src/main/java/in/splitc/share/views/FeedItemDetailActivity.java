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
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import in.splitc.share.R;
import in.splitc.share.ZApplication;
import in.splitc.share.adapters.GooglePlaceAutocompleteAdapter;
import in.splitc.share.data.Address;
import in.splitc.share.data.Feed;
import in.splitc.share.utils.CommonLib;
import in.splitc.share.utils.ImageLoader;

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

    private Feed ride;
    private Address startLocationObject;
    private AutoCompleteTextView startLocation, dropLocation;
    private ImageLoader loader;

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

        if (getIntent() != null && getIntent().hasExtra("feedItem")) {
            ride = (Feed) getIntent().getSerializableExtra("feedItem");
        } else
            finish();

        setUpActionBar();

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

        View userLocationContainer = findViewById(R.id.location_container);
        ((TextView)userLocationContainer.findViewById(R.id.start_location)).setText(ride.getFromAddress());
        ((TextView)userLocationContainer.findViewById(R.id.drop_location)).setText(ride.getToAddress());
        ((TextView)findViewById(R.id.pickup_timer)).setText(CommonLib.getTimeFormattedString(ride.getCreated()));
        if (ride.getDescription() != null && ride.getDescription().length() > 0)
            ((TextView)findViewById(R.id.description)).setText(ride.getDescription());
        else
            findViewById(R.id.description).setVisibility(View.GONE);

        if (ride.getFeedType() == CommonLib.FEED_TYPE_RIDE) {
            ((TextView)findViewById(R.id.user_trip_title)).setText(getResources().getString(R.string.travel_title_string, ride.getUser().getUserName()));
        } else
            ((TextView)findViewById(R.id.user_trip_title)).setText(getResources().getString(R.string.need_ride_string, ride.getUser().getUserName()));

        if (ride.getUser().getProfilePic() != null)
            loader.setImageFromUrlOrDisk(ride.getUser().getProfilePic(), (ImageView) findViewById(R.id.user_image), "", width, height, false);
        else
            ((ImageView)findViewById(R.id.user_image)).setImageBitmap(CommonLib.getBitmap(this, R.drawable.user, width, height));


        setListeners();
    }

    private void setUpActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getResources().getString(R.string.confirm_booking));
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