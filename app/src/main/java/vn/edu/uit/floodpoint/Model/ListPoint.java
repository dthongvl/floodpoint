package vn.edu.uit.floodpoint.Model;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Razor on 20/12/2016.
 */
public class ListPoint {
    private HashMap<String, FloodPoint> listPoint;
    private ArrayList<GeoPoint> listGeo;
    private static ListPoint ourInstance = new ListPoint();
    public static ListPoint getInstance() {
        return ourInstance;
    }

    private ListPoint() {
        listPoint= new HashMap<>();
        listGeo = new ArrayList<>();
    }

    public void updatePoint(String key, FloodPoint point) {
        listPoint.put(key, point);
    }

    public void removePoint(String key) {
        listPoint.remove(key);
    }

    public void add(GeoPoint geoPoint) {
        listGeo.add(geoPoint);
    }

    public void removeGeo(String key) {
        for (GeoPoint geo: listGeo) {
            if(geo.getKey().equalsIgnoreCase(key))
                listGeo.remove(geo);
        }
    }



    public HashMap<String, FloodPoint> getAllPost() {
        return listPoint;
    }

    public ArrayList<GeoPoint> getListGeo() {
        return listGeo;
    }

    public void setListGeo(ArrayList<GeoPoint> listGeo) {
        this.listGeo = listGeo;
    }

    public GeoPoint getGeoByLocation(LatLng location){
        for (GeoPoint geo :listGeo) {
            if(geo.getPosition().equals(location)){
                return geo;
            }
        }
        return new GeoPoint(null,null);
    }
}
