package com.persistent.bionation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.maps.model.UrlTileProvider;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationPresenter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;


public class MainActivity extends AppCompatActivity implements LocationListener, OnMapReadyCallback {

    private static final String TAG = "MainActivity";
    BottomNavigationView bottomNavigationView;

    private static final String API_URL = "https://api.inaturalist.org/v1/" ;
    double lat=0.0,lng=0.0;
    GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private LatLngBounds mapPositionLatLngBounds;
    TileOverlay gridTileOverlay;
    TileOverlay pointTileOverlay;


    public interface Observations {
        @GET("observations?")
        Call<Observation> observations(@Query("nelat") double nelat, @Query("nelng") double nelng, @Query("swlat") double swlat, @Query("swlng") double swlng,@Query("place_id") int placeId, @Query("per_page") String perPage, @Query("page") String page);
    }

    public interface ObservationsById {
        @GET("observations/{id}")
        Call<Observation> observations(@Path("id") int observationId);
    }

    public interface Places {
        @GET("places/nearby")
        Call<Place> places(@Query("nelat") double nelat, @Query("nelng") double nelng, @Query("swlat") double swlat, @Query("swlng") double swlng);
    }

    private BottomSheetBehavior bottomSheetBehavior;
    private BottomSheetBehavior.BottomSheetCallback bottomSheetCallback;
    private TextView observationCommonNameTextView;
    private TextView addressTextView;
    private ImageView imageView;
    private TextView observationWikipediaTextView;
    ObservationsById observationsById;
    Observation observationResult;
    Address address;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setNavigationBarColor(Color.WHITE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }
        setContentView(R.layout.activity_main);
        observationCommonNameTextView = findViewById(R.id.BottomSheet_ObservationNameText);
        imageView = findViewById(R.id.BottomSheet_Image);
        addressTextView = findViewById(R.id.BottomSheet_LocationAddressText);
        observationWikipediaTextView = findViewById(R.id.BottomSheet_ObservationWikipediaText);
        observationCommonNameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this,"Text Clicked ", Toast.LENGTH_SHORT).show();
            }
        });

        View bottomSheet = findViewById(R.id.BottomSheet);
        bottomSheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN){
                    if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED){
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
                    }else if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HALF_EXPANDED){
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    }else if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED){
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
                    }
                }
            }
        });

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.setFitToContents(false);
        bottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState){
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        //observationCommonNameTextView.setText("Collapsed");
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        //observationCommonNameTextView.setText("Expanded");
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        //observationCommonNameTextView.setText("Dragging");
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        observationCommonNameTextView.setText("");
                        observationWikipediaTextView.setText("");
                        addressTextView.setText("");
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        //observationCommonNameTextView.setText("Setting");
                        break;
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        //observationCommonNameTextView.setText("Setting");
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        };

        bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback);




        Retrofit retrofit =
                new Retrofit.Builder()
                        .baseUrl(API_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

        //Observations observations = retrofit.create(Observations.class);
        //Places places = retrofit.create(Places.class);
        observationsById = retrofit.create(ObservationsById.class);

        GoogleMapOptions googleMapOptions = new GoogleMapOptions();
        googleMapOptions.mapType(GoogleMap.MAP_TYPE_SATELLITE).compassEnabled(false)
                .rotateGesturesEnabled(false)
                .tiltGesturesEnabled(false);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.Map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);


        getPermission();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mFusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY,null).addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                lat = location.getLatitude();
                lng = location.getLongitude();
                LatLng latLng = new LatLng(lat,lng);
                mMap.addMarker(new MarkerOptions().position(latLng).title("Current Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,13));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,13));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mMap.addCircle(new CircleOptions().center(new LatLng(lat,lng))
                            .radius(3000)
                            .strokeColor(Color.BLUE)
                            .strokeWidth(3)
                            //.fillColor(Color.argb(0.1f,0.0f,0.0f,1.0f))
                    );
                }

                LatLngBounds latLngBounds =  mMap.getProjection().getVisibleRegion().latLngBounds;

            }

        });

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        lat = location.getLatitude();
        lng = location.getLongitude();
        LatLng latLng = new LatLng(lat,lng);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,13));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,13));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        TileProvider gridTileProvider = new UrlTileProvider(256,256) {
            @Nullable
            @Override
            public URL getTileUrl(int i, int i1, int i2) {
                if( i2 > 10){
                    return null;
                }
                String s  = String.format(Locale.ENGLISH, API_URL + "grid/%d/%d/%d.png",i2,i,i1);
                try {
                    return new URL(s);
                }catch (MalformedURLException e){
                    throw  new AssertionError(e);
                }
            }
        };

        TileProvider pointTileProvider = new UrlTileProvider(256,256) {
            @Nullable
            @Override
            public URL getTileUrl(int i, int i1, int i2) {
                if( i2 <= 10){
                    return null;
                }
                String s  = String.format(Locale.ENGLISH, API_URL + "points/%d/%d/%d.png",i2,i,i1);
                try {
                    return new URL(s);
                }catch (MalformedURLException e){
                    throw  new AssertionError(e);
                }
            }

        };

        gridTileOverlay = mMap.addTileOverlay(new TileOverlayOptions().transparency((float)0.25).tileProvider(gridTileProvider));
        pointTileOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(pointTileProvider));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {

                Log.d(TAG, "onMapClick: "+latLng.toString());

                try {
                    Geocoder geocoder = new Geocoder(MainActivity.this);
                    address = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1).get(0);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                final int zoom = (int)Math.floor(mMap.getCameraPosition().zoom);
                final UTFPosition position = new UTFPosition(zoom, latLng.latitude, latLng.longitude);



                final String pointUrl = String.format(Locale.ENGLISH,API_URL+"points/%d/%d/%d.grid.json",zoom,position.getTilePositionX(),position.getTilePositionY());

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder()
                                .url(pointUrl)
                                .build();

                        try {
                            Response response = client.newCall(request).execute();
                            String responseBody = response.body().string();
                            JSONObject utfGridJSON = new JSONObject(responseBody);
                            UTFGrid utfGrid = new UTFGrid(utfGridJSON);

                            JSONObject observation = utfGrid.getDataForPixel(position.getPixelPositionX(),position.getPixelPositionY());

                            if(observation != null){
                                Call<Observation> call = observationsById.observations(Integer.parseInt(observation.getString("id")));
                                call.enqueue(new Callback<Observation>() {
                                    @Override
                                    public void onResponse(Call<Observation> call, retrofit2.Response<Observation> response) {
                                        observationResult = response.body();
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    ObservationResult resultObservation = observationResult.observationResultList.get(0);
                                                    Log.d(TAG, "Observation run: " + resultObservation.taxon.commonName);
                                                    observationCommonNameTextView.setText(resultObservation.taxon.scientificName);
                                                    addressTextView.setText(address.getAddressLine(0));
                                                    Glide.with(MainActivity.this)
                                                            .load(resultObservation.taxon.speciesPhoto.photoMediumUrl)
                                                            .into(imageView);
                                                    observationWikipediaTextView.setText(Html.fromHtml(resultObservation.taxon.wikipediaSummary));
                                                    
                                                    //observation.toString());
                                                    Toast.makeText(MainActivity.this, observation.getString("id"),Toast.LENGTH_SHORT).show();
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFailure(Call<Observation> call, Throwable t) {

                                    }
                                });
                                if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN || bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED){
                                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
                                }
                                Log.d(TAG, "Observation Grid: " + observation.toString());

                            }else{
                                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                            }

                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            }
        });

        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setIndoorLevelPickerEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setTrafficEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);


        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                LatLngBounds latLngBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
                mapPositionLatLngBounds = latLngBounds;
                if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HALF_EXPANDED){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        bottomSheetBehavior.removeBottomSheetCallback(bottomSheetCallback);
        gridTileOverlay.clearTileCache();
        pointTileOverlay.clearTileCache();
        gridTileOverlay.remove();
        pointTileOverlay.remove();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int grantResult : grantResults) {
            if(grantResult != PackageManager.PERMISSION_GRANTED){
                getPermission();
            }
        }
    }

    private void getPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 101);
    }

}