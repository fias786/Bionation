package com.persistent.bionation.ui.explore;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
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
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.maps.model.UrlTileProvider;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.card.MaterialCardView;
import com.ibm.cloud.sdk.core.security.Authenticator;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.persistent.bionation.R;
import com.persistent.bionation.adapter.ObservationImageRecyclerView;
import com.persistent.bionation.data.ObservationImageData;
import com.persistent.bionation.ui.camera.CameraFragment;
import com.persistent.bionation.ui.camera.ObservationItems;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Interceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class ExploreFragment extends Fragment implements LocationListener, OnMapReadyCallback, TileProvider, MicBottomSheetDialog.BottomSheetListener {

    private static final String TAG = "MainActivity";
    private BottomNavigationView bottomNavigationView;

    private static final String API_URL = "https://api.inaturalist.org/v1/" ;
    private static final String genAI_URL = "https://eu-de.ml.cloud.ibm.com/ml/v1-beta/generation/text?version=2023-05-29/";
    double lat=0.0,lng=0.0;
    GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private LatLngBounds mapPositionLatLngBounds;
    TileOverlay gridTileOverlay;
    TileOverlay pointTileOverlay;

    public String getTileUrl(int x, int y, int z, String text) {
        return String.format(Locale.ENGLISH, API_URL + "points/%d/%d/%d.png?taxon_name=%s",z,x,y,text);
    }

    @Nullable
    @Override
    public Tile getTile(int i, int i1, int i2) {
        byte[] tileImage = null;
        try {
            tileImage = getTileImage(i, i1, i2, placeText);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(tileImage != null){
            Log.d(TAG, "getTile: "+ Arrays.toString(tileImage));
            return new Tile(256,256, tileImage);
        }
        return NO_TILE;
    }

    private byte[] getTileImage(int i, int i1, int i2, String text) throws IOException {
        Bitmap bmp = null;
        URL url = null;
        try {
            url = new URL( getTileUrl(i, i1, i2, text));
            bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if(bmp == null){
            return null;
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG,100,stream);
        return stream.toByteArray();
    }

    @Override
    public void onMicClicked(String text) {
        Log.d(TAG, "onMicClicked: "+text);
        if(!text.equals("")){
            requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    searchText.setText(text);
                    searchText.clearFocus();
                }
            });
        }
    }

    public interface GenAIRequest {
        @POST("generation/text?version=2023-05-29/")
        Call<GenAIData> genAIRequest();
    }

    public interface ObservationsById {
        @GET("observations/{id}")
        Call<Observation> observations(@Path("id") int observationId);
    }

    private MaterialCardView expandedAppBar;
    private MaterialCardView searchArea;
    private BottomSheetBehavior bottomSheetBehavior;
    private BottomSheetBehavior.BottomSheetCallback bottomSheetCallback;
    private TextView observationCommonNameTextView;
    private TextView addressTextView;
    private ImageView bottomSheetImageVIew;
    private TextView observationWikipediaTextView;
    private TextView genAITextView;
    private ImageButton bottomSheetDownArrow;
    private EditText searchText;
    private ImageButton searchMic;
    ObservationsById observationsById;
    Observation observationResult;
    CameraFragment.Observations observations;
    com.persistent.bionation.ui.camera.Observation observationItemsResult;
    Address address;
    private RecyclerView observationRecyclerView;
    private ObservationImageRecyclerView observationImageAdapter;
    private ArrayList<ObservationImageData> loadObservationImages = new ArrayList<>();

    String placeText="";
    String genAIAccessToken="";
    GenAIRequest genAIRequest;

    private MicBottomSheetDialog micBottomSheetDialog;
    private MicBottomSheetDialog.BottomSheetListener listener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listener = this::onMicClicked;
    }

    @SuppressLint("MissingPermission")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_explore, container, false);
        final float scale = getContext().getResources().getDisplayMetrics().density;
        bottomNavigationView = requireActivity().findViewById(R.id.nav_view);
        expandedAppBar = root.findViewById(R.id.ExpandedAppBar);
        bottomSheetDownArrow = root.findViewById(R.id.BottomSheetDownArrow);
        observationRecyclerView = root.findViewById(R.id.ExploreImageRecyclerView);

        searchArea = root.findViewById(R.id.SearchArea);
        searchText = root.findViewById(R.id.SearchEditText);
        searchMic = root.findViewById(R.id.SearchMic);

        observationCommonNameTextView = root.findViewById(R.id.BottomSheet_ObservationNameText);
        bottomSheetImageVIew = root.findViewById(R.id.BottomSheet_Image);
        addressTextView = root.findViewById(R.id.BottomSheet_LocationAddressText);
        observationWikipediaTextView = root.findViewById(R.id.BottomSheet_ObservationWikipediaText);
        genAITextView = root.findViewById(R.id.BottomSheet_GenAIText);
        observationCommonNameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        observationRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
        observationRecyclerView.setHasFixedSize(true);
        observationImageAdapter = new ObservationImageRecyclerView(getContext(),loadObservationImages);
        observationRecyclerView.setAdapter(observationImageAdapter);


        bottomSheetDownArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                expandedAppBar.setVisibility(View.INVISIBLE);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
            }
        });


        if(new IamAuthenticator.Builder().apikey("").build().requestToken().needsRefresh()){
            genAIAccessToken = new IamAuthenticator.Builder().apikey("").build().requestToken().getRefreshToken();
            Log.d(TAG, "1 Gen AI Token: "+genAIAccessToken);
        }else{
            genAIAccessToken = new IamAuthenticator.Builder().apikey("").build().requestToken().getAccessToken();
            Log.d(TAG, "2 Gen AI Token: "+genAIAccessToken);
        }

        Log.d(TAG, "Gen AI Token: "+genAIAccessToken);

        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                placeText = charSequence.toString();
                Log.d(TAG, "onTextChanged: " + charSequence + " " + i  + " "+ i1 + " "+i2);

                if(gridTileOverlay!=null){
                    gridTileOverlay.remove();
                }
                if(pointTileOverlay!=null){
                    pointTileOverlay.remove();
                }

                TileProvider gridTileProvider = new UrlTileProvider(256,256) {
                    @Nullable
                    @Override
                    public URL getTileUrl(int i, int i1, int i2) {
                        if( i2 > 10){
                            return null;
                        }
                        String s  = String.format(Locale.ENGLISH, API_URL + "grid/%d/%d/%d.png?taxon_name=%s&color=blue",i2,i,i1,placeText);
                        Log.d(TAG, "getTileUrl: "+s);
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
                        String s  = String.format(Locale.ENGLISH, API_URL + "points/%d/%d/%d.png?taxon_name=%s",i2,i,i1,placeText);
                        Log.d(TAG, "getTileUrl: "+s);
                        try {
                            return new URL(s);
                        }catch (MalformedURLException e){
                            throw  new AssertionError(e);
                        }
                    }

                };

                gridTileOverlay = mMap.addTileOverlay(new TileOverlayOptions().transparency((float)0.25).tileProvider(gridTileProvider));
                pointTileOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(pointTileProvider));

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH){
                    //searchOnMap();
                    searchText.clearFocus();
                    InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(root.getWindowToken(),0);
                }
                return true;
            }
        });

        searchMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                micBottomSheetDialog = new MicBottomSheetDialog(listener);
                micBottomSheetDialog.show(getChildFragmentManager(),"micBottomSheet");
            }
        });



        View bottomSheet = root.findViewById(R.id.BottomSheet);
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
        int expandedOffsetPixels = (int) (80 * scale + 0.5f);
        bottomSheetBehavior.setExpandedOffset(expandedOffsetPixels);
        bottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        Log.d(TAG, "onStateChanged: Collapsed");
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        Log.d(TAG, "onStateChanged: Expanded");
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        Log.d(TAG, "onStateChanged: Dragging ");
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        observationCommonNameTextView.setText("");
                        observationWikipediaTextView.setText("");
                        addressTextView.setText("");
                        bottomSheetImageVIew.setImageResource(0);
                        loadObservationImages.clear();
                        observationImageAdapter.notifyDataSetChanged();
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        Log.d(TAG, "onStateChanged: Settling");
                        break;
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        Log.d(TAG, "onStateChanged: " + "Half Expanded");
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                ViewGroup.LayoutParams layoutParams;
                final float scale = getContext().getResources().getDisplayMetrics().density;
                int bottomNavHeightPixels = (int) (50 * scale + 0.5f);
                Log.d(TAG, "onSlide: " + slideOffset);
                if(slideOffset == 1.0){
                    expandedAppBar.setVisibility(View.VISIBLE);
                    bottomSheet.setElevation(0);
                }else{
                    expandedAppBar.setVisibility(View.INVISIBLE);
                    bottomSheet.setElevation(5);
                }

                if(slideOffset <= 0.432){
                    searchArea.setVisibility(View.VISIBLE);
                }else{
                    searchArea.setVisibility(View.INVISIBLE);
                }
                
                if(slideOffset > -1.0){
                    layoutParams = bottomNavigationView.getLayoutParams();
                    layoutParams.height = 0;
                    bottomNavigationView.setLayoutParams(layoutParams);
                }else{
                    layoutParams = bottomNavigationView.getLayoutParams();
                    layoutParams.height = bottomNavHeightPixels;
                    bottomNavigationView.setLayoutParams(layoutParams);
                }

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
        observations = (CameraFragment.Observations) retrofit.create(CameraFragment.Observations.class);

        GoogleMapOptions googleMapOptions = new GoogleMapOptions();
        googleMapOptions.mapType(GoogleMap.MAP_TYPE_SATELLITE).compassEnabled(false)
                .rotateGesturesEnabled(false)
                .tiltGesturesEnabled(false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.Map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);


        getPermission();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

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

        OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(!bottomNavigationView.getMenu().getItem(0).isChecked()){
                    bottomNavigationView.setSelectedItemId(bottomNavigationView.getMenu().getItem(0).getItemId());
                }else if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
                }else if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HALF_EXPANDED || bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }else if(!searchText.getText().toString().equals("")){
                    placeText = "";
                    searchText.setText("");
                    searchText.clearFocus();
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lng),13));
                }else{
                    requireActivity().finish();
                }


            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(requireActivity(),backPressedCallback);

        return root;
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

        if(gridTileOverlay!=null){
            gridTileOverlay.remove();
        }
        if(pointTileOverlay!=null){
            pointTileOverlay.remove();
        }

        TileProvider gridTileProvider = new UrlTileProvider(256,256) {
            @Nullable
            @Override
            public URL getTileUrl(int i, int i1, int i2) {
                if( i2 > 10){
                    return null;
                }
                String s  = String.format(Locale.ENGLISH, API_URL + "grid/%d/%d/%d.png?taxon_name=%s&color=blue",i2,i,i1,placeText);
                Log.d(TAG, "getTileUrl: "+s);
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
                String s  = String.format(Locale.ENGLISH, API_URL + "points/%d/%d/%d.png?taxon_name=%s",i2,i,i1,placeText);
                Log.d(TAG, "getTileUrl: "+s);
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
                searchText.clearFocus();

                try {
                    Geocoder geocoder = new Geocoder(getContext());
                    if(geocoder.getFromLocation(latLng.latitude,latLng.longitude,1)!=null && geocoder.getFromLocation(latLng.latitude,latLng.longitude,1).get(0)!=null) {
                        address = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1).get(0);
                    }

                } catch (IOException | IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }

                final int zoom = (int)Math.floor(mMap.getCameraPosition().zoom);
                final UTFPosition position = new UTFPosition(zoom, latLng.latitude, latLng.longitude);

                final String pointUrl = String.format(Locale.ENGLISH,API_URL+"points/%d/%d/%d.grid.json?taxon_name=%s",zoom,position.getTilePositionX(),position.getTilePositionY(),placeText);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder()
                                .url(pointUrl)
                                .build();


                        try {
                            okhttp3.Response response = client.newCall(request).execute();
                            String responseBody = response.body().string();
                            JSONObject utfGridJSON = new JSONObject(responseBody);
                            UTFGrid utfGrid = new UTFGrid(utfGridJSON);

                            JSONObject observation = utfGrid.getDataForPixel(position.getPixelPositionX(),position.getPixelPositionY());

                            if(observation != null){
                                Call<Observation> call = observationsById.observations(Integer.parseInt(observation.getString("id")));
                                call.enqueue(new Callback<Observation>() {
                                    @Override
                                    public void onResponse(Call<Observation> call, Response<Observation> response) {
                                        observationResult = response.body();
                                        requireActivity().runOnUiThread(new Runnable() {
                                            @SuppressLint("SetTextI18n")
                                            @Override
                                            public void run() {
                                                genAITextView.setText("");
                                                observationWikipediaTextView.setText("");
                                                loadObservationImages.clear();
                                                observationImageAdapter.notifyDataSetChanged();
                                                ObservationResult resultObservation = observationResult.observationResultList.get(0);
                                                Log.d(TAG, "Observation run: " + resultObservation.taxon.commonName);
                                                observationCommonNameTextView.setText(resultObservation.taxon.scientificName);
                                                if(address!=null && address.getAddressLine(0)!=null){
                                                    if(resultObservation.taxon.commonName!=null && resultObservation.taxon.commonName!=""){
                                                        addressTextView.setText("Common Name: "+resultObservation.taxon.commonName+"\n\n"+address.getAddressLine(0));
                                                    }else{
                                                        addressTextView.setText(address.getAddressLine(0));
                                                    }
                                                }else{
                                                    if(resultObservation.taxon.commonName!=null && resultObservation.taxon.commonName!=""){
                                                        addressTextView.setText("Common Name: "+resultObservation.taxon.commonName+"\n\n"+"Unknown Location");
                                                    }else{
                                                        addressTextView.setText("Unknown Location");
                                                    }
                                                }

                                                Glide.with(requireActivity())
                                                        .load(resultObservation.taxon.speciesPhoto.photoMediumUrl)
                                                        .placeholder(R.drawable.image_spinner)
                                                        .into(bottomSheetImageVIew);
                                                Call<com.persistent.bionation.ui.camera.Observation> call = observations.observations(true,resultObservation.taxon.id,"30","1");
                                                call.enqueue(new Callback<com.persistent.bionation.ui.camera.Observation>() {
                                                    @Override
                                                    public void onResponse(Call<com.persistent.bionation.ui.camera.Observation> call, Response<com.persistent.bionation.ui.camera.Observation> response) {
                                                        observationItemsResult = response.body();
                                                        requireActivity().runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                List<String> imageUrls = new ArrayList<>();
                                                                for (int i = 0; i < observationItemsResult.observationItems.size(); i++) {
                                                                    for (int j = 0; j < observationItemsResult.observationItems.get(i).photosList.size(); j++) {
                                                                        String squareUrl = observationItemsResult.observationItems.get(i).photosList.get(j).url;
                                                                        String mediumUrl = squareUrl.split("square")[0] + "medium" + squareUrl.split("square")[1];
                                                                        imageUrls.add(mediumUrl);
                                                                    }
                                                                }
                                                                for (int i = 0; i < imageUrls.size(); i=i+3) {
                                                                    ObservationImageData observationImageData = new ObservationImageData(
                                                                            imageUrls.get((i)%imageUrls.size()),
                                                                            imageUrls.get((i+1)%imageUrls.size()),
                                                                            imageUrls.get((i+2)%imageUrls.size()));
                                                                    loadObservationImages.add(observationImageData);
                                                                    observationImageAdapter.notifyDataSetChanged();
                                                                }

                                                                if(resultObservation.taxon.wikipediaSummary != null){
                                                                    observationWikipediaTextView.setText(Html.fromHtml(resultObservation.taxon.wikipediaSummary));
                                                                }else {
                                                                    observationWikipediaTextView.setText("");
                                                                }

                                                            }
                                                        });
                                                    }

                                                    @Override
                                                    public void onFailure(Call<com.persistent.bionation.ui.camera.Observation> call, Throwable t) {

                                                    }
                                                });

                                                new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        OkHttpClient client = new OkHttpClient().newBuilder().build();
                                                        okhttp3.MediaType mediaType = okhttp3.MediaType.parse("application/json");
                                                        String mediaTypeData = "";
                                                        if(resultObservation.taxon.commonName!=null && resultObservation.taxon.commonName!=""){
                                                            mediaTypeData = "{\r\n\r\n \"model_id\": \"google/flan-t5-xxl\",\r\n\r\n \"input\": \"tell me something about "+resultObservation.taxon.commonName+" \\nTopic: biodiversity, extinction, disaster, wildlife, conservation, protection, habitat, mammals, birds, marine animals, wildfires, wetlands, rainforests, mangroves, encroachment, plastic\\\\nTone: energetic\\\\n\",\r\n\r\n \"parameters\": {\r\n\r\n \"decoding_method\": \"sample\",\r\n\r\n \"max_new_tokens\": 200,\r\n\r\n \"min_new_tokens\": 50,\r\n\r\n \"random_seed\": 111,\r\n\r\n \"stop_sequences\": [],\r\n\r\n \"temperature\": 0.17,\r\n\r\n \"top_k\": 10,\r\n\r\n \"top_p\": 1,\r\n\r\n \"repetition_penalty\": 2\r\n\r\n },\r\n\r\n \"project_id\": \"20fd5695-7674-48b7-867b-ab0a9908ba5a\"\r\n\r\n}";
                                                        }else{
                                                            mediaTypeData = "{\r\n\r\n \"model_id\": \"google/flan-t5-xxl\",\r\n\r\n \"input\": \"tell me something about Biodiversity \\nTopic: biodiversity, extinction, disaster, wildlife, conservation, protection, habitat, mammals, birds, marine animals, wildfires, wetlands, rainforests, mangroves, encroachment, plastic\\\\nTone: energetic\\\\n\",\r\n\r\n \"parameters\": {\r\n\r\n \"decoding_method\": \"sample\",\r\n\r\n \"max_new_tokens\": 200,\r\n\r\n \"min_new_tokens\": 50,\r\n\r\n \"random_seed\": 111,\r\n\r\n \"stop_sequences\": [],\r\n\r\n \"temperature\": 0.17,\r\n\r\n \"top_k\": 10,\r\n\r\n \"top_p\": 1,\r\n\r\n \"repetition_penalty\": 2\r\n\r\n },\r\n\r\n \"project_id\": \"20fd5695-7674-48b7-867b-ab0a9908ba5a\"\r\n\r\n}";
                                                        }
                                                        Log.d(TAG, "Gen AI run: "+mediaTypeData);
                                                        okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, mediaTypeData);
                                                        Request request = new Request.Builder().url("https://eu-de.ml.cloud.ibm.com/ml/v1-beta/generation/text?version=2023-05-29")
                                                                .method("POST", body)
                                                                .addHeader("Authorization", "Bearer "+genAIAccessToken)
                                                                .build();
                                                        try {
                                                            okhttp3.Response response = client.newCall(request).execute();
                                                            JSONObject genAIResponse = new JSONObject(response.body().string());
                                                            genAIResponse.getJSONArray("results").get(0);


                                                            Log.d(TAG, "Gen AI run: "+ new JSONObject(genAIResponse.getJSONArray("results").get(0).toString()).getString("generated_text"));
                                                            requireActivity().runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    try {
                                                                        genAITextView.setText(new JSONObject(genAIResponse.getJSONArray("results").get(0).toString()).getString("generated_text").replaceAll("nn"," "));
                                                                    } catch (JSONException e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                }
                                                            });
                                                        }catch (Exception e){
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }).start();

                                                if(resultObservation.taxon.wikipediaSummary != null){
                                                    observationWikipediaTextView.setText(Html.fromHtml(resultObservation.taxon.wikipediaSummary));
                                                }else {
                                                    observationWikipediaTextView.setText("");
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
    public void onStart() {
        super.onStart();
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getActivity().getWindow().setNavigationBarColor(Color.WHITE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLocationCallback != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
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
        ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
    }
}