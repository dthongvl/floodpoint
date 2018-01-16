package vn.edu.uit.floodpoint.Activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.warkiz.widget.IndicatorSeekBar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import vn.edu.uit.floodpoint.BackGroundServices.DetectPointGetInRange;
import vn.edu.uit.floodpoint.Model.FloodPoint;
import vn.edu.uit.floodpoint.Model.GeoPoint;
import vn.edu.uit.floodpoint.Model.ListPoint;
import vn.edu.uit.floodpoint.R;
import vn.edu.uit.floodpoint.Receiver.AlarmReceiver;
import vn.edu.uit.floodpoint.Ultis.ProgressDialog;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleApiClient.ConnectionCallbacks {

    //FloodPoint
    ArrayList<FloodPoint> nearbyFloodPoint;


    //Views
    FloatingActionButton fabAdd;
    ScrollView layoutAdd;
    CoordinatorLayout layoutMap;
    ImageView imgPhoto;
    TextView txtAdress;
    EditText txtName;
    EditText txtDescription;
    RadioGroup rdgLasting;
    Switch swFollow;
    IndicatorSeekBar boundSeek;
    ImageView deleteButton;
    //Image
    String mCurrentPhotoPath;
    Uri mCurrentURI;
    //FireBase
    private DatabaseReference geoFireRef = FirebaseDatabase.getInstance().getReference("geofire");
    private DatabaseReference floodPointRef = FirebaseDatabase.getInstance().getReference("floodpoint");
    private GeoFire geoFire = new GeoFire(geoFireRef);
    StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://floodpoint-966f9.appspot.com");
    private GeoQuery geoQuery;
    private com.google.firebase.database.Query query;
    //Map
    private GoogleMap mMap;
    Geocoder geocoder;
    List<android.location.Address> addresses;
    LatLng curentPoint;
    private CameraPosition mCameraPosition;
    private GoogleApiClient mGoogleApiClient;// The entry point to Google Play services, used by the Places API and Fused Location Provider.
    private LocationRequest mLocationRequest;// A request object to store parameters for requests to the FusedLocationProviderApi.
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000; // The desired interval for location updates. Inexact. Updates may be more or less frequent.
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);// A default location (Sydney, Australia) and default zoom to use when location permission is// not granted.
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private boolean mLocationPermissionGranted;
    private Location mCurrentLocation;    // The geographical location where the device is currently located.
    private static final String KEY_CAMERA_POSITION = "camera_position";    // Keys for storing activity state.
    private static final String KEY_LOCATION = "location";
    SupportMapFragment mapFragment ;
    PlaceAutocompleteFragment autocompleteFragment;

    //Marker
    Marker selectMarker;
    Circle curentCircle;

    //Alarm
    Long scheduleTime;

    //Bound
    Integer currentBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        buildGoogleApiClient();
        mGoogleApiClient.connect();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            /*Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            Matrix matrix = new_point Matrix();
            matrix.postRotate(-90);
            Bitmap rotateBitMap= Bitmap.createBitmap(imageBitmap,0,0,imageBitmap.getWidth(),imageBitmap.getHeight(),matrix,true);
            imgPhoto.setImageBitmap(Bitmap.createBitmap(rotateBitMap,0,rotateBitMap.getHeight()/3,
                    rotateBitMap.getWidth(),rotateBitMap.getHeight()/3));*/
            Picasso.with(MapsActivity.this).load(mCurrentURI).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(imgPhoto);
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            getDeviceLocation();
        }
    }

    /**
     * Stop location updates when the activity is no longer in focus, to reduce battery consumption.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }

    }

    @Override
    public void onBackPressed() {
        if(layoutAdd.getVisibility()==View.VISIBLE){
            layoutAdd.setVisibility(View.GONE);
            fabAdd.setImageResource(R.drawable.add);
        }
        else
            super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mCurrentLocation);
            super.onSaveInstanceState(outState);
        }
    }


    @Override
    public void onConnected(Bundle connectionHint) {
        getDeviceLocation();
        // Build the map.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        mapFragment.getMapAsync(this);
    }

    /**
     * Handles failure to connect to the Google Play services client.
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        // Refer to the reference doc for ConnectionResult to see what error codes might
        // be returned in onConnectionFailed.
    }

    /**
     * Handles suspension of the connection to the Google Play services client.
     */
    @Override
    public void onConnectionSuspended(int cause) {

    }


    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        searchNearby(location.getLatitude(),location.getLongitude(),currentBound);
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
//                new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM));

    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    private void addEvent() {
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Picasso.with(MapsActivity.this).load(R.drawable.take_photo).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(imgPhoto);

                if(layoutAdd.getVisibility()==View.GONE) {
                    layoutAdd.setVisibility(View.VISIBLE);
                    fabAdd.setImageResource(R.drawable.send);
                }
                else if(checkInformation()){
                    layoutAdd.setVisibility(View.GONE);
                    fabAdd.setImageResource(R.drawable.add);
                    addNewPoint(new FloodPoint(txtName.getText().toString(),txtDescription.getText().toString()
                            ,curentPoint.latitude,curentPoint.longitude),mCurrentPhotoPath);
                }
                else
                    Toast.makeText(MapsActivity.this,"Information isn't provided!",Toast.LENGTH_LONG).show();
            }
        });
        imgPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCamera();
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                txtAdress.setText(getAdress(latLng));
                curentPoint=latLng;
                if (selectMarker != null) {
                    selectMarker.remove();
                }

                selectMarker = mMap.addMarker(new MarkerOptions().
                        position(curentPoint).
                        icon(BitmapDescriptorFactory.fromResource(R.drawable.new_point)));

                fabAdd.setVisibility(View.VISIBLE);
                deleteButton.setVisibility(View.GONE);
            }
        });

        rdgLasting.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int choice) {
                switch(choice){
                    case R.id.rdThreeHours:
                        scheduleTime = Long.valueOf(10800);
                    case R.id.rdSixHours:
                        scheduleTime = Long.valueOf(21600);
                    default:
                        scheduleTime = Long.valueOf(7200);
                }
            }
        });

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                searchNearby(place.getLatLng().latitude,place.getLatLng().longitude,currentBound);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(place.getLatLng().latitude,place.getLatLng().longitude),DEFAULT_ZOOM));
            }

            @Override
            public void onError(Status status) {

            }
        });

        swFollow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                  if(b){
                      ArrayList<String> keyList = new ArrayList<String>();
                      Intent intent = new Intent(MapsActivity.this,DetectPointGetInRange.class);
                      startService(intent);
                      SharedPreferences sharedPreferences = getSharedPreferences("followstate",MODE_PRIVATE);
                      SharedPreferences.Editor edit = sharedPreferences.edit();
                      edit.putBoolean("follow",true);
                      edit.commit();
                      Toast.makeText(MapsActivity.this, "Notification enabled", Toast.LENGTH_SHORT).show();

                  }
                  else {
                      stopService(new Intent(MapsActivity.this, DetectPointGetInRange.class));
                      SharedPreferences sharedPreferences = getSharedPreferences("followstate",MODE_PRIVATE);
                      SharedPreferences.Editor edit = sharedPreferences.edit();
                      edit.putBoolean("follow",false);
                      edit.commit();
                      Toast.makeText(MapsActivity.this, "Notification disable", Toast.LENGTH_SHORT).show();

                  }
            }
        });

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                searchNearby(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude(),currentBound);
                return false;
            }
        });
    }

    private void mapViewID() {
        fabAdd = (FloatingActionButton) findViewById(R.id.fabMenu);
        layoutAdd= (ScrollView) findViewById(R.id.layoutAdd);
        layoutMap = (CoordinatorLayout) findViewById(R.id.layoutMap);
        imgPhoto = (ImageView) findViewById(R.id.imgViewPhoto);
        txtAdress= (TextView) findViewById(R.id.txtViewAdress);
        txtName = (EditText) findViewById(R.id.txtViewName);
        txtDescription= (EditText) findViewById(R.id.txtDescription);
        rdgLasting = (RadioGroup) findViewById(R.id.rdgLasting);
        swFollow = (Switch) findViewById(R.id.swFolow);
        swFollow.bringToFront();
        boundSeek = (IndicatorSeekBar) findViewById(R.id.bound_seek);
        boundSeek.bringToFront();
        deleteButton = (ImageView) findViewById(R.id.delete);
        deleteButton.setImageResource(R.drawable.delete_point);
        deleteButton.bringToFront();
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final GeoPoint geoPoint = ListPoint.getInstance().getGeoByLocation(selectMarker.getPosition());
                try {
                    if(geoPoint.getKey()!=null){
                        geoFireRef.child(geoPoint.getKey()).removeValue();
                        floodPointRef.child(geoPoint.getKey()).removeValue();
                        selectMarker.remove();
                    }
                } catch (Exception e) {

                }
                finally {
                    fabAdd.setVisibility(View.VISIBLE);
                    deleteButton.setVisibility(View.GONE);
                    searchNearby(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), currentBound);
                }


            }
        });
        currentBound = 1;
        if(getFollowState())
            swFollow.setChecked(true);
        else
            swFollow.setChecked(false);
        autocompleteFragment.getView().setBackgroundColor(Color.WHITE);
        View locationButton = ((View) mapFragment.getView().findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                locationButton.getLayoutParams();
        // position on right bottom
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        //layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        layoutParams.setMargins(0, 330,30,0);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        scheduleTime= Long.valueOf(7200);

        boundSeek.setOnSeekChangeListener(new IndicatorSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(IndicatorSeekBar seekBar, int progress, float progressFloat, boolean fromUserTouch) {
                currentBound = progress;
                searchNearby(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), currentBound);
                if (curentCircle != null) {
                    curentCircle.remove();
                }

            }

            @Override
            public void onSectionChanged(IndicatorSeekBar seekBar, int thumbPosOnTick, String textBelowTick, boolean fromUserTouch) {

            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar, int thumbPosOnTick) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {

            }
        });

    }

    private boolean getFollowState() {
        SharedPreferences sharedPreferences = getSharedPreferences("followstate",MODE_PRIVATE);
        if(sharedPreferences.getBoolean("follow",false))
            return true;
        return false;
    }


    private String getAdress(LatLng latLng) {
        geocoder = new Geocoder(this,Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            return address;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Unknow";
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        createLocationRequest();
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        /*
         * Sets the desired interval for active location updates. This interval is
         * inexact. You may not receive updates at all if no location sources are available, or
         * you may receive them slower than requested. You may also receive updates faster than
         * requested if other applications are requesting location at a faster interval.
         */
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        /*
         * Sets the fastest rate for active location updates. This interval is exact, and your
         * application will never receive updates faster than this value.
         */
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void getDeviceLocation() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */

        createLocationRequest();

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         * Also request regular updates about the device location.
         */
        if (mLocationPermissionGranted) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, this);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */

    /**
     * Adds markers for places nearby the device and turns the My Location feature on or off,
     * provided location permission has been granted.
     */
    private void addInRangeMarkers(GeoPoint geo) {
        if (mMap == null) {
            return;
        }
        FloodPoint point = ListPoint.getInstance().getAllPost().get(geo.getKey());
        point.setMarker(mMap.addMarker(new MarkerOptions().position(geo.getPosition())
                .title(point.getName())
                .snippet(point.getComment())
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.flood_marker))));
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void removeOutRangeMarker(String key){
        ListPoint.getInstance().getAllPost().get(key).getMarker().remove();
    }


    @SuppressWarnings("MissingPermission")
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }

        if (mLocationPermissionGranted) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            mMap.setMyLocationEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mCurrentLocation = null;
        }
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
        mapViewID();
        addEvent();
        //loadData();
        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();
        // Add markers for nearby places.

        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                FloodPoint floodPoint = new FloodPoint();
                final GeoPoint geoPoint = ListPoint.getInstance().getGeoByLocation(marker.getPosition());

                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents, null);
                TextView txtName = ((TextView) infoWindow.findViewById(R.id.txtViewName));
                TextView txtDescription = ((TextView) infoWindow.findViewById(R.id.txtViewDescription));
                TextView txtAdress = ((TextView) infoWindow.findViewById(R.id.txtViewAdress));
                ImageView imgPhoto = (ImageView) infoWindow.findViewById(R.id.imgViewPhoto);


                if(geoPoint.getKey()!=null){
                    floodPoint=ListPoint.getInstance().getAllPost().get(geoPoint.getKey());
                    Picasso.with(MapsActivity.this).load(floodPoint.getImageUrl()).resize(200,120).centerCrop().into(imgPhoto);
                    txtName.setText(floodPoint.getName());
                    txtDescription.setText(floodPoint.getComment());
                    txtAdress.setText(getAdress(marker.getPosition()));
                }
                return infoWindow;
            }
        });
        /*
         * Set the map's camera position to the current location of the device.
         * If the previous state was saved, set the position to the saved state.
         * If the current location is unknown, use a default position and zoom value.
         */
        if (mCameraPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        } else if (mCurrentLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mCurrentLocation.getLatitude(),
                            mCurrentLocation.getLongitude()), DEFAULT_ZOOM));
        } else {
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                selectMarker = marker;
                fabAdd.setVisibility(View.GONE);
                deleteButton.setVisibility(View.VISIBLE);
                return false;
            }
        });
    }


    public void addNewPoint(final FloodPoint floodPoint, String imagePath) {
        ProgressDialog.getInstance(MapsActivity.this,"Working").showProgressDialog();
        final String key = floodPointRef.push().getKey();
        Uri file = Uri.fromFile(new File(imagePath));
        StorageReference postImageRef = storageRef.child("floodpoint/image/" + key + "/" + file.getLastPathSegment());
        UploadTask uploadTask = postImageRef.putFile(file);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                try {
                    selectMarker.remove();
                    floodPoint.setImageUrl(taskSnapshot.getDownloadUrl().toString());
                    floodPointRef.child(key).setValue(floodPoint);
                    geoFire.setLocation(key, new GeoLocation(floodPoint.getLatitude(), floodPoint.getLongitude()));
                    ProgressDialog.getInstance(MapsActivity.this,"Working").hideProgressDialog();
                    scheduleAlarm(scheduleTime,key);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
                Log.d("imagePath", photoFile.getAbsolutePath());
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                mCurrentURI = photoURI;
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                Log.d("imagePath", photoURI.toString());


            }
        }

    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String imageFileName = "cache";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void searchNearby(double latitude, double longitude, double distance) {
        mMap.clear();
        geoQuery = geoFire.queryAtLocation(new GeoLocation(latitude, longitude), distance);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                GeoPoint geoPoint =new GeoPoint(key,new LatLng(location.latitude,location.longitude));
                ListPoint.getInstance().getListGeo().add(geoPoint);
                addInRangeMarkers(geoPoint);

            }

            @Override
            public void onKeyExited(String key) {
//                ListPoint.getInstance().removeGeo(key);
//                removeOutRangeMarker(key);
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                System.out.println(String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));
            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                System.err.println("There was an error with this query: " + error);
            }
        });

        curentCircle = mMap.addCircle(
                new CircleOptions()
                        .center(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()))
                        .radius(currentBound * 1000));
    }

    private void scheduleAlarm(long timeInSecond, String keyToDelete){

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int totalPending = sharedPreferences.getInt("totalPending",0);
        totalPending++;

        Long time = new GregorianCalendar().getTimeInMillis()+timeInSecond*1000;
        Intent intentAlarm = new Intent(this, AlarmReceiver.class);
        intentAlarm.putExtra("key",keyToDelete);
        // create the object
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        //set the alarm for particular time
        alarmManager.set(AlarmManager.RTC_WAKEUP,time, PendingIntent.getBroadcast(this,totalPending,  intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
        Toast.makeText(this, "Point will be automatically deleted after " + timeInSecond/3600 + " hours", Toast.LENGTH_LONG).show();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("totalPending",totalPending);
        editor.apply();
    }

    private boolean checkInformation(){
        if(txtAdress.getText().toString() == "" ||
                txtDescription.getText().toString() == "" ||
                txtName.getText().toString() == "")
            return  false;
        return true;

    }
}
