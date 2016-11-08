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
public class AcceptRideActivity extends AppCompatActivity implements UploadManagerCallback {

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

        UploadManager.addCallback(this);

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

        findViewById(R.id.accept).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (startLocationObject == null || startLocation.getText() == null || startLocation.getText().toString().length() < 1) {
                    Toast.makeText(mContext, "Invalid start location", Toast.LENGTH_SHORT).show();
                    return;
                }

                zProgressDialog = ProgressDialog.show(AcceptRideActivity.this, null, "Uploading your wish. Please wait!!!");

                String url = CommonLib.SERVER_URL + "ride/action?";
                FormBody.Builder requestBuilder = new FormBody.Builder();
                requestBuilder.add("access_token", prefs.getString("access_token", ""));
                requestBuilder.add("client_id", CommonLib.CLIENT_ID);
                requestBuilder.add("app_type", CommonLib.APP_TYPE);
                requestBuilder.add("action", 1 + "");
                requestBuilder.add("rideId", ride.getRideId() + "");
                requestBuilder.add("fromAddress", startLocationObject.getDisplayName());
                requestBuilder.add("startLat", startLocationObject.getLatitude() + "");
                requestBuilder.add("startLon", startLocationObject.getLongitude() + "");
                requestBuilder.add("startGooglePlaceId", startLocationObject.getPlaceId());
                requestBuilder.add("description", ((TextView)findViewById(R.id.your_description)).getText().toString());

                UploadManager.postDataToServer(UploadManager.FEED_RIDE_ACCEPT, url, requestBuilder);
            }
        });
    }

    @Override
    public void onDestroy() {
        destroyed = true;
        UploadManager.removeCallback(this);
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

    @Override
    public void uploadStarted(int requestType, Object data) {

    }

    @Override
    public void uploadFinished(int requestType, Object data, boolean status, String errorMessage) {
        if (requestType == UploadManager.FEED_RIDE_ACCEPT) {
            if(!destroyed) {
                if(zProgressDialog != null && zProgressDialog.isShowing())
                    zProgressDialog.dismiss();
                if(status) {
                    // fetch ride details and open chat if possible
                    finish();
                }
            }
        }
    }
}