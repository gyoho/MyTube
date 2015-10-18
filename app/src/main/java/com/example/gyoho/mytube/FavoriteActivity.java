package com.example.gyoho.mytube;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by gyoho on 10/17/15.
 */
public class FavoriteActivity extends Activity {
    private static final String TAG = "FavoriteActivity";

    private ListView videosFound;

    private Handler handler;
    private List<VideoItem> searchResults;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        setContentView(R.layout.activity_video);

        // enable UP button
        getActionBar().setDisplayHomeAsUpEnabled(true);

        videosFound = (ListView)findViewById(R.id.videos_found);
        handler = new Handler();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }


    private void displayFavoriteList(final String keywords){
        new Thread(){
            public void run(){
                YoutubeConnector yc = new YoutubeConnector(FavoriteActivity.this);

                searchResults = yc.getFavoriteVideo(SearchActivity.favoriteVideoIds);
                handler.post(new Runnable(){
                    public void run(){
                        // add the task to the message queue
                        updateVideosFound();
                    }
                });
            }
        }.start();
    }

    // Generate an ArrayAdapter and pass it on to the ListView to display the search results.
    protected void updateVideosFound(){
        // create anonymous class by extending the basic ArrayAdapter
        ArrayAdapter<VideoItem> adapter = new ArrayAdapter<VideoItem>(getApplicationContext(), R.layout.favorite_item, searchResults) {
            // describe the process of converting the Java object to a View
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                if(convertView == null){
                    convertView = getLayoutInflater().inflate(R.layout.video_item, parent, false);
                }

                // associate the layout items attributes with the array data
                ImageView thumbnail = (ImageView)convertView.findViewById(R.id.video_thumbnail);
                TextView title = (TextView)convertView.findViewById(R.id.video_title);
                TextView publishedDate = (TextView)convertView.findViewById(R.id.video_publish_date);
                TextView viewCount = (TextView)convertView.findViewById(R.id.video_view_count);

                // get an item in a certain position(= index)
                final VideoItem searchResult = searchResults.get(position);

                // set the layout values
                Picasso.with(getApplicationContext()).load(searchResult.getThumbnailURL()).into(thumbnail);
                title.setText(searchResult.getTitle());
                publishedDate.setText(searchResult.getPublishedDate());
                viewCount.setText(String.valueOf(searchResult.getViewCount()));

                // set the thumbnail to respond to start video
                thumbnail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
                        intent.putExtra("VIDEO_ID", searchResults.get(position).getId());
                        // increment the view count
                        searchResults.get(position).incrementViewCount();

                        startActivity(intent);
                    }
                });

                // return the set view object
                return convertView;
            }
        };

        videosFound.setAdapter(adapter);
    }
}
