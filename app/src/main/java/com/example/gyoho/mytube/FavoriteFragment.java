package com.example.gyoho.mytube;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by gyoho on 10/17/15.
 */
public class FavoriteFragment extends Fragment implements UpdateableFragment{
    private static final String TAG = "FavoriteFragment";
    public static final String FAVORITE_PAGE = "FAVORITE_PAGE";

    private ListView videosFound;

    private Handler handler;
    private List<VideoItem> searchResults;

    private int mPage;

    public static FavoriteFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(FAVORITE_PAGE, page);
        FavoriteFragment fragment = new FavoriteFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void update(){}


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(FAVORITE_PAGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_favorite, container, false);

        videosFound = (ListView) rootView.findViewById(R.id.videos_found);

        handler = new Handler();
//        Log.d(TAG, "Favorite list contents: " + SearchFragment.favoriteVideoIds);
        if(!SearchFragment.favoriteVideoIds.isEmpty()) {
            displayFavoriteList();
        }

        return rootView;
    }


    private void displayFavoriteList(){
        new Thread(){
            public void run(){
                YoutubeConnector yc = new YoutubeConnector(getActivity());
                searchResults = yc.getFavoriteVideo(SearchFragment.favoriteVideoIds);
                handler.post(new Runnable() {
                    public void run() {
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
        ArrayAdapter<VideoItem> adapter = new ArrayAdapter<VideoItem>(getActivity(), R.layout.favorite_item, searchResults) {
            // describe the process of converting the Java object to a View
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                if(convertView == null){
                    convertView = getActivity().getLayoutInflater().inflate(R.layout.favorite_item, parent, false);
                }

                // associate the layout items attributes with the array data
                ImageView thumbnail = (ImageView)convertView.findViewById(R.id.video_thumbnail);
                TextView title = (TextView)convertView.findViewById(R.id.video_title);
                TextView publishedDate = (TextView)convertView.findViewById(R.id.video_publish_date);
                TextView viewCount = (TextView)convertView.findViewById(R.id.video_view_count);

                // get an item in a certain position(= index)
                final VideoItem searchResult = searchResults.get(position);

                // set the layout values
                Picasso.with(getActivity()).load(searchResult.getThumbnailURL()).into(thumbnail);
                title.setText(searchResult.getTitle());
                publishedDate.setText(searchResult.getPublishedDate());
                viewCount.setText(String.valueOf(searchResult.getViewCount()));

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
