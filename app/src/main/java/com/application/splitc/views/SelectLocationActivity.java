package com.application.splitc.views;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;

import com.application.splitc.R;
import com.application.splitc.ZApplication;
import com.application.splitc.adapters.GooglePlaceAutocompleteAdapter;
import com.application.splitc.data.GooglePlaceAutocompleteObject;
import com.application.splitc.utils.CommonLib;

/**
 * Created by apoorvarora on 12/10/16.
 */
public class SelectLocationActivity extends AppCompatActivity {

    private Activity mContext;
    private boolean destroyed = false;
    private SharedPreferences prefs;
    private ZApplication zapp;
    private int width, height;

    private GooglePlaceAutocompleteObject placeAutocompleteObject;
    private AutoCompleteTextView locationAutoComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_location);

        prefs = getSharedPreferences("application_settings", 0);
        zapp = (ZApplication) getApplication();
        mContext = this;
        width = getWindowManager().getDefaultDisplay().getWidth();
        height = getWindowManager().getDefaultDisplay().getHeight();

        locationAutoComplete = (AutoCompleteTextView) findViewById(R.id.location);
        placeAutocompleteObject = new GooglePlaceAutocompleteObject();
        GooglePlaceAutocompleteAdapter mAdapter = new GooglePlaceAutocompleteAdapter(mContext, "regions", CommonLib.GOOGLE_PLACES_API_KEY);
        locationAutoComplete.setAdapter(mAdapter);
        locationAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (adapterView != null && adapterView.getAdapter() != null) {
                    final GooglePlaceAutocompleteObject item = (GooglePlaceAutocompleteObject) adapterView.getAdapter().getItem(position);
                    placeAutocompleteObject.setId(item.getId());
                    placeAutocompleteObject.setDisplayName(item.getDisplayName());
                    locationAutoComplete.setText(item.getDisplayName());
                    Intent intent = new Intent();
                    intent.putExtra("location", placeAutocompleteObject);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });

    }

    @Override
    public void onDestroy(){
        destroyed = true;
        super.onDestroy();
    }
}
