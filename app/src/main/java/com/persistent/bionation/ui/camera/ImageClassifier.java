package com.persistent.bionation.ui.camera;

import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.FileUtils;
import android.os.SystemClock;
import android.util.Log;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.nnapi.NnApiDelegate;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Classifies images with Tensorflow Lite. */
public class ImageClassifier {

    /** Tag for the {@link Log}. */
    private static final String TAG = "ImageClassifier";

    /** Dimensions of inputs. */
    private static final int DIM_BATCH_SIZE = 1;

    private static final int DIM_PIXEL_SIZE = 3;

    public static final int DIM_IMG_SIZE_X = 299;
    public static final int DIM_IMG_SIZE_Y = 299;

    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128.0f;

    private final Taxonomy mTaxonomy;
    private final FileDescriptor mModelFilename;
    private final InputStream mTaxonomyFilename;
    private final String mModelVersion;
    private int mModelSize;
    private AssetFileDescriptor assetFileDescriptor;

    /* Preallocated buffers for storing image data in. */
    private int[] intValues = new int[DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y];

    /** An instance of the driver class to run model inference with Tensorflow Lite. */
    private Interpreter mTFlite;

    /** A ByteBuffer to hold image data, to be feed into Tensorflow Lite as inputs. */
    private ByteBuffer imgData;

    public void setFilterByTaxonId(Integer taxonId) {
        mTaxonomy.setFilterByTaxonId(taxonId);
    }

    public Integer getFilterByTaxonId() {
        return mTaxonomy.getFilterByTaxonId();
    }

    public void setNegativeFilter(boolean negative) {
        mTaxonomy.setNegativeFilter(negative);
    }

    public boolean getNegativeFilter() {
        return mTaxonomy.getNegativeFilter();
    }


    /** Initializes an {@code ImageClassifier}. */
    public ImageClassifier(AssetFileDescriptor modelPath, InputStream taxonomyPath, String version) throws IOException {
        mModelFilename = modelPath.getFileDescriptor();
        mTaxonomyFilename = taxonomyPath;
        this.assetFileDescriptor= modelPath;
        mModelVersion = version;
        mTFlite = new Interpreter(loadModelFile());
        imgData =
                ByteBuffer.allocateDirect(
                        4 * DIM_BATCH_SIZE * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE);
        imgData.order(ByteOrder.nativeOrder());
        //Log.d(TAG, "Created a Tensorflow Lite Image Classifier.");

        mTaxonomy = new Taxonomy(mTaxonomyFilename, mModelVersion);
        mModelSize = mTaxonomy.getModelSize();
    }

    /** Classifies a frame from the preview stream. */
    public List<Prediction> classifyFrame(Bitmap bitmap) {
        if (mTFlite == null) {
            //Log.d(TAG, "Image classifier has not been initialized; Skipped.");
            return null;
        }
        if (bitmap == null) {
            //Log.d(TAG, "Null input bitmap");
            return null;
        }

        long startTime = SystemClock.uptimeMillis();
        convertBitmapToByteBuffer(bitmap);

        byte[] arr = new byte[imgData.remaining()];
        imgData.get(arr);

        Map<Integer, Object> expectedOutputs = new HashMap<>();
        for (int i = 0; i < 1; i++) {
            expectedOutputs.put(i, new float[1][mModelSize]);
        }

        Object[] input = { imgData };
        List<Prediction> predictions = null;
        try {
            mTFlite.runForMultipleInputsOutputs(input, expectedOutputs);
            predictions = mTaxonomy.predict(expectedOutputs);
        } catch (Exception exc) {
            exc.printStackTrace();
            return new ArrayList<Prediction>();
        } catch (OutOfMemoryError exc) {
            exc.printStackTrace();
            return new ArrayList<Prediction>();
        }
        long endTime = SystemClock.uptimeMillis();

        return predictions;
    }

    /** Closes tflite to release resources. */
    public void close() {
        mTFlite.close();
        mTFlite = null;
    }

    /** Memory-map the model file in Assets. */
    private MappedByteBuffer loadModelFile() throws IOException {
        FileInputStream inputStream = new FileInputStream(mModelFilename);
        FileChannel fileChannel = inputStream.getChannel();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
    }

    /** Writes Image data into a {@code ByteBuffer}. */
    private void convertBitmapToByteBuffer(Bitmap bitmap) {
        if (imgData == null) {
            return;
        }
        imgData.rewind();
        long startTime = SystemClock.uptimeMillis();

        // Convert pixel values to be float from 0 to 1
        float[][][][] input = new float[1][ImageClassifier.DIM_IMG_SIZE_X][ImageClassifier.DIM_IMG_SIZE_Y][3];
        for (int x = 0; x < ImageClassifier.DIM_IMG_SIZE_X; x++) {
            for (int y = 0; y < ImageClassifier.DIM_IMG_SIZE_Y; y++) {
                int pixel = bitmap.getPixel(x, y);
                // TODO: rephrase to check for 1.0 version and have 2 as else
                if (mModelVersion.equals("2.3") || mModelVersion.equals("2.4")) {
                    input[0][x][y][0] = Color.red(pixel);
                    input[0][x][y][1] = Color.green(pixel);
                    input[0][x][y][2] = Color.blue(pixel);
                } else {
                    // Normalize channel values to [0.0, 1.0] for version 1.0
                    input[0][x][y][0] = Color.red(pixel) / 255.0f;
                    input[0][x][y][1] = Color.green(pixel) / 255.0f;
                    input[0][x][y][2] = Color.blue(pixel) / 255.0f;
                }
            }
        }
        // Convert to ByteBuffer
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * input.length * input[0].length * input[0][0].length * input[0][0][0].length);
            byteBuffer.order(ByteOrder.nativeOrder());
            for (int i = 0; i < input.length; i++) {
                for (int j = 0; j < input[0].length; j++) {
                    for (int k = 0; k < input[0][0].length; k++) {
                        for (int l = 0; l < input[0][0][0].length; l++) {
                            byteBuffer.putFloat(input[i][j][k][l]);
                        }
                    }
                }
            }
            byteBuffer.rewind();
            imgData.put(byteBuffer);
            long endTime = SystemClock.uptimeMillis();
            //Log.d(TAG, "Timecost to put values into ByteBuffer: " + Long.toString(endTime - startTime));
        } catch (BufferOverflowException exc) {
            //Log.d(TAG, "Exception while converting to byte buffer: " + exc);
        }
    }

}
