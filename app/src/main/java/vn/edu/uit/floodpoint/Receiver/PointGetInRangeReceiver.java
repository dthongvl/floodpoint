package vn.edu.uit.floodpoint.Receiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import vn.edu.uit.floodpoint.Activity.MapsActivity;
import vn.edu.uit.floodpoint.Activity.SignInActivity;
import vn.edu.uit.floodpoint.R;

/**
 * Created by Razor on 28/12/2016.
 */

public class PointGetInRangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        showNotification(context);
    }
    public void showNotification(Context context){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("FloodPoint")
                        .setContentText("There is a new_point flood point near you!")
                        .setLights(Color.RED,1,1)
                        .setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
        Intent resultIntent = new Intent(context, SignInActivity.class);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int mNotificationId = sharedPreferences.getInt("totalPending",0);
// Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
// Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }
}
