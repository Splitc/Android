package com.application.splitc.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.application.splitc.R;
import com.application.splitc.ZApplication;
import com.application.splitc.adapters.GooglePlaceAutocompleteAdapter;
import com.application.splitc.data.GooglePlaceAutocompleteObject;
import com.application.splitc.utils.CommonLib;
import com.application.splitc.utils.TypefaceSpan;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by apoorvarora on 10/10/16.
 */
public class NewRideActivity extends AppCompatActivity {

    private Activity mContext;
    private boolean destroyed = false;
    private SharedPreferences prefs;
    private ZApplication zapp;
    private int width, height;
    private LayoutInflater inflater;

    private GooglePlaceAutocompleteObject startLocationObject, dropLocationObject;
    private AutoCompleteTextView startLocation, dropLocation;

    private int starthour, startmin, startDay, startMonth, startyear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_ride);

        prefs = getSharedPreferences("application_settings", 0);
        zapp = (ZApplication) getApplication();
        mContext = this;
        width = getWindowManager().getDefaultDisplay().getWidth();
        height = getWindowManager().getDefaultDisplay().getHeight();
        startLocation = (AutoCompleteTextView) findViewById(R.id.start_location);
        dropLocation = (AutoCompleteTextView) findViewById(R.id.drop_location);
        startLocationObject = new GooglePlaceAutocompleteObject();
        dropLocationObject = new GooglePlaceAutocompleteObject();
        inflater = LayoutInflater.from(mContext);

        GooglePlaceAutocompleteAdapter adapter1 = new GooglePlaceAutocompleteAdapter(mContext, "cities", "AIzaSyC07KGQE5fTVM0z6G4Z_IgeR0Td0b1uCvI");//vapp.getAppConfig().getGoogleApiKey()
        startLocation.setAdapter(adapter1);
        startLocation.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (adapterView != null && adapterView.getAdapter() != null) {
                    final GooglePlaceAutocompleteObject item = (GooglePlaceAutocompleteObject) adapterView.getAdapter().getItem(position);
                    startLocationObject.setId(item.getId());
                    startLocationObject.setDisplayName(item.getDisplayName());
                    startLocation.setText(item.getDisplayName());
                }
            }
        });

        GooglePlaceAutocompleteAdapter adapter2 = new GooglePlaceAutocompleteAdapter(mContext, "cities", "AIzaSyC07KGQE5fTVM0z6G4Z_IgeR0Td0b1uCvI");//vapp.getAppConfig().getGoogleApiKey()
        dropLocation.setAdapter(adapter2);
        dropLocation.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (adapterView != null && adapterView.getAdapter() != null) {
                    final GooglePlaceAutocompleteObject item = (GooglePlaceAutocompleteObject) adapterView.getAdapter().getItem(position);
                    dropLocationObject.setId(item.getId());
                    dropLocationObject.setDisplayName(item.getDisplayName());
                    dropLocation.setText(item.getDisplayName());
                }
            }
        });



        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupActionBar();
        setListeners();


    }

    private void setListeners() {
        final EditText et= (EditText) findViewById(R.id.required_persons);
        et.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    findViewById(R.id.required_persons_hint).setVisibility(View.VISIBLE);
                    et.setHint("");
                } else{
                    if(et.getText().toString().equals("")){
                        et.setHint(R.string.time_duration);
                        findViewById(R.id.required_persons_hint).setVisibility(View.GONE);
                    }
                    if(et.getText().toString().equals("1")){
                        ((TextView)findViewById(R.id.required_persons_hint)).setText("Person");
                    }
                    else
                        ((TextView)findViewById(R.id.required_persons_hint)).setText("Persons");
                }
            }
        });

