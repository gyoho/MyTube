package com.example.gyoho.mytube;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

/**
 * Created by gyoho on 10/18/15.
 */
public class MyTubeFragmentPagerAdapter extends FragmentStatePagerAdapter {
    private static final String TAG = "FragmentPagerAdapter";
    final int PAGE_COUNT = 2;
    private String tabTitles[] = new String[] { "Search", "Favorite"};
    private Context context;

    public MyTubeFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        switch(position) {
            case 0:
                return SearchFragment.newInstance(position + 1);
            case 1:
                return FavoriteFragment.newInstance(position + 1);
            default:
                return null;
        }
    }

    @Override
    public int getItemPosition(Object item) {
        if (item instanceof UpdateableFragment) {
           return POSITION_NONE;
        }
        //don't return POSITION_NONE, avoid fragment recreation.
        return POSITION_UNCHANGED;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}
