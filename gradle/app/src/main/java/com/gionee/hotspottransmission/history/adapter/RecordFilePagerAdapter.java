package com.gionee.hotspottransmission.history.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;

import com.gionee.hotspottransmission.adapter.FragmentPagerAdapter;
import com.gionee.hotspottransmission.R;

import java.util.ArrayList;

public class RecordFilePagerAdapter extends FragmentPagerAdapter {

    public ArrayList<Fragment> mFragmentList = null;
    private String[] recordTabTitles;

    public RecordFilePagerAdapter(Context context,FragmentManager fragmentManager, ArrayList<Fragment> fragmentList) {
        super(fragmentManager);
        mFragmentList = fragmentList;
        recordTabTitles = context.getResources().getStringArray(R.array.records_tab);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return recordTabTitles[position];
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return recordTabTitles.length;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}
