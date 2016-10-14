package com.application.splitc.views;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.application.splitc.R;
import com.application.splitc.ZApplication;
import com.application.splitc.data.GooglePlaceAutocompleteObject;
import com.application.splitc.utils.CommonLib;

import org.w3c.dom.Text;

/**
 * Created by apoorvarora on 12/10/16.
 */
public class HomeFragment extends Fragment {

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

}
