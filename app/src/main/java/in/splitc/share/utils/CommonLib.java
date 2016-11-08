package in.splitc.share.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Hashtable;

/**
 * Created by apoorvarora on 03/10/16.
 */
public class CommonLib {

    public final static boolean ZLogger = false;

    public static String SERVER_URL = "http://192.168.1.38:8080/SplitcServer/rest/";

    // Login animation time
    public static final int ANIMATION_LOGIN = 200;
    public static final int ANIMATION_DURATION_SIGN_IN = 300;

    /**
     * GCM Sender ID
     */
    public static final String GCM_SENDER_ID = "996855199819";

    public static final String GOOGLE_PLACES_API_KEY = "AIzaSyC07KGQE5fTVM0z6G4Z_IgeR0Td0b1uCvI";

    public static final String CLIENT_ID = "splitc_android_client";
    public static final String APP_TYPE = "splitc_android";

    public static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 201;
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 202;

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyPermissions(Activity activity, final String permission, final String[] permissions, final int requestCode) {
        // Check if we have write permission
        int activePermission = ActivityCompat.checkSelfPermission(activity, permission);

        if (activePermission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    permissions,
                    requestCode
            );
        }
    }

    // Font file def follows
    public static String FONT_MEDIUM = "fonts/transporter_Medium.ttf";
    public static String FONT_LIGHT = "fonts/transporter_Light.ttf";
    public static String FONT_REGULAR = "fonts/transporter_Regular.ttf";
    public static String FONT_BOLD = "fonts/transporter_Bold.ttf";
    public static String Icons = "fonts/splitc_Icon.ttf";
    public static String IconsZ = "fonts/zapp_Icon.ttf";

    public static final int REQUEST_CODE_START_LOCATION = 101;
    public static final int REQUEST_CODE_DROP_LOCATION = 102;
    /**
     * Preferences
     */
    public final static String APP_SETTINGS = "application_settings";
    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String PROPERTY_APP_VERSION = "appVersion";

    /** Constant to track location identification progress */
    public static final int LOCATION_NOT_ENABLED = 0;
    /** Constant to track location identification progress */
    public static final int LOCATION_NOT_DETECTED = 1;
    /** Constant to track location identification progress */
    public static final int LOCATION_DETECTED = 2;
    public static final int LOCATION_DETECTION_RUNNING = 6;

    /** Application version */
    public static final int VERSION = 1;
    public static final String VERSION_STRING = "1.0";

    // Hashtable for different type faces
    public static final Hashtable<String, Typeface> typefaces = new Hashtable<String, Typeface>();

    /**
     * Returns the bitmap associated with sampling
     */
    public static Bitmap getBitmap(Context mContext, int resId, int width, int height) throws OutOfMemoryError {
        if (mContext == null)
            return null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(mContext.getResources(), resId, options);
        options.inSampleSize = CommonLib.calculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        if (!CommonLib.isAndroidL())
            options.inPurgeable = true;

        Bitmap bitmap = null;

        bitmap = BitmapFactory.decodeResource(mContext.getResources(), resId, options);

        return bitmap;
    }

    // Calculate the sample size of bitmaps
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        int inSampleSize = 1;
        double ratioH = (double) options.outHeight / reqHeight;
        double ratioW = (double) options.outWidth / reqWidth;

        int h = (int) Math.round(ratioH);
        int w = (int) Math.round(ratioW);

        if (h > 1 || w > 1) {
            if (h > w) {
                inSampleSize = h >= 2 ? h : 2;

            } else {
                inSampleSize = w >= 2 ? w : 2;
            }
        }
        return inSampleSize;
    }

    // check done before storing the bitmap in the memory
    public static boolean shouldScaleDownBitmap(Context context, Bitmap bitmap) {
        if (context != null && bitmap != null && bitmap.getWidth() > 0 && bitmap.getHeight() > 0) {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);
            int width = metrics.widthPixels;
            int height = metrics.heightPixels;
            return ((width != 0 && width / bitmap.getWidth() < 1) || (height != 0 && height / bitmap.getHeight() < 1));
        }
        return false;
    }

    // Fetches the typeface to set for the text views
    public static Typeface getTypeface(Context c, String name) {
        synchronized (typefaces) {
            if (!typefaces.containsKey(name)) {
                try {
                    InputStream inputStream = c.getAssets().open(name);
                    File file = createFileFromInputStream(inputStream, name);
                    if (file == null) {
                        return Typeface.DEFAULT;
                    }
                    Typeface t = Typeface.createFromFile(file);
                    typefaces.put(name, t);
                } catch (Exception e) {
                    e.printStackTrace();
                    return Typeface.DEFAULT;
                }
            }
            return typefaces.get(name);
        }
    }

    // Creates the file from input stream
    private static File createFileFromInputStream(InputStream inputStream, String name) {

        try {
            File f = File.createTempFile("font", null);
            OutputStream outputStream = new FileOutputStream(f);
            byte buffer[] = new byte[1024];
            int length = 0;

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();
            return f;
        } catch (Exception e) {
            // Logging exception
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Remove the keyboard explicitly.
     */
    public static void hideKeyBoard(Activity mActivity, View mGetView) {
        try {
            ((InputMethodManager) mActivity.getSystemService(Activity.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(mGetView.getRootView().getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void hideKeyboard(Activity activity) {
        if (activity != null && activity.getWindow() != null && activity.getWindow().getDecorView() != null) {
            InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
        }
    }

    public static boolean isAndroidL() {
        return android.os.Build.VERSION.SDK_INT >= 21;
    }

    // RIVIGO Logging end points
    public static void ZLog(String Tag, String Message) {
        if (ZLogger && Message != null)
            Log.i(Tag, Message);
    }

    public static void ZLog(String Tag, float Message) {
        if (ZLogger)
            Log.i(Tag, Message + "");
    }

    public static void ZLog(String Tag, boolean Message) {
        if (ZLogger)
            Log.i(Tag, Message + "");
    }

    public static void ZLog(String Tag, int Message) {
        if (ZLogger)
            Log.i(Tag, Message + "");
    }

    // Checks if network is available
    public static boolean isNetworkAvailable(Context c) {
        ConnectivityManager connectivityManager = (ConnectivityManager) c
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static Bitmap getBitmapFromDisk(String url, Context ctx) {

        Bitmap defautBitmap = null;
        try {
            String filename = constructFileName(url);
            File filePath = new File(ctx.getCacheDir(), filename);

            if (filePath.exists() && filePath.isFile() && !filePath.isDirectory()) {
                FileInputStream fi;
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inPreferredConfig = Bitmap.Config.RGB_565;
                fi = new FileInputStream(filePath);
                defautBitmap = BitmapFactory.decodeStream(fi, null, opts);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }

        return defautBitmap;
    }

    public static String constructFileName(String url) {
        return url.replaceAll("/", "_");
    }


    public static void addBitmapToDisk(String url, Bitmap bmp, Context ctx) {
        writeBitmapToDisk(url, bmp, ctx, Bitmap.CompressFormat.PNG);
    }

    public static void writeBitmapToDisk(String url, Bitmap bmp, Context ctx, Bitmap.CompressFormat format) {
        FileOutputStream fos;
        String fileName = constructFileName(url);
        try {
            if (bmp != null) {
                fos = new FileOutputStream(new File(ctx.getCacheDir(), fileName));
                bmp.compress(format, 75, fos);
                fos.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param lat1
     * @param lng1
     * @param lat2
     * @param lng2
     * @return distance in km
     */

    public static float distFrom(double lat1, double lng1, double lat2, double lng2) {
        Log.e("lat1"+lat1+"  long1"+lng1,"lat 2"+lat2 +"  long2"+lng2);
		/*double earthRadius = 6371;
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);
		double sindLat = Math.sin(dLat / 2);
		double sindLng = Math.sin(dLng / 2);
		double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2) * Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double dist = earthRadius * c;
		Log.e("difference in distance",""+dist);
		return distance(lat1, lng1, lat2, lng2);*/
        float[] result=new float[3];
        Location.distanceBetween(lat1,lng1,lat2,lng2,result);
        //conversion to m
        result[0]=result[0]/1000;

        return result[0];

    }

    //IMEISV
    public static String getIMEI(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String imeisv = telephonyManager.getDeviceId();
        if (imeisv == null)
            imeisv = "Unknown";
        return imeisv;
    }


    public static String getTimeFormattedString(long timeInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);

        Time mTime = new Time();
        mTime.set(0, calendar.get(Calendar.MINUTE), calendar.get(Calendar.HOUR_OF_DAY), 1, 1, 1);
        return calendar.get(Calendar.DAY_OF_MONTH) + calendar.get(Calendar.MONTH) + calendar.get(Calendar.YEAR) + mTime.format("%I:%M %P");
    }

    public static byte[] Serialize_Object(Object O) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(O);
        out.close();

        // Get the bytes of the serialized object
        byte[] buf = bos.toByteArray();
        return buf;
    }

    public static Object Deserialize_Object(byte[] input, String Type) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(input));

        if (Type.equals("")) {
            Object o = in.readObject();
            in.close();
            return o;
        } else {
            in.close();
            return null;
        }

    }


}
