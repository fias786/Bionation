package com.persistent.bionation.ui.camera;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.persistent.bionation.R;
import com.persistent.bionation.adapter.BadgesRecyclerView;
import com.persistent.bionation.adapter.NewsArticlesRecyclerViewAdapter;
import com.persistent.bionation.data.BadgeObservationData;
import com.persistent.bionation.data.BadgesDataObject;
import com.persistent.bionation.data.NewsArticlesData;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class ObservationBadges extends AppCompatActivity {

    TextView observationBadgeCount,badgesNoObservation;
    ImageView observationBadgeIcon,currentBadge;
    RecyclerView badgeRecyclerView;
    BadgesRecyclerView badgesRecyclerViewAdapter;
    ArrayList<BadgeObservationData> badgeObservationDataArrayList = new ArrayList<>();
    Realm realm;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setNavigationBarColor(Color.WHITE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }
        setContentView(R.layout.observation_badges_layout);
        observationBadgeCount = findViewById(R.id.ObservationBadgeCount);
        observationBadgeIcon = findViewById(R.id.ObservationBadgeIcon);
        currentBadge = findViewById(R.id.CurrentBadge);
        badgeRecyclerView = findViewById(R.id.BadgesRecyclerView);
        badgesNoObservation = findViewById(R.id.BadgesNoObservation);
        badgesNoObservation.setVisibility(View.GONE);
        observationBadgeCount.setText("Observation Count: "+SharedPrefs.readSharedSetting(this,"badges"));

        if(SharedPrefs.readSharedSetting(this,"badges") == 0){
            currentBadge.setBackgroundResource(R.drawable.empty_badge);
            observationBadgeCount.setText("Need 1 observation to achieve");
            observationBadgeIcon.setBackgroundResource(R.drawable.bronze_badge);
        }else if(SharedPrefs.readSharedSetting(this,"badges") > 0 && SharedPrefs.readSharedSetting(this,"badges") <= 50){
            currentBadge.setBackgroundResource(R.drawable.bronze_badge);
            observationBadgeCount.setText("Need "+(50-SharedPrefs.readSharedSetting(this,"badges"))+" more observations to achieve");
            observationBadgeIcon.setBackgroundResource(R.drawable.silver_badge);
        }else if(SharedPrefs.readSharedSetting(this,"badges") > 50 && SharedPrefs.readSharedSetting(this,"badges") <= 100){
            currentBadge.setBackgroundResource(R.drawable.silver_badge);
            observationBadgeCount.setText("Need "+(100-SharedPrefs.readSharedSetting(this,"badges"))+" more observations to achieve");
            observationBadgeIcon.setBackgroundResource(R.drawable.gold_badge);
        }else{
            currentBadge.setBackgroundResource(R.drawable.gold_badge);
            observationBadgeCount.setText("You have achieve max observations");
            observationBadgeIcon.setBackgroundResource(R.drawable.gold_badge);
        }

        badgeRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        badgeRecyclerView.setHasFixedSize(true);
        badgesRecyclerViewAdapter = new BadgesRecyclerView(this,badgeObservationDataArrayList);
        badgeRecyclerView.setAdapter(badgesRecyclerViewAdapter);

        realm = Realm.getDefaultInstance();

        RealmResults<BadgesDataObject> realmResults = realm.where(BadgesDataObject.class).findAllAsync();
        if(realmResults!=null){
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

    }
}
