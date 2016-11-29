package in.splitc.share.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.facebook.LoginActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import in.splitc.share.R;
import in.splitc.share.ZApplication;
import in.splitc.share.data.Message;
import in.splitc.share.utils.CommonLib;

/**
 * Created by apoorvarora on 07/10/16.
 */
public class FirebaseMessageService extends FirebaseMessagingService {

    private static final String TAG = FirebaseMessageService.class.getName();
    private ZApplication vapp;
    private SharedPreferences prefs;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage != null) {
            // random notification
            if (remoteMessage.getNotification() != null && remoteMessage.getNotification().getBody() != null)
                sendNotification(remoteMessage.getNotification().getBody());
            else if (remoteMessage.getData() != null) { // chat notification
//                Gson gson = new Gson();
//                try {
//                    MessageDetails message = gson.fromJson(gson.toJson(ParserJson.parse_FriendlyMessage(remoteMessage.getData())), MessageDetails.class);
//                    sendChatNotification(message);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        vapp = (ZApplication) getApplication();
        prefs = getSharedPreferences("application_settings", 0);
    }

    @Override
    public void onSendError(String s, Exception e) {
        super.onSendError(s, e);
    }

    @Override
    public void onMessageSent(String s) {
        super.onMessageSent(s);
        CommonLib.ZLog(TAG, "Message sent: " + s);
    }

    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Firebase Push Notification")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }

    /***
     * This method checks if the app is open for the particular chat,
     * if it is not then show notification,
     * else locally broadcast the message
     */
    private void sendChatNotification(Message message) {

        // always broadcast the message so appropriate receiver can listen to it
//        Intent smsIntent = new Intent(CommonLib.LOCAL_CHAT_BROADCAST);
//        smsIntent.putExtra("message", message);
//        LocalBroadcastManager.getInstance(this).sendBroadcast(smsIntent);
//
//        // If the case is not handled, then show the notification
//        Bundle bundle = new Bundle();
//        bundle.putSerializable("message", message);
//
//        in.cloudtech.vyom.utils.NotificationManager.getInstance(this).sendNotification(bundle);
    }
}