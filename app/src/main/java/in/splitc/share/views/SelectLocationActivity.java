package in.splitc.share.views;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import in.splitc.share.R;
import in.splitc.share.ZApplication;
import in.splitc.share.adapters.GooglePlaceAutocompleteAdapter;
import in.splitc.share.adapters.MyRidesAdapter;
import in.splitc.share.adapters.RecentAddressesAdapter;
import in.splitc.share.data.Address;
import in.splitc.share.data.Ride;
import in.splitc.share.db.RecentAddressDBWrapper;
import in.splitc.share.utils.CommonLib;
import in.splitc.share.utils.RandomCallback;
import in.splitc.share.utils.TypefaceSpan;

/**
 * Created by apoorvarora on 12/10/16.
 */
public class SelectLocationActivity extends AppCompatActivity {

    private Activity mContext;
    private boolean destroyed = false;
    private SharedPreferences prefs;
    private ZApplication zapp;
    private int width, height;

    private Address placeAutocompleteObject;
    private AutoCompleteTextView locationAutoComplete;

    private RecyclerView recyclerView;
    private RecentAddressesAdapter mAdapter;
    ArrayList<Address> rides = new ArrayList<Address>();

    RandomCallback callback;

    private AsyncTask mAsyncRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_location);

        prefs = getSharedPreferences("application_settings", 0);
        zapp = (ZApplication) getApplication();
        mContext = this;
        width = getWindowManager().getDefaultDisplay().getWidth();
        height = getWindowManager().getDefaultDisplay().getHeight();

        setUpActionBar();

        callback = new RandomCallback() {
            @Override
            public void randomMethod(Object[] data) {
                if(data != null && data.length > 0) {
                    final Address item = (Address) data[0];

                    new AddAddress().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, new Object[]{item});

                    placeAutocompleteObject.setPlaceId(item.getPlaceId());
                    placeAutocompleteObject.setDisplayName(item.getDisplayName());
                    locationAutoComplete.setText(item.getDisplayName());
                    Intent intent = new Intent();
                    intent.putExtra("location", placeAutocompleteObject);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        };

        locationAutoComplete = (AutoCompleteTextView) findViewById(R.id.location);
        placeAutocompleteObject = new Address();
        GooglePlaceAutocompleteAdapter mAdapter = new GooglePlaceAutocompleteAdapter(mContext, "regions", CommonLib.GOOGLE_PLACES_API_KEY);
        locationAutoComplete.setAdapter(mAdapter);
        locationAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (adapterView != null && adapterView.getAdapter() != null) {

                    final Address item = (Address) adapterView.getAdapter().getItem(position);

                    new AddAddress().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, new Object[]{item});

                    placeAutocompleteObject.setPlaceId(item.getPlaceId());
                    placeAutocompleteObject.setDisplayName(item.getDisplayName());
                    locationAutoComplete.setText(item.getDisplayName());
                    Intent intent = new Intent();
                    intent.putExtra("location", placeAutocompleteObject);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(SelectLocationActivity.this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                CommonLib.verifyPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, CommonLib.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            } else {
                CommonLib.verifyPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, CommonLib.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        } else {
            refreshView();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CommonLib.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    refreshView();
                } else {
                    CommonLib.verifyPermissions(SelectLocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, CommonLib.MY_PERMISSIONS_REQUEST_FINE_LOCATION);
                }
                return;
            }
        }
    }

    private void refreshView() {
        if (mAsyncRunning != null)
            mAsyncRunning.cancel(true);
        mAsyncRunning = new GetAddresses().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    private void setUpActionBar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getResources().getString(R.string.pick_location));
    }

    @Override
    public void onDestroy(){
        destroyed = true;
        super.onDestroy();
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

    @Override
    public void onBackPressed() {
        try {
            CommonLib.hideKeyboard(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onBackPressed();
    }


    private class GetAddresses extends AsyncTask<Object, Void, Object> {

        // execute the api
        @Override
        protected Object doInBackground(Object... params) {
            try {
                ArrayList<Address> list = RecentAddressDBWrapper.getAddresses(prefs.getInt("uid", 0));
                CommonLib.ZLog("API RESPONSER", "CALLING GET WRAPPER");
                return list;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            if (destroyed)
                return;

            if (result != null) {
                if (result instanceof ArrayList<?>) {
                    rides = (ArrayList<Address>) result;
                    mAdapter = new RecentAddressesAdapter(rides, SelectLocationActivity.this, callback);
                    recyclerView.setAdapter(mAdapter);
                }
            }
        }
    }

    private class AddAddress extends AsyncTask<Object, Void, Object> {
        // execute the api
        @Override
        protected Object doInBackground(Object... params) {
            RecentAddressDBWrapper.addAddress((Address) params[0], prefs.getInt("uid", 0), System.currentTimeMillis());
            return null;
        }
    }
}