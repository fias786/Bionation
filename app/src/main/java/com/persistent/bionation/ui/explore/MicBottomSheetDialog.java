package com.persistent.bionation.ui.explore;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.ibm.cloud.sdk.core.security.Authenticator;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneHelper;
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneInputStream;
import com.ibm.watson.developer_cloud.android.library.audio.utils.ContentType;
import com.ibm.watson.speech_to_text.v1.SpeechToText;
import com.ibm.watson.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.speech_to_text.v1.model.RecognizeWithWebsocketsOptions;
import com.ibm.watson.speech_to_text.v1.model.SpeechRecognitionResults;
import com.ibm.watson.speech_to_text.v1.websocket.BaseRecognizeCallback;
import com.ibm.watson.speech_to_text.v1.websocket.RecognizeCallback;
import com.persistent.bionation.R;

import java.io.IOException;
import java.io.InputStream;

public class MicBottomSheetDialog extends BottomSheetDialogFragment {

    private static final String TAG = "MicBottomSheetDialog";
    private SpeechToText speechService;

    private BottomSheetListener bottomSheetListener;
    private TextView micText;
    private TextView tapMicText;
    private ImageButton micBottom;
    RecognizeCallback recognizeCallback;
    private volatile boolean listening = false;
    private volatile boolean running = false;
    MicrophoneHelper microphoneHelper;
    MicrophoneInputStream capture;
    String text="";

    public MicBottomSheetDialog(BottomSheetListener listener){
        this.bottomSheetListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mic_bottom_sheet_layout, container, false);
        micBottom = view.findViewById(R.id.micBottom);
        micText = view.findViewById(R.id.micText);
        tapMicText = view.findViewById(R.id.TapMicText);

        microphoneHelper = new MicrophoneHelper(getActivity());
        speechService = intiSpeechToTextService();
        recognizeCallback = new MicrophoneRecognizeDelegate();
        capture = microphoneHelper.getInputStream(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    speechService.recognizeUsingWebSocket(getRecognizeOption(capture), recognizeCallback);
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tapMicText.setVisibility(View.INVISIBLE);
                            micText.setText("Say something you want");
                        }
                    });
                } catch (Exception e) {
                    Log.d(TAG, "Exception run: " + e.toString());
                }
            }
        }).start();
        running = true;

        micBottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                capture = microphoneHelper.getInputStream(false);
                if(!running || !listening) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                speechService.recognizeUsingWebSocket(getRecognizeOption(capture), recognizeCallback);
                            } catch (Exception e) {
                                Log.d(TAG, "Exception run: " + e.toString());
                            }
                        }
                    }).start();
                    listening = true;
                }else{
                    try {
                        microphoneHelper.closeInputStream();
                        capture.close();
                        listening = false;
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                if(listening){
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tapMicText.setVisibility(View.INVISIBLE);
                            micText.setText("Say something you want");
                        }
                    });
                }
            }
        });

        return view;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        microphoneHelper.closeInputStream();
        try {
            capture.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private SpeechToText intiSpeechToTextService() {

       Authenticator authenticator = new IamAuthenticator.Builder().apikey("").build();
       SpeechToText speech = new SpeechToText(authenticator);
       speech.setServiceUrl("https://api.eu-gb.speech-to-text.watson.cloud.ibm.com/instances/fb1925f3-9b34-4588-a44d-66e540ae48f1");
       return speech;
    }

    public interface BottomSheetListener {
        void onMicClicked(String text);
    }

    @Override
    public void onAttachFragment(@NonNull Fragment childFragment) {
        super.onAttachFragment(childFragment);
        try {
            bottomSheetListener = (BottomSheetListener) childFragment;
        }catch (ClassCastException e){
            throw new ClassCastException(childFragment.toString() + "must implement BottomSheetListener");
        }
    }

    private RecognizeWithWebsocketsOptions getRecognizeOption(InputStream captureStream){
        return new RecognizeWithWebsocketsOptions.Builder()
                .audio(captureStream)
                .contentType(ContentType.RAW.toString())
                .model(RecognizeOptions.Model.EN_IN_TELEPHONY)
                .interimResults(true)
                .inactivityTimeout(3)
                .build();
    }

    private class MicrophoneRecognizeDelegate extends BaseRecognizeCallback implements RecognizeCallback {
        @Override
        public void onTranscription(SpeechRecognitionResults speechResults) {
            super.onTranscription(speechResults);
            if(speechResults.getResults() != null && !speechResults.getResults().isEmpty()){
                text = speechResults.getResults().get(0).getAlternatives().get(0).getTranscript();
                micText.setText(text);
                Log.d(TAG, "onTranscription: "+text);
            }
        }

        @Override
        public void onConnected() {
            super.onConnected();
            Log.d(TAG, "onConnected: ");
        }

        @Override
        public void onListening() {
            super.onListening();
            Log.d(TAG, "onListening: ");
            requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tapMicText.setVisibility(View.INVISIBLE);
                    micText.setText("Say something you want");
                    micBottom.setImageResource(R.drawable.blue_mic_gif);
                }
            });
        }

        @Override
        public void onTranscriptionComplete() {
            super.onTranscriptionComplete();
            Log.d(TAG, "onTranscriptionComplete: ");
        }

        @Override
        public void onError(Exception e) {
            super.onError(e);
            listening = false;
            Log.d(TAG, "onError: "+ e.toString());
            microphoneHelper.closeInputStream();
            try {
                capture.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        @Override
        public void onDisconnected() {
            super.onDisconnected();
            listening = false;
            Log.d(TAG, "onDisconnected: ");
            bottomSheetListener.onMicClicked(text);
            if(!text.equals("")){
                dismiss();
            }else{
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        micText.setText("You can try saying");
                        tapMicText.setVisibility(View.VISIBLE);
                        micBottom.setImageResource(R.drawable.ic_blue_mic_24dp);
                    }
                });
            }
            microphoneHelper.closeInputStream();
            try {
                capture.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onInactivityTimeout(RuntimeException runtimeException) {
            super.onInactivityTimeout(runtimeException);
        }

    }

}
