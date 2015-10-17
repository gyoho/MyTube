package com.example.gyoho.mytube;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
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
    List<String> favoriteVideoIds = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

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

        addClickListener();
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
    private void updateVideosFound(){
        // create anonymous class by extending the basic ArrayAdapter
        ArrayAdapter<VideoItem> adapter = new ArrayAdapter<VideoItem>(getApplicationContext(), R.layout.video_item, searchResults) {
            // describe the process of converting the Java object to a View
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null){
                    convertView = getLayoutInflater().inflate(R.layout.video_item, parent, false);
                }

                // associate the layout items attributes with the array data
                ImageView thumbnail = (ImageView)convertView.findViewById(R.id.video_thumbnail);
                TextView title = (TextView)convertView.findViewById(R.id.video_title);
                TextView viewCount = (TextView)convertView.findViewById(R.id.video_view_count);
                CheckBox starred = (CheckBox)convertView.findViewById(R.id.star_icon);

                // get an item in a certain position(= index)
                VideoItem searchResult = searchResults.get(position);

                // set the layout values
                Picasso.with(getApplicationContext()).load(searchResult.getThumbnailURL()).into(thumbnail);
                title.setText(searchResult.getTitle());
//                viewCount.setText(String.valueOf(searchResult.getViewCount()));

                // set the checkbox to respond to starred event
//                starred.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        favoriteVideoIds.add(searchResult.getId());
//                    }
//                });
//                Log.d(TAG, String.valueOf(favoriteVideoIds.size()));

                // return the set view object
                return convertView;
            }
        };

        videosFound.setAdapter(adapter);
    }

    private void addClickListener(){
        // attach click listener to each item on the videoFound ListView
        videosFound.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
                Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
                intent.putExtra("VIDEO_ID", searchResults.get(pos).getId());
                // increment the view count
//                searchResults.get(pos).incrementViewCount();

                startActivity(intent);
            }
        });
    }
}