//        findViewById(R.id.pickup_timer).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showDateTimePicker(false);
//            }
//        });
    }

    @Override
    public void onDestroy(){
        destroyed = true;
        super.onDestroy();
    }

    private void setupActionBar() {
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);

        if (Build.VERSION.SDK_INT > 20)
            actionBar.setElevation(0);

        LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarCustomView = inflator.inflate(R.layout.new_ride_action_bar, null);
        actionBarCustomView.findViewById(R.id.home_icon_container).setVisibility(View.VISIBLE);
        actionBar.setCustomView(actionBarCustomView);

        SpannableString s = new SpannableString(getString(R.string.make_new_request));
        s.setSpan(
                new TypefaceSpan(getApplicationContext(), CommonLib.FONT_BOLD,
                        getResources().getColor(R.color.white), getResources().getDimension(R.dimen.size16)),
                0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        TextView title = (TextView) actionBarCustomView.findViewById(R.id.title);

        actionBarCustomView.findViewById(R.id.back_icon).setPadding(width / 20 + width / 80 + width / 100, 0, width / 20, 0);
        actionBarCustomView.findViewById(R.id.post_icon).setPadding(width / 20 + width / 80 + width / 100, 0, width / 20 + width / 80 + width / 100, 0);
        title.setText(s);
        title.setAllCaps(true);

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

    public void actionBarSelected(View v) {

        switch (v.getId()) {

            case R.id.home_icon_container:
                onBackPressed();
            default:
                break;
        }

    }

    public void postRequest(View v){
    }

    public void showDateTimePicker(final boolean showTimePicker) {
        final View customView = inflater.inflate(R.layout.date_time_picker, null);

        // Define your date pickers
        final DatePicker dpStartDate = (DatePicker) customView.findViewById(R.id.date_picker);
        final TimePicker dpStartTime = (TimePicker) customView.findViewById(R.id.time_picker);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        if(showTimePicker && startDay == 0 && startMonth == 0 && startyear == 0) {
            dpStartDate.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            dpStartTime.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
            dpStartTime.setCurrentMinute(calendar.get(Calendar.MINUTE));
            ((TextView)customView.findViewById(R.id.dp_title)).setText(getResources().getString(R.string.pickup_date));
        } else {
            if(startDay != 0 && startMonth != 0 && startyear != 0) {
                calendar.set(Calendar.DAY_OF_MONTH, startDay);
                calendar.set(Calendar.MONTH, startMonth - 1);
                calendar.set(Calendar.YEAR, startyear);
                dpStartDate.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            }
            ((TextView)customView.findViewById(R.id.dp_title)).setText(getResources().getString(R.string.pickup_date));
        }

        // Build the dialog
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(customView); // Set the view of the dialog to your custom layout
        builder.setPositiveButton(mContext.getResources().getString(R.string.submit), null);
        builder.setNegativeButton(mContext.getResources().getString(R.string.dialog_cancel), null);

        final AlertDialog dialog = builder.show();
        //Overriding the handler immediately after show is probably a better approach than OnShowListener as described below
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (customView.findViewById(R.id.dp).getVisibility() == View.VISIBLE ) {
                    int startYear = dpStartDate.getYear();
                    int startMonthOfYear = dpStartDate.getMonth() + 1;
                    int startDayOfMonth = dpStartDate.getDayOfMonth();

                    SimpleDateFormat sdf = new SimpleDateFormat("EEE dd, MMM yy", Locale.getDefault());
                    GregorianCalendar cal = new GregorianCalendar(Locale.getDefault());

                    startDay = startDayOfMonth;
                    startMonth = startMonthOfYear;
                    startyear = startYear;

                    String date = startDayOfMonth + "/" + (startMonthOfYear) + "/" + startYear;

                    //show time picker now
                    customView.findViewById(R.id.dp).setVisibility(View.GONE);
                    customView.findViewById(R.id.tp).setVisibility(View.VISIBLE);

                } else if(customView.findViewById(R.id.dp).getVisibility() == View.GONE) {
                    int startHour		=	dpStartTime.getCurrentHour();
                    int startMinute 	=	dpStartTime.getCurrentMinute();

                    starthour = startHour;
                    startmin = startMinute;

                    Time mTime = new Time();
                    mTime.set(0, startMinute, startHour, 1, 1, 1);
                    String startTime = mTime.format("%I:%M %P");
                    String date = startDay + "/" + (startMonth) + "/" + startyear;

//                    fromDate.setText(date + " "+ startTime);
//                    dialog.dismiss();
//
//                    if (type == ROUND_TRIP_FRAGMENT && toDate.getText().toString().length() < 1) {
//                        dropTimerContainer.performClick();
//                    }
                }
            }
        });

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
}
