package in.splitc.share.views;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import in.splitc.share.R;
import in.splitc.share.ZApplication;
import in.splitc.share.adapters.GooglePlaceAutocompleteAdapter;
import in.splitc.share.data.Address;
import in.splitc.share.utils.CommonLib;
import in.splitc.share.utils.TypefaceSpan;
import in.splitc.share.utils.UploadManager;
import in.splitc.share.utils.UploadManagerCallback;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import okhttp3.FormBody;

/**
 * Created by apoorvarora on 10/10/16.
 */
public class NewRideActivity extends AppCompatActivity implements UploadManagerCallback {

    private Activity mContext;
    private boolean destroyed = false;
    private SharedPreferences prefs;
    private ZApplication zapp;
    private int width, height;
    private LayoutInflater inflater;

    private Address startLocationObject, dropLocationObject;
    private AutoCompleteTextView startLocation, dropLocation;

    private int starthour, startmin, startDay, startMonth, startyear;

    private Calendar tripStartdate;

    private ProgressDialog zProgressDialog;

    private int checkedId = R.id.need_ride;

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
        startLocationObject = new Address();
        dropLocationObject = new Address();
        inflater = LayoutInflater.from(mContext);

        UploadManager.addCallback(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupActionBar();
        setListeners();
    }

    private void setListeners() {
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

        GooglePlaceAutocompleteAdapter adapter2 = new GooglePlaceAutocompleteAdapter(mContext, "regions", CommonLib.GOOGLE_PLACES_API_KEY);
        dropLocation.setAdapter(adapter2);
        dropLocation.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (adapterView != null && adapterView.getAdapter() != null) {
                    final Address item = (Address) adapterView.getAdapter().getItem(position);
                    dropLocationObject = item;
                    dropLocation.setText(item.getDisplayName());
                }
            }
        });

        ((EditText) findViewById(R.id.required_persons)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String value = editable.toString();
                int persons = 0;
                try {
                    persons = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                if (persons > 1) {
                    ((TextView)findViewById(R.id.required_persons_hint)).setText(getResources().getString(R.string.persons));
                } else
                    ((TextView)findViewById(R.id.required_persons_hint)).setText(getResources().getString(R.string.person));
            }
        });

        ((RadioGroup) findViewById(R.id.group)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                checkedId = i;
                switch (i) {
                    case R.id.need_ride:
                        ((TextView)findViewById(R.id.persons_label)).setText(getResources().getString(R.string.for_str));
                        break;
                    case R.id.driving:
                        ((TextView)findViewById(R.id.persons_label)).setText(getResources().getString(R.string.need));
                        break;
                }
            }
        });

        findViewById(R.id.pickup_timer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDateTimePicker();
            }
        });
    }

    @Override
    public void onDestroy(){
        destroyed = true;
        if (zProgressDialog != null && zProgressDialog.isShowing())
            zProgressDialog.dismiss();
        UploadManager.removeCallback(this);
        super.onDestroy();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
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

    @Override
    public void onBackPressed() {
        boolean showGoBack = true;
        if (((TextView)findViewById(R.id.required_persons)).getText().toString().length() > 0
                || ((TextView)findViewById(R.id.pickup_timer)).getText().toString().length() > 0
                || startLocation.length() > 0
                || dropLocation.length() > 0
                || ((TextView)findViewById(R.id.description_et)).getText().toString().length() > 0
                )
            showGoBack = false;

        if (!showGoBack) {
            final AlertDialog backPressedDialog;
            backPressedDialog = new AlertDialog.Builder(mContext).setTitle(getResources().getString(R.string.unfilled_details))
                    .setMessage(getResources().getString(R.string.go_back))
                    .setPositiveButton(getResources().getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                CommonLib.hideKeyboard(NewRideActivity.this);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            finish();
                        }
                    }).setNegativeButton(getResources().getString(R.string.dialog_cancel),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                    .create();
            backPressedDialog.show();
        }

        try {
            CommonLib.hideKeyboard(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onBackPressed();
    }

    public void postRequest(View v) {
        if (startLocationObject == null || startLocation.getText() == null || startLocation.getText().toString().length() < 1) {
            Toast.makeText(mContext, "Invalid start location", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dropLocationObject == null || dropLocation.getText() == null || dropLocation.getText().toString().length() < 1) {
            Toast.makeText(mContext, "Invalid drop location", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tripStartdate == null) {
            Toast.makeText(mContext, "Pickup time", Toast.LENGTH_SHORT).show();
            return;
        }

        FormBody.Builder requestBuilder = new FormBody.Builder();
        requestBuilder.add("access_token", prefs.getString("access_token", ""));
        requestBuilder.add("client_id", CommonLib.CLIENT_ID);
        requestBuilder.add("app_type", CommonLib.APP_TYPE);
        requestBuilder.add("fromAddress", startLocation.getText().toString());
        requestBuilder.add("startGooglePlaceId", startLocationObject.getPlaceId());
        requestBuilder.add("toAddress", dropLocation.getText().toString());
        requestBuilder.add("dropGooglePlaceId", dropLocationObject.getPlaceId());
        requestBuilder.add("persons", ((TextView)findViewById(R.id.required_persons)).getText().toString());
        requestBuilder.add("description", ((TextView)findViewById(R.id.description_et)).getText().toString());
        requestBuilder.add("startTime", tripStartdate.getTimeInMillis()+"");

        StringBuilder url = new StringBuilder();
        url.append(CommonLib.SERVER_URL);
        if (checkedId == R.id.need_ride)
            url.append("rideRequest/add");
        else
            url.append("ride/add");
        UploadManager.postDataToServer(UploadManager.NEW_RIDE, url.toString(), requestBuilder);

        zProgressDialog = ProgressDialog.show(mContext, null, "Uploading your wish. Please wait!!!");
    }

    public void showDateTimePicker() {
        final View customView = inflater.inflate(R.layout.date_time_picker, null);

        // Define your date pickers
        final DatePicker dpStartDate = (DatePicker) customView.findViewById(R.id.date_picker);
        final TimePicker dpStartTime = (TimePicker) customView.findViewById(R.id.time_picker);

        tripStartdate = Calendar.getInstance();
        tripStartdate.setTimeInMillis(System.currentTimeMillis());
        tripStartdate.setTimeInMillis(System.currentTimeMillis());

        if(startDay != 0 && startMonth != 0 && startyear != 0) {
            tripStartdate.set(Calendar.DAY_OF_MONTH, startDay);
            tripStartdate.set(Calendar.MONTH, startMonth - 1);
            tripStartdate.set(Calendar.YEAR, startyear);
            dpStartDate.updateDate(tripStartdate.get(Calendar.YEAR), tripStartdate.get(Calendar.MONTH), tripStartdate.get(Calendar.DAY_OF_MONTH));
        }
        ((TextView)customView.findViewById(R.id.dp_title)).setText(getResources().getString(R.string.pickup_date));

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

                    startDay = startDayOfMonth;
                    startMonth = startMonthOfYear;
                    startyear = startYear;

                    tripStartdate.set(Calendar.DAY_OF_MONTH, startDay);
                    tripStartdate.set(Calendar.MONTH, startMonth);
                    tripStartdate.set(Calendar.YEAR, startyear);

                    //show time picker now
                    customView.findViewById(R.id.dp).setVisibility(View.GONE);
                    customView.findViewById(R.id.tp).setVisibility(View.VISIBLE);

                } else if(customView.findViewById(R.id.dp).getVisibility() == View.GONE) {
                    int startHour		=	dpStartTime.getCurrentHour();
                    int startMinute 	=	dpStartTime.getCurrentMinute();

                    starthour = startHour;
                    startmin = startMinute;

                    tripStartdate.set(Calendar.HOUR_OF_DAY, starthour);
                    tripStartdate.set(Calendar.MINUTE, startmin);

                    Time mTime = new Time();
                    mTime.set(0, startMinute, startHour, 1, 1, 1);
                    String startTime = mTime.format("%I:%M %P");
                    String date = startDay + "/" + (startMonth) + "/" + startyear;

                    ((TextView)findViewById(R.id.pickup_timer)).setText(date + " "+ startTime);
                    dialog.dismiss();
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

    @Override
    public void uploadStarted(int requestType, Object data) {

    }

    @Override
    public void uploadFinished(int requestType, Object data, boolean status, String errorMessage) {
        if(requestType == UploadManager.NEW_RIDE) {
            if(!destroyed) {
                if (zProgressDialog != null && zProgressDialog.isShowing())
                    zProgressDialog.dismiss();
                if (status) {
                    finish();
                }
            }
        }
    }
}
