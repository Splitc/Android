package in.splitc.share.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setUpActionBar();

        locationAutoComplete = (AutoCompleteTextView) findViewById(R.id.location);
        placeAutocompleteObject = new Address();
        GooglePlaceAutocompleteAdapter mAdapter = new GooglePlaceAutocompleteAdapter(mContext, "regions", CommonLib.GOOGLE_PLACES_API_KEY);
        locationAutoComplete.setAdapter(mAdapter);
        locationAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (adapterView != null && adapterView.getAdapter() != null) {
                    final Address item = (Address) adapterView.getAdapter().getItem(position);
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

        refreshView();
    }

    private void refreshView() {
        if (mAsyncRunning != null)
            mAsyncRunning.cancel(true);
        mAsyncRunning = new GetAddresses().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    private void setUpActionBar() {

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        if(Build.VERSION.SDK_INT > 20)
            actionBar.setElevation(0);

        LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarCustomView = inflator.inflate(R.layout.white_action_bar, null);
        actionBarCustomView.findViewById(R.id.home_icon_container).setVisibility(View.VISIBLE);
        actionBar.setCustomView(actionBarCustomView);

        SpannableString s = new SpannableString(getString(R.string.pick_location));
        s.setSpan(
                new TypefaceSpan(getApplicationContext(), CommonLib.FONT_BOLD,
                        getResources().getColor(R.color.white), getResources().getDimension(R.dimen.size16)),
                0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        TextView title = (TextView) actionBarCustomView.findViewById(R.id.title);

        ((RelativeLayout.LayoutParams) actionBarCustomView.findViewById(R.id.back_icon).getLayoutParams())
                .setMargins(width / 40, 0, 0, 0);
        actionBarCustomView.findViewById(R.id.title).setPadding(width / 20, 0, width / 40, 0);
        title.setText(s);
        title.setAllCaps(true);
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

    private class GetAddresses extends AsyncTask<Object, Void, Object> {

        // execute the api
        @Override
        protected Object doInBackground(Object... params) {
            try {
                ArrayList<Address> list = RecentAddressDBWrapper.getAddresses(prefs.getInt("uid", 0));
                CommonLib.ZLog("API RESPONSER", "CALLING GET WRAPPER");
                Object info = null;
                int len = 0;
                if(len > 0){
                    return list;
                }
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
                findViewById(R.id.content).setVisibility(View.VISIBLE);
                if (result instanceof ArrayList<?>) {
                    rides = (ArrayList<Address>) result;
                    mAdapter = new RecentAddressesAdapter(rides, SelectLocationActivity.this);
                    recyclerView.setAdapter(mAdapter);
                }
            }
        }
    }
}