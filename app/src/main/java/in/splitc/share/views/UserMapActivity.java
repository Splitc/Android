package in.splitc.share.views;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import in.splitc.share.R;
import in.splitc.share.ZApplication;
import in.splitc.share.data.Ride;
import in.splitc.share.data.User;
import in.splitc.share.utils.CommonLib;

/**
 * Created by neo on 05/11/16.
 */
public class UserMapActivity extends AppCompatActivity {

    private ZApplication zapp;
    private SharedPreferences prefs;
    private int width;
    private LayoutInflater inflater;
    private AsyncTask mAsyncRunning;

    /** Map Object */
    private GoogleMap mMap;
    private MapView mMapView;
    private float defaultMapZoomLevel = 12.5f + 1.75f;
    // private LatLng recentLatLng;
    private ArrayList<LatLng> mapCoords;
    private final float MIN_MAP_ZOOM = 13.0f;

    private double lat;
    private double lon;

    private ArrayList<Ride> mapResultList;
    private boolean destroyed = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_map);

        width = getWindowManager().getDefaultDisplay().getWidth();

        inflater = LayoutInflater.from(this);

        try {
            MapsInitializer.initialize(this);
        } catch (Exception e) {
        }

        mMapView = (MapView) findViewById(R.id.search_map);
        mMapView.onCreate(savedInstanceState);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        prefs = getSharedPreferences("application_settings", 0);
        zapp = (ZApplication) getApplication();
        lat = zapp.lat;
        lon = zapp.lon;
        refreshView();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void setUpMapIfNeeded() {
        if (mMap == null && mMapView != null)
            mMap = mMapView.getMap();
        if (mMap != null) {

            LatLng targetCoords = null;

            if (lat != 0.0 || lon != 0.0)
                targetCoords = new LatLng(lat, lon);
            else {
                // target the current city
            }
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mMap.getUiSettings().setAllGesturesEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(false);
            mMap.getUiSettings().setTiltGesturesEnabled(false);
            mMap.getUiSettings().setCompassEnabled(false);
            mMap.setMyLocationEnabled(true);
            mMap.setBuildingsEnabled(true);

            CameraPosition cameraPosition;
            if (targetCoords != null) {
                cameraPosition = new CameraPosition.Builder().target(targetCoords) // Sets
                        // the
                        // center
                        // of
                        // the
                        // map
                        // to
                        // Mountain
                        // View
                        .zoom(defaultMapZoomLevel) // Sets the zoom
                        .build(); // Creates a CameraPosition from the builder

                try {
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                } catch (Exception e) {
                    MapsInitializer.initialize(UserMapActivity.this);
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            }

        }

    }

    private void disableMap() {
        if (mMap != null)
            mMap.getUiSettings().setAllGesturesEnabled(false);
    }

    private void enableMap() {
        if (mMap != null) {
            mMap.getUiSettings().setAllGesturesEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(false);
            mMap.getUiSettings().setTiltGesturesEnabled(false);
            mMap.getUiSettings().setCompassEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }

    }

    @Override
    public void onPause() {
        displayed = false;
        if (mMapView != null)
            mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onLowMemory() {

        try {
            if (mMapView != null) {
                mMapView.onLowMemory();
            }
            // clearMapCache();
        } catch (Exception e) {
        }

        super.onLowMemory();
    }

    @Override
    public void onDestroy() {

        if (mMapView != null)
            mMapView.onDestroy();

        destroyed = true;
        // if (zapp != null && zapp.cacheForMarkerImages != null)
        // zapp.cacheForMarkerImages.clear();
        super.onDestroy();
    }

    private void refreshMap() {

        if (lat != 0.0 && lon != 0.0) {

            if (mMap == null)
                setUpMapIfNeeded();

            // Clearing The current clustering restaurant dataset
            if (mMap != null && mapResultList != null && !mapResultList.isEmpty()) {

                for (Ride r : mapResultList) {
                    if (r.getStartLat() != 0.0 || r.getStartLon() != 0.0) {
                        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.map_icon);
                        mMap.addMarker(new MarkerOptions().position(new LatLng(r.getStartLat(), r.getStartLat())).icon(icon));
                    }
                }

            }
        }
    }

    private boolean displayed = false;

    @Override
    public void onResume() {

        super.onResume();
        displayed = true;
        if (mMapView != null) {
            mMapView.onResume();

            if (mMap == null && (lat != 0.0 || lon != 0.0))
                setUpMapIfNeeded();
        }

    }

    private void dynamicZoom(LatLngBounds bounds) throws IllegalStateException {

        if (mapResultList != null && mapResultList.size() == 1) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, width / 40 + width / 15 + width / 15));
        } else if (mMap != null)
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, width / 40 + width / 15));
    }

    private void refreshView() {
        if (mAsyncRunning != null)
            mAsyncRunning.cancel(true);
//        mAsyncRunning = new GetCategoriesList().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }
}
