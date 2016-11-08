package in.splitc.share.views;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import in.splitc.share.R;
import in.splitc.share.ZApplication;
import in.splitc.share.adapters.FeedAdapter;
import in.splitc.share.data.Ride;
import in.splitc.share.utils.CommonLib;
import in.splitc.share.utils.OnLoadMoreListener;
import in.splitc.share.utils.RandomCallback;
import in.splitc.share.utils.UploadManager;
import in.splitc.share.utils.UploadManagerCallback;
import in.splitc.share.utils.ZLocationCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by apoorvarora on 12/10/16.
 */
public class HomeFragment extends Fragment implements ZLocationCallback, UploadManagerCallback, RandomCallback {

    public static final  String TAG = HomeFragment.class.getSimpleName();
    private View mView;
    ZApplication zapp;
    private int width, height;
    private SharedPreferences prefs;
    private Activity activity;
    private boolean destroyed = false;
    private View getView;

    private RecyclerView recyclerView;
    private FeedAdapter mAdapter;
    List<Ride> rides = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private int mTotalRides = 0;
    private int start = 0;
    private int count = 10;

    private in.splitc.share.data.Address startAddress, dropAddress;

    private ProgressDialog zProgressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_home, container, false);
        return mView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        activity = getActivity();
        getView = getView();
        prefs = activity.getSharedPreferences("application_settings", 0);
        zapp = (ZApplication) getActivity().getApplication();
        width = getActivity().getWindowManager().getDefaultDisplay().getWidth();
        height = getActivity().getWindowManager().getDefaultDisplay().getHeight();
        destroyed = false;
        mSwipeRefreshLayout = (SwipeRefreshLayout) getView.findViewById(R.id.swiperefresh);

        UploadManager.addCallback(this);

        recyclerView = (RecyclerView) getView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        refreshView();
        setListeners();

        zapp.zll.addCallback(this);
        zapp.zll.forced = true;
        // check permission
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                CommonLib.verifyPermissions(activity, Manifest.permission.ACCESS_FINE_LOCATION, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, CommonLib.MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            } else {
                CommonLib.verifyPermissions(activity, Manifest.permission.ACCESS_FINE_LOCATION, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, CommonLib.MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            }
        } else {
            zapp.startLocationCheck();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CommonLib.MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    zapp.startLocationCheck();
                } else {
                    CommonLib.verifyPermissions(activity, Manifest.permission.ACCESS_FINE_LOCATION, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, CommonLib.MY_PERMISSIONS_REQUEST_FINE_LOCATION);
                }
                return;
            }
        }
    }

    private void setListeners(){
        getView.findViewById(R.id.start_location_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, SelectLocationActivity.class);
                activity.startActivityForResult(intent, CommonLib.REQUEST_CODE_START_LOCATION);
            }
        });
        getView.findViewById(R.id.dropLoc_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, SelectLocationActivity.class);
                activity.startActivityForResult(intent, CommonLib.REQUEST_CODE_DROP_LOCATION);
            }
        });

        mAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {

                start = rides.size();

                rides.add(null);
                mAdapter.notifyItemInserted(rides.size() - 1);

                String url = CommonLib.SERVER_URL + "ride/feed?start=" + start + "&count=" + count;
                FormBody.Builder requestBuilder = new FormBody.Builder();
                requestBuilder.add("access_token", prefs.getString("access_token", ""));
                requestBuilder.add("client_id", CommonLib.CLIENT_ID);
                requestBuilder.add("app_type", CommonLib.APP_TYPE);
                UploadManager.postDataToServer(UploadManager.FEED_RIDES_LOAD_MORE, url, requestBuilder);
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refreshView();
                    }
                }
        );
    }

    private void refreshView() {
        rides = new ArrayList<Ride>();
        mAdapter = new FeedAdapter(rides, recyclerView, startAddress, activity, this, zapp, width, height);
        recyclerView.setAdapter(mAdapter);

        String url = CommonLib.SERVER_URL + "ride/feed?start=" + 0 + "&count=" + count;
        FormBody.Builder requestBuilder = new FormBody.Builder();
        requestBuilder.add("access_token", prefs.getString("access_token", ""));
        requestBuilder.add("client_id", CommonLib.CLIENT_ID);
        requestBuilder.add("app_type", CommonLib.APP_TYPE);
        UploadManager.postDataToServer(UploadManager.FEED_RIDES, url, requestBuilder);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CommonLib.REQUEST_CODE_START_LOCATION && resultCode == Activity.RESULT_OK) {
            if( data != null && data.hasExtra("location") ) {
                in.splitc.share.data.Address location = (in.splitc.share.data.Address) data.getSerializableExtra("location");
                ((TextView)getView.findViewById(R.id.start_location)).setText(location.getDisplayName());
                refreshView();
            }
        } else if (requestCode == CommonLib.REQUEST_CODE_DROP_LOCATION) {
            if( data != null && data.hasExtra("location") ) {
                in.splitc.share.data.Address location = (in.splitc.share.data.Address) data.getSerializableExtra("location");
                ((TextView)getView.findViewById(R.id.drop_location)).setText(location.getDisplayName());
                refreshView();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCoordinatesIdentified(Location loc) {
        if(loc != null) {
            float lat = (float) loc.getLatitude();
            float lon = (float) loc.getLongitude();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("lat1", lat + "");
            editor.putString("lon1", lon + "");
            editor.commit();

//            UploadManager.updateLocation(lat, lon);

            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(activity, Locale.getDefault());

            try {
                addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                if (addresses != null && addresses.size() > 0 && addresses.get(0) != null) {
                    String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    String city = addresses.get(0).getLocality();
                    String state = addresses.get(0).getAdminArea();
                    String country = addresses.get(0).getCountryName();
                    address = address + ", " + city + ", " + state + ", " + country;
                    zapp.setLocationString(city);
                    zapp.setAddressString(address);
                } else {
                    new GetLocationInfo().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Object[]{loc.getLatitude(), loc.getLongitude()});
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!destroyed)
                ((TextView)getView.findViewById(R.id.start_location)).setText(zapp.getAddressString());
        }
    }

    @Override
    public void onLocationIdentified() {

    }

    @Override
    public void onLocationNotIdentified() {

    }

    @Override
    public void onDifferentCityIdentified() {

    }

    @Override
    public void locationNotEnabled() {

    }

    @Override
    public void onLocationTimedOut() {

    }

    @Override
    public void onNetworkError() {

    }

    @Override
    public void uploadStarted(int requestType, Object data) {

    }

    @Override
    public void uploadFinished(int requestType, Object data, boolean status, String errorMessage) {
        if (requestType == UploadManager.FEED_RIDES) {
            if(!destroyed && status && data instanceof Object[] && ((Object[]) data).length == 3) {
                Object[] output = (Object[]) ((Object[]) data)[0];

                mTotalRides = (int) output[0];
                rides.addAll((ArrayList<Ride>) output[1]);
                mAdapter.notifyDataSetChanged();
                if (mTotalRides <= rides.size()) {
                    mAdapter.setLoaded();
                }

                mSwipeRefreshLayout.setRefreshing(false);
            }
        } else if (requestType == UploadManager.FEED_RIDES_LOAD_MORE) {
            if(!destroyed && status) {
                Object[] output = (Object[]) ((Object[]) data)[0];
                rides.remove(rides.size() - 1);
                mAdapter.notifyItemRemoved(rides.size());

                mTotalRides = (int) output[0];
                rides.addAll((ArrayList<Ride>) output[1]);
                mAdapter.notifyDataSetChanged();
                if (mTotalRides <= rides.size()) {
                    mAdapter.setLoaded();
                }
            }
        } else if (requestType == UploadManager.FEED_RIDE_ACCEPT) {
            if(!destroyed && status) {
                // fetch ride details and open chat if possible
            }
        }
    }

    @Override
    public void randomMethod(Object[] data) {
        if(data instanceof Object[]) {

            Intent intent = new Intent(activity, AcceptRideActivity.class);
            intent.putExtra("ride", (Ride) data[0]);
            intent.putExtra("startAddress", (in.splitc.share.data.Address) data[1]);
            activity.startActivity(intent);

        }
    }

    private class GetLocationInfo extends AsyncTask<Object, Void, JSONObject> {

        private double lat;
        private double lon;

        // execute the api
        @Override
        protected JSONObject doInBackground(Object... params) {
            lat = (Double) params[0];
            lon = (Double) params[1];

            String url = "http://maps.google.com/maps/api/geocode/json?latlng="+lat+","+lon+"&sensor=true";
            Request
                    request = new Request.Builder()
                    .addHeader("Content-Type", "application/json")
                    .url(url)
                    .get()
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            Call call = okHttpClient.newCall(request);

            try {
                Response response = call.execute();
                return new JSONObject(response.body().string());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new JSONObject();
        }

        @Override
        protected void onPostExecute(JSONObject ret) {
            if (destroyed)
                return;

            JSONObject location;
            String location_string;
            try {
                location = ret.getJSONArray("results").getJSONObject(0);
                location_string = location.getString("formatted_address");
                zapp.setAddressString(location_string);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        zapp.zll.removeCallback(this);
        UploadManager.removeCallback(this);
        if (zProgressDialog != null && zProgressDialog.isShowing())
            zProgressDialog.dismiss();

        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        destroyed = true;
        if (zProgressDialog != null && zProgressDialog.isShowing())
            zProgressDialog.dismiss();

        super.onDestroyView();
    }

}
