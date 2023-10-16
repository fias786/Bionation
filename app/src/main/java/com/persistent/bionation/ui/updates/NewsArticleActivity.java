package com.persistent.bionation.ui.updates;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.persistent.bionation.R;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class NewsArticleActivity extends AppCompatActivity {

    TextView newsTitleTextView,newsDescriptionTextView,newsTimeTextView,newsContentTextView;
    ImageView newsImageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setNavigationBarColor(Color.WHITE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }
        setContentView(R.layout.news_article_activity);
        newsTitleTextView = findViewById(R.id.newsTitle);
        newsDescriptionTextView = findViewById(R.id.newsDescription);
        newsTimeTextView = findViewById(R.id.newsTime);
        newsImageView = findViewById(R.id.newsImage);
        newsContentTextView = findViewById(R.id.newsContent);

        String newsTitle = getIntent().getExtras().get("newsTitle").toString();
        String newsDescription = getIntent().getExtras().get("newsDescription").toString();
        String newsTime = getIntent().getExtras().get("newsTime").toString();
        String newsUrlImage = getIntent().getExtras().get("newsUrlImage").toString();
        String newsContent = getIntent().getExtras().get("newsContent").toString();

        newsTitleTextView.setText(newsTitle);
        newsDescriptionTextView.setText(newsDescription);
        newsTimeTextView.setText(newsTime);
        Glide.with(this).load(newsUrlImage).placeholder(R.drawable.image_spinner).into(newsImageView);
        newsContentTextView.setText(newsContent);
    }
}
