package com.demo.adapter;

import java.util.List;

import android.app.Fragment;
import android.app.FragmentManager;
import com.demo.ui.GnMyFragment;
import android.support.v13.app.FragmentPagerAdapter;

public class GnFragAdapter extends FragmentPagerAdapter {
    
    private List<Fragment> fragments ;

    public GnFragAdapter(FragmentManager fm){
        super(fm);
    }
    
    public GnFragAdapter(FragmentManager fm,List<Fragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    /**
     * add the fragment to the special position
     * @param location the position be added to.
     * @param fragment
     */
    public void addFragment(int location,Fragment fragment){
        this.fragments.add(location, fragment);
        this.notifyDataSetChanged();
    }
    /**
     * add the fragment to the default position.the end of the list.
     * @param fragment
     */
    public void addFragment(Fragment fragment){
        this.fragments.add(fragment);
        this.notifyDataSetChanged();
    }
}
