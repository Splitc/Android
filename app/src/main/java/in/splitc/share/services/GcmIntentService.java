package in.splitc.share.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import in.splitc.share.receivers.GcmBroadcastReceiver;
import in.splitc.share.utils.CommonLib;
import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * Created by apoorvarora on 07/10/16.
 */
public class GcmIntentService extends IntentService {

    public static boolean notificationDismissed = false;

    public Context context;
    private SharedPreferences prefs;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    public static final int NOTIFICATION_ID = 1;

    public static final String NOTIFICATION_RECEIVED = "notificationReceived";
    public static final String KEY_CALLED_FROM_NOTIFICATION = "calledFromNotification";
    public static final int PUSH_NOTIFICATION_ID = 9887664;


    public GcmIntentService() {
        super("GcmIntentService");
        context = this;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Bundle extras = null;
        if(intent != null)
            extras = intent.getExtras();

        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        String messageType = gcm.getMessageType(intent);

        prefs = getSharedPreferences("application_settings", 0);

        if (extras != null && !extras.isEmpty()) {
			/*
			 * Filter messages based on message type. Since it is likely that
			 * GCM will be extended in the future with new message types, just
			 * ignore any message types you're not interested in, or that you
			 * don't recognize.
			 */

            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                CommonLib.ZLog("Send error:", extras.toString());

            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                CommonLib.ZLog("Deleted messages on server:", extras.toString());

            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                if (extras.containsKey("message")) {
//                    JSONObject messageJson;
//                    PushNotificationDto notificationContentDTO = null;
//                    try {
//                        messageJson = new JSONObject(extras.getString("message", ""));
//                        notificationContentDTO = ParserJson.parse_PushNotificationDto(messageJson);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//
//                    if (notificationContentDTO == null) {
//                        Toast.makeText(context, "invalid notification", Toast.LENGTH_LONG).show();
//                        return;
//                    }
//
//
//                    if(notificationContentDTO!=null && notificationContentDTO.getNotificationType()==null){
//                        notificationContentDTO.setNotificationType(NotificationType.NEW_MESSAGE);
//                    }
//
//                    //This broadcast will update the notification count
//                    LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
//                    Intent updateNotificationCountIntent = new Intent(NOTIFICATION_RECEIVED);
//                    manager.sendBroadcast(updateNotificationCountIntent);
//                    Intent notifIntent;
//                    if(notificationContentDTO.getNotificationMessage()!=null && notificationContentDTO.getNotificationMessage().contains("play store")) {
//                        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
//                        try {
//                            notifIntent  = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName));
//                            notifIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            // startActivity(activityIntent);
//                        } catch (android.content.ActivityNotFoundException anfe) {
//                            notifIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));
//                            notifIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            //startActivity(activityIntent);
//                        }
//
//                    } else {
//                        notifIntent = new Intent(this, NotificationsActivity.class);
//                    }
//                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
//                    stackBuilder.addParentStack(NotificationsActivity.class);
//                    stackBuilder.addNextIntent(notifIntent);
//                    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT);
//                    Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//                    Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
//                    NotificationCompat.Builder noBuilder = new NotificationCompat.Builder(this)
//                            .setSmallIcon(R.mipmap.small_ic_launcher)
//                            .setLargeIcon(largeIcon)
//                            .setColor(getApplicationContext().getResources().getColor(R.color.white))
//                            .setContentTitle(notificationContentDTO.getNotificationHeader())
//                            .setContentText(notificationContentDTO.getNewNotificationMessage())
//                            .setAutoCancel(true)
//                            .setSound(sound)
//                            .setContentIntent(resultPendingIntent);
//                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//                    notificationManager.notify(PUSH_NOTIFICATION_ID, noBuilder.build());
                }
            }
        }

        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

}