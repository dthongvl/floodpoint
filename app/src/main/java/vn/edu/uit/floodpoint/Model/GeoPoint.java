package vn.edu.uit.floodpoint.Model;

import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Razor on 22/12/2016.
 */

public class GeoPoint {
    String key;
    LatLng position;

    public GeoPoint(String key, LatLng position) {
        this.key = key;
        this.position = position;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }
}
