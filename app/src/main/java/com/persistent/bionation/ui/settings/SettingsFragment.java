package com.persistent.bionation.ui.settings;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.persistent.bionation.R;
import com.persistent.bionation.adapter.BadgesRecyclerView;
import com.persistent.bionation.data.BadgeObservationData;
import com.persistent.bionation.data.BadgesDataObject;
import com.persistent.bionation.ui.camera.SharedPrefs;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";

    TextView observationBadgeCount,badgesNoObservation;
    ImageView observationBadgeIcon,currentBadge;
    RecyclerView badgeRecyclerView;
    BadgesRecyclerView badgesRecyclerViewAdapter;
    ArrayList<BadgeObservationData> badgeObservationDataArrayList = new ArrayList<>();
    Realm realm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        Realm.init(getContext());
        Realm.setDefaultConfiguration(new RealmConfiguration.Builder().name("BadgesData").deleteRealmIfMigrationNeeded().allowWritesOnUiThread(true).allowQueriesOnUiThread(true).build());

        observationBadgeCount = root.findViewById(R.id.ObservationBadgeCount);
        observationBadgeIcon = root.findViewById(R.id.ObservationBadgeIcon);
        currentBadge = root.findViewById(R.id.CurrentBadge);
        badgeRecyclerView = root.findViewById(R.id.BadgesRecyclerView);
        badgesNoObservation = root.findViewById(R.id.BadgesNoObservation);
        badgesNoObservation.setVisibility(View.GONE);
        observationBadgeCount.setText("Observation Count: "+ SharedPrefs.readSharedSetting(getContext(),"badges"));

        if(SharedPrefs.readSharedSetting(getContext(),"badges") == 0){
            currentBadge.setBackgroundResource(R.drawable.empty_badge);
            observationBadgeCount.setText("Need 1 observation to achieve");
            observationBadgeIcon.setBackgroundResource(R.drawable.bronze_badge);
        }else if(SharedPrefs.readSharedSetting(getContext(),"badges") > 0 && SharedPrefs.readSharedSetting(getContext(),"badges") <= 50){
            currentBadge.setBackgroundResource(R.drawable.bronze_badge);
            observationBadgeCount.setText("Need "+(50-SharedPrefs.readSharedSetting(getContext(),"badges"))+" more observations to achieve");
            observationBadgeIcon.setBackgroundResource(R.drawable.silver_badge);
        }else if(SharedPrefs.readSharedSetting(getContext(),"badges") > 50 && SharedPrefs.readSharedSetting(getContext(),"badges") <= 100){
            currentBadge.setBackgroundResource(R.drawable.silver_badge);
            observationBadgeCount.setText("Need "+(100-SharedPrefs.readSharedSetting(getContext(),"badges"))+" more observations to achieve");
            observationBadgeIcon.setBackgroundResource(R.drawable.gold_badge);
        }else{
            currentBadge.setBackgroundResource(R.drawable.gold_badge);
            observationBadgeCount.setText("You have achieve max observations");
            observationBadgeIcon.setBackgroundResource(R.drawable.gold_badge);
        }

        badgeRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        badgeRecyclerView.setHasFixedSize(true);
        badgesRecyclerViewAdapter = new BadgesRecyclerView(getContext(),badgeObservationDataArrayList);
        badgeRecyclerView.setAdapter(badgesRecyclerViewAdapter);

        realm = Realm.getDefaultInstance();

        RealmResults<BadgesDataObject> realmResults = realm.where(BadgesDataObject.class).findAllAsync();
        Log.d(TAG, "onCreateView: "+ realmResults.toArray().length);
        if(realmResults!=null && realmResults.toArray().length>0){
            for(BadgesDataObject badgesDataObject : realmResults){
                BadgeObservationData badgeObservationData = new BadgeObservationData(badgesDataObject.imageUrl,
                        badgesDataObject.scientific_name,badgesDataObject.time,
                        badgesDataObject.commonName,badgesDataObject.isThreatened,badgesDataObject.observeCount);
                badgeObservationDataArrayList.add(badgeObservationData);
                badgesRecyclerViewAdapter.notifyDataSetChanged();
            }
        }else{
            badgesNoObservation.setVisibility(View.VISIBLE);
        }

        return root;
    }
}
