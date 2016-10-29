package com.application.splitc.views;

import android.Manifest;
import android.app.Activity;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.application.splitc.R;
import com.application.splitc.ZApplication;
import com.application.splitc.data.GooglePlaceAutocompleteObject;
import com.application.splitc.utils.CommonLib;
import com.application.splitc.utils.ParserJson;
import com.application.splitc.utils.UploadManager;
import com.application.splitc.utils.ZLocationCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by apoorvarora on 12/10/16.
 */
public class HomeFragment extends Fragment implements ZLocationCallback {

    public static final  String TAG = HomeFragment.class.getSimpleName();
    private View mView;
    ZApplication zapp;
    private int width, height;
    private SharedPreferences prefs;
    private Activity activity;
    private boolean destroyed = false;
    private View getView;

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
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CommonLib.REQUEST_CODE_START_LOCATION && resultCode == Activity.RESULT_OK) {
            if( data != null && data.hasExtra("location") ) {
                GooglePlaceAutocompleteObject location = (GooglePlaceAutocompleteObject) data.getSerializableExtra("location");
                ((TextView)getView.findViewById(R.id.start_location)).setText(location.getDisplayName());
            }
        } else if (requestCode == CommonLib.REQUEST_CODE_DROP_LOCATION) {
            if( data != null && data.hasExtra("location") ) {
                GooglePlaceAutocompleteObject location = (GooglePlaceAutocompleteObject) data.getSerializableExtra("location");
                ((TextView)getView.findViewById(R.id.drop_location)).setText(location.getDisplayName());
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
        super.onDestroy();
    }
}
