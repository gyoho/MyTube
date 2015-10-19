package com.example.gyoho.mytube;

import android.content.Context;
import android.util.Log;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Joiner;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gyoho on 10/15/15.
 */
public class YoutubeConnector {
    private static final String TAG = "YouTubeConnector";

    private YouTube youtube;
    private YouTube.Search.List query;

    // Your developer key goes here
    protected static final String KEY1 = "AIzaSyAWTGqRmW_8wiRYTndIrtTgPucEhDGkF84";
    protected static final String KEY2 = "AIzaSyC2tY9H2NbzrYz-cT1W0JJzeh0fkWTTWDM";
    private static final long NUMBER_OF_VIDEOS_RETURNED = 25;

    public YoutubeConnector(Context content) {
        youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest hr) throws IOException {}
        }).setApplicationName(content.getString(R.string.app_name)).build();

        try{
            query = youtube.search().list("id,snippet");
            query.setKey(KEY1);
            query.setType("video");
            query.setFields("items(id/videoId,snippet/title,snippet/publishedAt,snippet/description,snippet/thumbnails/default/url)");
            query.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
        } catch(IOException e){
            Log.d(TAG, "Could not initialize: " + e);
        }
    }

    public List<VideoItem> search(String keywords){
        query.setQ(keywords);
        try{
            SearchListResponse response = query.execute();
            List<SearchResult> results = response.getItems();

            List<VideoItem> items = new ArrayList<VideoItem>();
            for(SearchResult result:results){
                VideoItem item = new VideoItem();
                item.setTitle(result.getSnippet().getTitle());
                try {
                    item.setPublishedDate(new SimpleDateFormat("MM/dd/yyy").format(
                            (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                                    .parse(result.getSnippet().getPublishedAt().toString()))));
                } catch (ParseException e) {
                    Log.d(TAG, "Could parse the date: " + e);
                }
                item.setDescription(result.getSnippet().getDescription());
                item.setThumbnailURL(result.getSnippet().getThumbnails().getDefault().getUrl());
                item.setId(result.getId().getVideoId());
                items.add(item);
            }
            return items;
        }catch(IOException e){
            Log.d(TAG, "Could not search: "+ e);
            return null;
        }
    }

    public List<VideoItem> getFavoriteVideo(List<String> favoriteVideoIds){
        Joiner stringJoiner = Joiner.on(',');
        String videoId = stringJoiner.join(favoriteVideoIds);

        Log.d(TAG, "String video ID: " + videoId);

        // Call the YouTube Data API's youtube.videos.list method to
        // retrieve the resources that represent the specified videos
        YouTube.Videos.List listVideosRequest = null;
        try {
            listVideosRequest = youtube.videos().list("snippet, recordingDetails").setId(videoId);
            listVideosRequest.setKey(KEY2);
            VideoListResponse listResponse = listVideosRequest.execute();

            List<Video> videoList = listResponse.getItems();

            List<VideoItem> items = new ArrayList<VideoItem>();
            for(Video video:videoList){
                VideoItem item = new VideoItem();
                item.setTitle(video.getSnippet().getTitle());
                try {
                    item.setPublishedDate(new SimpleDateFormat("MM/dd/yyyy").format(
                            (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                                    .parse(video.getSnippet().getPublishedAt().toString()))));
                } catch (ParseException e) {
                    Log.d(TAG, "Could parse the date: " + e);
                }
                item.setDescription(video.getSnippet().getDescription());
                item.setThumbnailURL(video.getSnippet().getThumbnails().getDefault().getUrl());
                // same as id in search method
                item.setId(video.getId());
                items.add(item);
            }
            return items;
        } catch(IOException e){
            Log.d(TAG, "Could not search: " + e);
            return null;
        }
    }

//    private <T extends com.google.api.services.youtube.model> List<VideoItem> generateVideoItemList(List<T> list) {
//        List<VideoItem> items = new ArrayList<VideoItem>();
//        for ( T element : list ){
//            VideoItem item = new VideoItem();
//            item.setTitle(element.getSnippet().getTitle());
//            item.setPublishedDate(result.getSnippet().getPublishedAt().toString());
//            item.setDescription(element.getSnippet().getDescription());
//            item.setThumbnailURL(element.getSnippet().getThumbnails().getDefault().getUrl());
//            items.add(item);
//        }
//
//        return items;
//    }
}
