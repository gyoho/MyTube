package com.example.gyoho.mytube;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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
public class SearchFragment extends Fragment {
    private static final String TAG = "SearchFragment";
    public static final String SEARCH_PAGE = "SEARCH_PAGE";

    private EditText searchInput;
    private ListView videosFound;

    // make handler as a global variable
    private Handler handler;
    private List<VideoItem> searchResults;

    // store unique video ID
    static List<String> favoriteVideoIds = new ArrayList<String>();

    private int mPage;

    public static SearchFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(SEARCH_PAGE, page);
        SearchFragment fragment = new SearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(SEARCH_PAGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_video, container, false);

        // instantiate handler in the constructor for the class in the same thread pools
        // DEFAULT: associates with the Looper for the current thread --> Connected to the UI thread
        handler = new Handler();

        videosFound = (ListView) rootView.findViewById(R.id.videos_found);
        searchInput = (EditText) rootView.findViewById(R.id.search_input);

        searchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    searchOnYoutube(v.getText().toString());
                    return false;
                }
                return true;
            }
        });

        return rootView;
    }

    // Use thread to handle the request
    // Network connection should run separatelly from user interface thread
    private void searchOnYoutube(final String keywords){
        new Thread(){
            public void run(){
                YoutubeConnector yc = new YoutubeConnector(getActivity());
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
        ArrayAdapter<VideoItem> adapter = new ArrayAdapter<VideoItem>(getActivity(), R.layout.video_item, searchResults) {
            // describe the process of converting the Java object to a View
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                if(convertView == null){
                    convertView = getActivity().getLayoutInflater().inflate(R.layout.video_item, parent, false);
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
                Picasso.with(getActivity()).load(searchResult.getThumbnailURL()).into(thumbnail);
                title.setText(searchResult.getTitle());
                publishedDate.setText(searchResult.getPublishedDate());
                viewCount.setText(String.valueOf(searchResult.getViewCount()));
                // the checkbox will be repeated due to View Recycling
                starred.setChecked(false);

                // force it to be unchecked by default and only check it if needed
               if(searchResult.isStarred()) {
                   starred.setChecked(true);
               }

                // set the checkbox to respond to starred event
                starred.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        searchResult.setStarred();
                        favoriteVideoIds.add(searchResult.getId());
                    }
                });
//                Log.d(TAG, String.valueOf(favoriteVideoIds.size()));

                // set the thumbnail to respond to start video
                thumbnail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), PlayerActivity.class);
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
