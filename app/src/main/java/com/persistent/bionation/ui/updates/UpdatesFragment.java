package com.persistent.bionation.ui.updates;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import retrofit2.Retrofit;

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
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onSuccess(ArticleResponse response) {
                        for (int i = 0; i < 50 ; i++) {
                            Article article = response.getArticles().get(i);
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
                            LocalDateTime date = LocalDateTime.parse(article.getPublishedAt(), formatter);
                            String time="";
                            if(date.getHour()>12){
                                time = date.getHour()-12 + ":"+date.getMinute()+" pm";
                            }else{
                                time = date.getHour() + ":"+date.getMinute()+" am";
                            }

                            NewsArticlesData newsArticlesData = new NewsArticlesData(article.getUrlToImage(),article.getUrl(),article.getContent()
                            ,article.getAuthor(),article.getDescription(),date.getMonth() + " " +date.getDayOfMonth()+", "+date.getYear()+ " at "+time,article.getTitle());
                            newsArticlesDataArrayList.add(newsArticlesData);
                            newsArticlesRecyclerViewAdapter.notifyDataSetChanged();
                        }
                        Article article = response.getArticles().get(0);

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                OkHttpClient client = new OkHttpClient();
                                Request request = new Request.Builder()
                                        .url(article.getUrl())
                                        .build();

                                try {
                                    Response response = client.newCall(request).execute();
                                    String responseBody = response.body().string();
                                    //String responseBody = "<html><body>Hi Everyone</body></html>";

                                    if(!responseBody.equals("")) {

                                        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                                        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                                        Document doc = dBuilder.parse(new ByteArrayInputStream(responseBody.getBytes
                                                (Charset.forName("UTF-8"))));

                                        Element element = doc.getDocumentElement();
                                        element.normalize();

                                        NodeList nList = element.getElementsByTagName("article");
                                        for (int i = 0; i < nList.getLength(); i++) {
                                            Node node = nList.item(i);
                                            if (node.getNodeType() == Node.ELEMENT_NODE) {
                                                Element element2 = (Element) node;
                                                Log.d(TAG, "run: " + element.getTextContent());
                                            }
                                        }
                                        Log.d(TAG, "run: " + element.getTextContent());
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        });
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