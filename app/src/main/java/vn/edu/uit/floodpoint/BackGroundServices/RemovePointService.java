package vn.edu.uit.floodpoint.BackGroundServices;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Razor on 24/12/2016.
 */
public class RemovePointService extends IntentService {
    public DatabaseReference floodPointRef = FirebaseDatabase.getInstance().getReference("floodpoint");
    private DatabaseReference geoFireRef = FirebaseDatabase.getInstance().getReference("geofire");
    final String ACTION = "RemovePoint";
    public RemovePointService(String name) {
        super(name);
    }
    public RemovePointService() {
        super("RemovePointService");
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        String key = intent.getDataString();
        Intent resultIntent = new Intent(ACTION);
        geoFireRef.child(key).removeValue();
        floodPointRef.child(key).removeValue();
        resultIntent.putExtra("key",key);
        sendBroadcast(resultIntent);
    }
}
