package vn.edu.uit.floodpoint.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import vn.edu.uit.floodpoint.BackGroundServices.RemovePointService;

/**
 * Created by Razor on 24/12/2016.
 */

public class AlarmReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent removeIntent = new Intent(context, RemovePointService.class);
        removeIntent.setData(Uri.parse(intent.getStringExtra("key")));
        context.startService(removeIntent);
    }
}
