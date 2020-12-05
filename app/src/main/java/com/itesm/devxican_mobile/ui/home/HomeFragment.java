package com.itesm.devxican_mobile.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.itesm.devxican_mobile.HomeActivity;
import com.itesm.devxican_mobile.R;
import com.kwabenaberko.newsapilib.NewsApiClient;
import com.kwabenaberko.newsapilib.models.Article;
import com.kwabenaberko.newsapilib.models.request.TopHeadlinesRequest;
import com.kwabenaberko.newsapilib.models.response.ArticleResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class HomeFragment extends Fragment {


    String API_KEY = "4fbfe269d3c44865a4abace9678fe3a8"; // ### YOUE NEWS API HERE ###
    String NEWS_SOURCE = "techcrunch"; // Other news source code at: https://newsapi.org/sources
    ListView listNews;
    ProgressBar loader;

    ArrayList<HashMap<String, String>> dataList = new ArrayList<>();
    static final String KEY_AUTHOR = "author";
    static final String KEY_TITLE = "title";
    static final String KEY_DESCRIPTION = "description";
    static final String KEY_URL = "url";
    static final String KEY_URLTOIMAGE = "urlToImage";
    static final String KEY_PUBLISHEDAT = "publishedAt";


    private HomeViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);



        listNews = root.findViewById(R.id.listNews);
        loader = root.findViewById(R.id.loader);
        listNews.setEmptyView(loader);

        loadNews();

        return root;
    }


    private void loadNews() {
        NewsApiClient newsApiClient = new NewsApiClient(API_KEY);

        newsApiClient.getTopHeadlines(
                new TopHeadlinesRequest.Builder()
                        .sources(NEWS_SOURCE)
                        .language("en")
                        .build(),
                new NewsApiClient.ArticlesResponseCallback() {
                    @Override
                    public void onSuccess(ArticleResponse response) {

                        for (int i = 0; i < response.getArticles().size(); i++) {
                            Article news = response.getArticles().get(i);
                            HashMap<String, String> map = new HashMap<>();
                            map.put(KEY_AUTHOR, news.getAuthor());
                            map.put(KEY_TITLE, news.getTitle());
                            map.put(KEY_DESCRIPTION, news.getDescription());
                            map.put(KEY_URL, news.getUrl());
                            map.put(KEY_URLTOIMAGE, news.getUrlToImage());
                            map.put(KEY_PUBLISHEDAT, news.getPublishedAt());
                            dataList.add(map);
                        }


                        NewsAdapter adapter = new NewsAdapter((Activity) getContext(), dataList);
                        listNews.setAdapter(adapter);

                        listNews.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            public void onItemClick(AdapterView<?> parent, View view,
                                                    int position, long id) {
                                Intent i = new Intent(getContext(), DetailNews.class);
                                i.putExtra("url", dataList.get(+position).get(KEY_URL));
                                startActivity(i);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Toast.makeText(getContext(), "Unexpected error", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

}