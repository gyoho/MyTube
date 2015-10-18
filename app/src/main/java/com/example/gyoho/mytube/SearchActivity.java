package com.example.gyoho.mytube;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gyoho on 10/15/15.
 */
public class SearchActivity extends Activity {
    private static final String TAG = "SearchActivity";

    private EditText searchInput;
    private ListView videosFound;

    // make handler as a global variable
    private Handler handler;
    private List<VideoItem> searchResults;

    // store unique video ID
    static List<String> favoriteVideoIds = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video);

        // enable UP button
        ActionBar actionBar = getActionBar();
        actionBar.show();

        searchInput = (EditText)findViewById(R.id.search_input);
        videosFound = (ListView)findViewById(R.id.videos_found);

        // instantiate handler in the constructor for the class in the same thread pools
        // DEFAULT: associates with the Looper for the current thread --> Connected to the UI thread
        handler = new Handler();

        searchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    searchOnYoutube(v.getText().toString());
                    return false;
                }
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Use thread to handle the request
    // Network connection should run separatelly from user interface thread
    private void searchOnYoutube(final String keywords){
        new Thread(){
            public void run(){
                YoutubeConnector yc = new YoutubeConnector(SearchActivity.this);
                // the lib uses thread to execute this task
                searchResults = yc.search(keywords);
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
        ArrayAdapter<VideoItem> adapter = new ArrayAdapter<VideoItem>(getApplicationContext(), R.layout.video_item, searchResults) {
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
                CheckBox starred = (CheckBox)convertView.findViewById(R.id.star_icon);

                // get an item in a certain position(= index)
                final VideoItem searchResult = searchResults.get(position);

                // set the layout values
                Picasso.with(getApplicationContext()).load(searchResult.getThumbnailURL()).into(thumbnail);
                title.setText(searchResult.getTitle());
                publishedDate.setText(searchResult.getPublishedDate());
                viewCount.setText(String.valueOf(searchResult.getViewCount()));

                // set the checkbox to respond to starred event
                starred.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        favoriteVideoIds.add(searchResult.getId());
                    }
                });
//                Log.d(TAG, String.valueOf(favoriteVideoIds.size()));

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
