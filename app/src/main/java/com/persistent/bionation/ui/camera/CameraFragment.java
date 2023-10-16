package com.persistent.bionation.ui.camera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.maps.model.UrlTileProvider;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.common.util.concurrent.ListenableFuture;
import com.persistent.bionation.R;
import com.persistent.bionation.adapter.ImageRecyclerViewAdapter;
import com.persistent.bionation.adapter.ObservationImageRecyclerView;
import com.persistent.bionation.data.CommonName;
import com.persistent.bionation.data.ImageGalleryData;
import com.persistent.bionation.data.ObservationImageData;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class CameraFragment extends Fragment implements LocationListener, OnMapReadyCallback {

    private static final String TAG = "CameraFragment";
    private static final String API_URL = "https://api.inaturalist.org/v1/";
    private static final String Wiki_URL = "https://en.wikipedia.org/api/rest_v1/";

    PreviewView previewView;
    private BottomNavigationView bottomNavigationView;
    ImageClassifier imageClassifier;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ProcessCameraProvider cameraProvider;
    private Camera camera;
    private OverlayView overlayView;
    private ImageButton cameraCapture,cameraFlash, closeCamera, cameraGallery,cameraSetting;
    private boolean isFlashOn = false;
    private BottomSheetBehavior bottomSheetBehavior;
    private BottomSheetBehavior.BottomSheetCallback bottomSheetCallback;
    private TextView observationCommonNameTextView;
    private ImageView bottomSheetImageVIew;
    private TextView observationWikipediaTextView;
    private TextView observationIsThreatened;
    Observations observations;
    Observation observationResult;
    WikipediaSummary wikipediaSummary;
    double lat=0.0,lng=0.0;
    GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;
    private BottomSheetBehavior imageGalleryBottomSheetBehavior;
    private BottomSheetBehavior.BottomSheetCallback imageGalleryBottomSheetCallback;
    private RecyclerView imageGalleryRecyclerView;
    View imageGalleryBottomSheet, bottomSheet;
    private RecyclerView observationRecyclerView;
    private ObservationImageRecyclerView observationImageAdapter;
    private ArrayList<ObservationImageData> loadObservationImages = new ArrayList<>();
    SortedMap<Float,Map<String,SpeciesObject>> result;
    private boolean changeObservationOverlay = true;
    TileOverlay gridTileOverlay;
    TileOverlay pointTileOverlay;
    String placeText = "";
    SupportMapFragment mapFragment;

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
                String s  = String.format(Locale.ENGLISH, API_URL + "grid/%d/%d/%d.png?taxon_name=%s",i2,i,i1,placeText);
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
    public void onLocationChanged(@NonNull Location location) {

    }

    public interface Observations {
        @GET("observations?")
        Call<Observation> observations(@Query("verifiable") boolean verifiable, @Query("taxon_id") int taxonId, @Query("per_page") String perPage, @Query("page") String page);
    }

    public interface WikipediaSummary {
        @GET("page/summary/{title}")
        Call<WikipediaSummaryData> wikipediaSummary(@Path("title") String title);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cameraProviderFuture = ProcessCameraProvider.getInstance(getContext());
        Realm.init(getContext());
        Realm.setDefaultConfiguration(new RealmConfiguration.Builder().name("CommonName").deleteRealmIfMigrationNeeded().allowWritesOnUiThread(true).allowQueriesOnUiThread(true).build());

    }

    Realm realm;

    @SuppressLint("MissingPermission")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_camera, container, false);
        bottomSheet = root.findViewById(R.id.CameraBottomSheet);
        imageGalleryBottomSheet = root.findViewById(R.id.ImageGalleryBottomSheet);
        overlayView = root.findViewById(R.id.CameraOverlay);
        cameraCapture = root.findViewById(R.id.Capture);
        cameraFlash = root.findViewById(R.id.CameraFlash);
        closeCamera = root.findViewById(R.id.CloseCamera);
        cameraGallery = root.findViewById(R.id.CameraGallery);
        cameraSetting = root.findViewById(R.id.CameraSetting);
        observationCommonNameTextView = root.findViewById(R.id.CameraBottomSheet_ObservationNameText);
        bottomSheetImageVIew = root.findViewById(R.id.CameraBottomSheet_Image);
        observationWikipediaTextView = root.findViewById(R.id.CameraBottomSheet_ObservationWikipediaText);
        observationIsThreatened = root.findViewById(R.id.CameraBottomSheet_ObservationIsThreatenedText);
        imageGalleryRecyclerView = root.findViewById(R.id.ImageGalleryRecyclerView);
        observationRecyclerView = root.findViewById(R.id.BottomSheet_ObservationRecyclerView);
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.CameraMap);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        mapFragment.getView().setVisibility(View.INVISIBLE);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        bottomNavigationView = requireActivity().findViewById(R.id.nav_view);
        ViewGroup.LayoutParams layoutParams = bottomNavigationView.getLayoutParams();
        layoutParams.height = 0;
        bottomNavigationView.setLayoutParams(layoutParams);
        previewView = root.findViewById(R.id.CameraPreview);
        getPermission();
        startCamera();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        mFusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY,null).addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                lat = location.getLatitude();
                lng = location.getLongitude();
                LatLng latLng = new LatLng(lat,lng);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,13));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,13));

            }

        });

        try {
            AssetFileDescriptor model = requireActivity().getAssets().openFd("model.tflite");
            InputStream label = requireActivity().getAssets().open("labels.csv");
            imageClassifier = new ImageClassifier(model,label,"1.0");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Retrofit retrofit =
                new Retrofit.Builder()
                        .baseUrl(API_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

        Retrofit wikipediaRetrofit =
                new Retrofit.Builder()
                        .baseUrl(Wiki_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

        observations = retrofit.create(Observations.class);
        wikipediaSummary = wikipediaRetrofit.create(WikipediaSummary.class);

        observationRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
        observationRecyclerView.setHasFixedSize(true);
        observationImageAdapter = new ObservationImageRecyclerView(getContext(),loadObservationImages);
        observationRecyclerView.setAdapter(observationImageAdapter);


        realm = Realm.getDefaultInstance();

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                try {
                    InputStream inputStream = requireActivity().getAssets().open("commonNames.txt");
                    realm.createAllFromJson(CommonName.class, inputStream);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final float scale = getContext().getResources().getDisplayMetrics().density;
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
                        //Log.d(TAG, "onStateChanged: Collapsed");
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        //Log.d(TAG, "onStateChanged: Expanded");
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        //Log.d(TAG, "onStateChanged: Dragging ");
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        observationCommonNameTextView.setText("");
                        observationWikipediaTextView.setText("");
                        observationIsThreatened.setText("");
                        bottomSheetImageVIew.setImageResource(0);
                        result.clear();
                        loadObservationImages.clear();
                        observationImageAdapter.notifyDataSetChanged();
                        mapFragment.getView().setVisibility(View.INVISIBLE);
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        //Log.d(TAG, "onStateChanged: Settling");
                        break;
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        //Log.d(TAG, "onStateChanged: " + "Half Expanded");
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }

        };

        imageGalleryBottomSheetBehavior = BottomSheetBehavior.from(imageGalleryBottomSheet);
        imageGalleryBottomSheetBehavior.setHideable(true);
        imageGalleryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        imageGalleryBottomSheetBehavior.setFitToContents(false);
        imageGalleryBottomSheetBehavior.setExpandedOffset(expandedOffsetPixels);
        imageGalleryBottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        //Log.d(TAG, "onStateChanged: Collapsed");
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        //Log.d(TAG, "onStateChanged: Expanded");
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        //Log.d(TAG, "onStateChanged: Dragging ");
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        //Log.d(TAG, "onStateChanged: Hidden");
                        observationIsThreatened.setText("");
                        observationCommonNameTextView.setText("");
                        result.clear();
                        loadObservationImages.clear();
                        observationImageAdapter.notifyDataSetChanged();
                        mapFragment.getView().setVisibility(View.INVISIBLE);
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        //Log.d(TAG, "onStateChanged: Settling");
                        break;
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        //Log.d(TAG, "onStateChanged: " + "Half Expanded");
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }

        };

        imageGalleryRecyclerView.setLayoutManager(new GridLayoutManager(getContext(),4));
        imageGalleryRecyclerView.setHasFixedSize(true);
        imageGalleryRecyclerView.setAdapter(new ImageRecyclerViewAdapter(getContext(),requireActivity(),observationCommonNameTextView,observationIsThreatened,observationImageAdapter,loadObservationImages,observationWikipediaTextView,bottomSheetBehavior,imageGalleryBottomSheetBehavior,loadImagesFromGallery()));

        cameraFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isFlashOn){
                    cameraFlash.setBackgroundResource(R.drawable.ic_flash_on_24dp);
                    isFlashOn = true;
                    camera.getCameraControl().enableTorch(true);
                }else{
                    cameraFlash.setBackgroundResource(R.drawable.ic_flash_off_24dp);
                    isFlashOn = false;
                    camera.getCameraControl().enableTorch(false);
                }
            }
        });

        closeCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(imageGalleryBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED || imageGalleryBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED || imageGalleryBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HALF_EXPANDED){
                    imageGalleryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }else if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
                }else if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HALF_EXPANDED || bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }else{
                    requireActivity().onBackPressed();
                }
            }
        });

        cameraCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                observationCommonNameTextView.setText("");
                observationWikipediaTextView.setText("");
                observationIsThreatened.setText("");
                loadObservationImages.clear();
                observationImageAdapter.notifyDataSetChanged();
                mapFragment.getView().setVisibility(View.INVISIBLE);
                if(result.get(10.0f)!=null){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
                    Call<Observation> call = observations.observations(true,result.get(10.0f).get("species").taxon_id,"30","1");
                    call.enqueue(new Callback<Observation>() {
                        @Override
                        public void onResponse(Call<Observation> call, retrofit2.Response<Observation> response) {
                            observationResult = response.body();
                            requireActivity().runOnUiThread(new Runnable() {
                                @SuppressLint("SetTextI18n")
                                @Override
                                public void run() {
                                    ObservationItems resultObservation = observationResult.observationItems.get(0);
                                    observationCommonNameTextView.setText(resultObservation.taxon.scientificName);
                                    if(resultObservation.taxon.scientificName.split(" ").length>1){
                                        placeText = resultObservation.taxon.scientificName.split(" ")[0];
                                    }else{
                                        placeText = resultObservation.taxon.scientificName;
                                    }
                                    mapFragment.getView().setVisibility(View.VISIBLE);

                                    if(resultObservation.taxon.isThreatened.equals("true")){
                                        observationIsThreatened.setText("Common Name: "+resultObservation.taxon.commonName+"\n\nIs Species Threatened: Yes\nObservation Count: "+resultObservation.taxon.observations_count);
                                    }else{
                                        observationIsThreatened.setText("Common Name: "+resultObservation.taxon.commonName+"\n\nIs Species Threatened: No\nObservation Count: "+resultObservation.taxon.observations_count);
                                    }
                                    List<String> imageUrls = new ArrayList<>();
                                    for (int i = 0; i < observationResult.observationItems.size(); i++) {
                                        for (int j = 0; j < observationResult.observationItems.get(i).photosList.size(); j++) {
                                            String squareUrl = observationResult.observationItems.get(i).photosList.get(j).url;
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

                                    Glide.with(requireActivity())
                                            .load(resultObservation.taxon.speciesPhoto.photoMediumUrl)
                                            .placeholder(R.drawable.image_spinner)
                                            .into(bottomSheetImageVIew);
                                    if(resultObservation.taxon.wikipediaSummary != null){
                                        observationWikipediaTextView.setText(Html.fromHtml(resultObservation.taxon.wikipediaSummary));
                                    }else {
                                        Call<WikipediaSummaryData> wikipediaSummaryDataCall = wikipediaSummary.wikipediaSummary(resultObservation.taxon.scientificName);
                                        wikipediaSummaryDataCall.enqueue(new Callback<WikipediaSummaryData>() {
                                            @Override
                                            public void onResponse(Call<WikipediaSummaryData> call, Response<WikipediaSummaryData> response) {
                                                WikipediaSummaryData wikipediaSummaryData = response.body();
                                                if(wikipediaSummaryData!=null && wikipediaSummaryData.extract_html!=null && !wikipediaSummaryData.extract_html.equals("")) {
                                                    observationWikipediaTextView.setText(Html.fromHtml(wikipediaSummaryData.extract_html));
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<WikipediaSummaryData> call, Throwable t) {

                                            }
                                        });
                                    }

                                }
                            });
                        }

                        @Override
                        public void onFailure(Call<Observation> call, Throwable t) {

                        }
                    });
                }else{
                    Toast.makeText(getContext(),"Species not detected",Toast.LENGTH_SHORT).show();
                }
            }
        });

        cameraGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageGalleryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
            }
        });

        cameraSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(changeObservationOverlay){
                    Toast.makeText(getContext(),"Show only species",Toast.LENGTH_LONG).show();
                    changeObservationOverlay = false;
                }else{
                    Toast.makeText(getContext(),"Show all taxonomy levels",Toast.LENGTH_LONG).show();
                    changeObservationOverlay = true;
                }
            }
        });

    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onDestroyView() {
        cameraProvider.unbindAll();
        cameraProvider.shutdown();
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getActivity().getWindow().setNavigationBarColor(Color.WHITE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }
        final float scale = getContext().getResources().getDisplayMetrics().density;
        int bottomNavHeightPixels = (int) (50 * scale + 0.5f);
        bottomNavigationView = requireActivity().findViewById(R.id.nav_view);
        ViewGroup.LayoutParams layoutParams = bottomNavigationView.getLayoutParams();
        layoutParams.height = bottomNavHeightPixels;
        bottomNavigationView.setLayoutParams(layoutParams);
        super.onDestroyView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean permissionGranted = true;
        for (int grantResult : grantResults) {
            if(grantResult != PackageManager.PERMISSION_GRANTED){
                getPermission();
                permissionGranted = false;
            }
        }
        if(!permissionGranted){
            Toast.makeText(getContext(),"Permission request denied",Toast.LENGTH_SHORT).show();
        }else{
            startCamera();
        }
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

        imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), new ImageAnalysis.Analyzer() {
            @SuppressLint("UnsafeOptInUsageError")
            @Override
            public void analyze(@NonNull ImageProxy imageProxy) {
                SortedMap<Float,Map<String,SpeciesObject>> results = new TreeMap<>();
                if (imageClassifier != null) {
                    Bitmap bmp = BitmapUtils.getBitmap(imageProxy);
                    // Crop the center square of the frame
                    int minDim = Math.min(bmp.getWidth(), bmp.getHeight());
                    int cropX = (bmp.getWidth() - minDim) / 2;
                    int cropY = (bmp.getHeight() - minDim) / 2;
                    Bitmap croppedBitmap = Bitmap.createBitmap(bmp, cropX, cropY, minDim, minDim);

                    // Resize to expected classifier input size
                    Bitmap rescaledBitmap = Bitmap.createScaledBitmap(
                            croppedBitmap,
                            ImageClassifier.DIM_IMG_SIZE_X,
                            ImageClassifier.DIM_IMG_SIZE_Y,
                            false);
                    bmp.recycle();
                    bmp = rescaledBitmap;
                    List<Prediction> predictions = imageClassifier.classifyFrame(bmp);
                    for (Prediction prediction : predictions) {
                        if (prediction.rank % 10 != 0) {
                            continue;
                        }
                        if (prediction.probability > 0.7) {
                            Map<String, SpeciesObject> map = Taxonomy.predictionToMap(prediction);
                            if (map == null) continue;
                            results.put(prediction.node.rank, map);
                            if (prediction.node.rank <= 50.0f) {
                                overlayView.setResults(results,changeObservationOverlay,realm);
                                result = results;
                            } else{
                                overlayView.setResults(null,changeObservationOverlay,realm);
                                overlayView.clear();
                            }
                        }
                    }
                }
                imageProxy.close();
            }
        });


        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        cameraProvider.unbindAll();
        camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview, imageAnalysis);

    }

    private void getLastPicFromGallery() {
        Uri uriExternal = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = new String[]{
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.Media._ID,
                MediaStore.Images.ImageColumns.DATE_ADDED,
                MediaStore.Images.ImageColumns.MIME_TYPE
        };
        @SuppressLint("Recycle") final Cursor cursor = getContext().getContentResolver()
                .query(uriExternal, projection, null,
                        null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");
        if (cursor.moveToFirst()) {
            int columnIndexID = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            long imageId = cursor.getLong(columnIndexID);
            Uri imageURI = Uri.withAppendedPath(uriExternal, "" + imageId);
            Glide.with(getContext())
                    .load(imageURI)
                    .into(cameraGallery);
        }
    }

    private ArrayList<ImageGalleryData> loadImagesFromGallery() {
        ArrayList<ImageGalleryData> images = new ArrayList<>();
        Uri uriExternal = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = new String[]{
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.DATE_ADDED,
                MediaStore.Images.ImageColumns.MIME_TYPE
        };
        @SuppressLint("Recycle") final Cursor cursor = getContext().getContentResolver()
                .query(uriExternal, projection, null,
                        null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

        int cursor_index_data = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
        while(cursor.moveToNext()){
            ImageGalleryData imageGalleryData = new ImageGalleryData(cursor.getString(cursor_index_data));
            images.add(imageGalleryData);
        }
        return images;
    }

    private void startCamera() {
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {

            }
        }, ContextCompat.getMainExecutor(getContext()));

        getLastPicFromGallery();

    }


    private void getPermission() {
        ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, 101);
    }
}