package com.persistent.bionation.ui.updates;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kwabenaberko.newsapilib.NewsApiClient;
import com.kwabenaberko.newsapilib.models.Article;
import com.kwabenaberko.newsapilib.models.request.EverythingRequest;
import com.kwabenaberko.newsapilib.models.response.ArticleResponse;
import com.persistent.bionation.R;
import com.persistent.bionation.adapter.ImageRecyclerViewAdapter;
import com.persistent.bionation.adapter.NewsArticlesRecyclerViewAdapter;
import com.persistent.bionation.data.NewsArticlesData;

import java.util.ArrayList;

public class UpdatesFragment extends Fragment {
    private static final String TAG = "UpdatesFragment";

    RecyclerView newsArticlesRecyclerView;
    NewsArticlesRecyclerViewAdapter newsArticlesRecyclerViewAdapter;
    ArrayList<NewsArticlesData> newsArticlesDataArrayList = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_updates, container, false);
        newsArticlesRecyclerView = root.findViewById(R.id.NewsArticlesRecyclerView);
        newsArticlesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        newsArticlesRecyclerView.setHasFixedSize(true);
        newsArticlesRecyclerViewAdapter = new NewsArticlesRecyclerViewAdapter(getContext(),newsArticlesDataArrayList);
        newsArticlesRecyclerView.setAdapter(newsArticlesRecyclerViewAdapter);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NewsApiClient newsApiClient = new NewsApiClient("");
        newsApiClient.getEverything(
                new EverythingRequest.Builder()
                        .q("biodiversity")
                        .build(),
                new NewsApiClient.ArticlesResponseCallback() {
                    @Override
                    public void onSuccess(ArticleResponse response) {
                        for (int i = 0; i < 50 ; i++) {
                            Article article = response.getArticles().get(i);
                            NewsArticlesData newsArticlesData = new NewsArticlesData(article.getUrlToImage(),article.getUrl(),article.getContent()
                            ,article.getAuthor(),article.getDescription(),article.getPublishedAt(),article.getTitle());
                            newsArticlesDataArrayList.add(newsArticlesData);
                            newsArticlesRecyclerViewAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        System.out.println(throwable.getMessage());
                    }
                }
        );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        newsArticlesDataArrayList.clear();
        newsArticlesRecyclerViewAdapter.notifyDataSetChanged();
    }
}