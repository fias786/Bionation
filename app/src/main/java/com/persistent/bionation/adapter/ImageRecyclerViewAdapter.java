package com.persistent.bionation.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.persistent.bionation.R;
import com.persistent.bionation.data.ImageGalleryData;
import com.persistent.bionation.data.ObservationImageData;
import com.persistent.bionation.ui.camera.BitmapUtils;
import com.persistent.bionation.ui.camera.CameraFragment;
import com.persistent.bionation.ui.camera.ImageClassifier;
import com.persistent.bionation.ui.camera.Observation;
import com.persistent.bionation.ui.camera.ObservationItems;
import com.persistent.bionation.ui.camera.Prediction;
import com.persistent.bionation.ui.camera.SpeciesObject;
import com.persistent.bionation.ui.camera.Taxonomy;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ImageRecyclerViewAdapter extends RecyclerView.Adapter<ImageRecyclerViewAdapter.ViewHolder> {

    private static final String API_URL = "https://api.inaturalist.org/v1/" ;

    private Context context;
    private FragmentActivity activity;
    private ArrayList<ImageGalleryData> data;
    private BottomSheetBehavior imageBottomSheetBehaviour;
    private BottomSheetBehavior bottomSheetBehavior;
    CameraFragment.Observations observations;
    Observation observationResult;
    TextView observationCommonNameTextView,observationIsThreatened;
    private ObservationImageRecyclerView observationImageAdapter;
    private ArrayList<ObservationImageData> loadObservationImages;

    public ImageRecyclerViewAdapter(Context context, FragmentActivity activity, TextView observationCommonNameTextView,
                                    TextView observationIsThreatened, ObservationImageRecyclerView observationImageAdapter,ArrayList<ObservationImageData> loadObservationImages,
                                    BottomSheetBehavior bottomSheetBehavior, BottomSheetBehavior imageBottomSheetBehaviour,
                                    ArrayList<ImageGalleryData> data) {
        this.context = context;
        this.activity = activity;
        this.observationCommonNameTextView = observationCommonNameTextView;
        this.observationIsThreatened = observationIsThreatened;
        this.observationImageAdapter = observationImageAdapter;
        this.loadObservationImages = loadObservationImages;
        this.data = data;
        this.bottomSheetBehavior = bottomSheetBehavior;
        this.imageBottomSheetBehaviour = imageBottomSheetBehaviour;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_image_layout,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(context).load(data.get(position).getImage()).into(holder.galleryImage);
        holder.galleryImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadObservationImages.clear();
                observationImageAdapter.notifyDataSetChanged();
                ImageClassifier imageClassifier;
                try {
                    AssetFileDescriptor model = context.getAssets().openFd("model.tflite");
                    InputStream label = context.getAssets().open("labels.csv");
                    imageClassifier = new ImageClassifier(model,label,"1.0");

                    //Toast.makeText(context,""+position,Toast.LENGTH_SHORT).show();
                    Bitmap bitmap = BitmapUtils.getBitmapFromContentUri(context.getContentResolver(),Uri.fromFile(new File(data.get(position).getImage())));
                    if (imageClassifier != null) {
                        @SuppressLint("UnsafeOptInUsageError") Bitmap bmp = bitmap;
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
                        Map<String, SpeciesObject> map=null;
                        for (Prediction prediction : predictions) {
                            if (prediction.rank % 10 != 0) {
                                continue;
                            }
                            if (prediction.probability > 0.7) {
                                map = Taxonomy.predictionToMap(prediction);
                                if (map == null) continue;
                                Log.d("Main", "onClick: "+map.toString());
                                if (prediction.node.rank == 10.0f) {
                                    imageBottomSheetBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
                                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                                    Retrofit retrofit =
                                            new Retrofit.Builder()
                                                    .baseUrl(API_URL)
                                                    .addConverterFactory(GsonConverterFactory.create())
                                                    .build();

                                    observations = retrofit.create(CameraFragment.Observations.class);

                                    Call<Observation> call = observations.observations(true,map.get("species").taxon_id,"30","1");
                                    call.enqueue(new Callback<Observation>() {
                                        @Override
                                        public void onResponse(Call<Observation> call, retrofit2.Response<Observation> response) {
                                            observationResult = response.body();
                                            activity.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    ObservationItems resultObservation = observationResult.observationItems.get(0);
                                                    observationCommonNameTextView.setText(resultObservation.taxon.scientificName);
                                                    if (resultObservation.taxon.isThreatened.equals("true")) {
                                                        observationIsThreatened.setText("Species Threatened: Yes");
                                                    } else {
                                                        observationIsThreatened.setText("Species Threatened: No");
                                                    }
                                                    List<String> imageUrls = new ArrayList<>();
                                                    for (int i = 0; i < observationResult.observationItems.size(); i++) {
                                                        for (int j = 0; j < observationResult.observationItems.get(i).photosList.size(); j++) {
                                                            String squareUrl = observationResult.observationItems.get(i).photosList.get(j).url;
                                                            String mediumUrl = squareUrl.split("square")[0] + "medium" + squareUrl.split("square")[1];
                                                            imageUrls.add(mediumUrl);
                                                        }
                                                    }
                                                    for (int i = 0; i < imageUrls.size(); i = i + 3) {
                                                        ObservationImageData observationImageData = new ObservationImageData(
                                                                imageUrls.get((i) % imageUrls.size()),
                                                                imageUrls.get((i + 1) % imageUrls.size()),
                                                                imageUrls.get((i + 2) % imageUrls.size()));
                                                        loadObservationImages.add(observationImageData);
                                                        observationImageAdapter.notifyDataSetChanged();
                                                    }

                                                }
                                            });
                                        }

                                        @Override
                                        public void onFailure(Call<Observation> call, Throwable t) {

                                        }

                                    });
                                }
                            }
                        }
                        if(map==null || map.get("species")==null){
                            Toast.makeText(context,"Species Not Detected",Toast.LENGTH_SHORT).show();
                            assert map != null;
                            map.clear();
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView galleryImage;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            galleryImage = itemView.findViewById(R.id.GalleryImage);
        }
    }
}
