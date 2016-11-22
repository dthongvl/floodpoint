package vn.edu.uit.floodpoint.Activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

import vn.edu.uit.floodpoint.Model.FloodPoint;
import vn.edu.uit.floodpoint.R;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DatabaseReference geoFireRef = FirebaseDatabase.getInstance().getReference("geofire");
    private DatabaseReference floodPointRef = FirebaseDatabase.getInstance().getReference("floodpoint");
    private GeoFire geoFire = new GeoFire(geoFireRef);
    StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://floodpoint-5840b.appspot.com");
    private GeoQuery geoQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }


    public void addNewPoint(final FloodPoint floodPoint, String imagePath) {
        final String key = floodPointRef.push().getKey();
        Uri file = Uri.fromFile(new File(imagePath));
        StorageReference postImageRef = storageRef.child("floodpoint/image/" + key + "/" + file.getLastPathSegment());
        UploadTask uploadTask = postImageRef.putFile(file);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                try {
                    floodPoint.setImageUrl(taskSnapshot.getDownloadUrl().toString());
                    floodPointRef.child(key).setValue(floodPoint);
                    geoFire.setLocation(key, new GeoLocation(floodPoint.getLatitude(), floodPoint.getLongitude()));
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void searchNearby(double latitude, double longitude, double distance) {
        geoQuery = geoFire.queryAtLocation(new GeoLocation(latitude, longitude), distance);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                System.out.println(String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));
            }

            @Override
            public void onKeyExited(String key) {
                System.out.println(String.format("Key %s is no longer in the search area", key));
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                System.out.println(String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));
            }

            @Override
            public void onGeoQueryReady() {
                System.out.println("All initial data has been loaded and events have been fired!");
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                System.err.println("There was an error with this query: " + error);
            }
        });
    }

}
