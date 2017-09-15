package com.gionee.hotspottransmission.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import java.util.ArrayList;
import com.gionee.hotspottransmission.R;
/**
 * Created by luorw on 5/23/17.
 */
public class TransferPagerAdapter extends FragmentPagerAdapter{

    public ArrayList<Fragment> mFragmentList = null;
    private String[] tabTitles;
    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }

    public TransferPagerAdapter(Context context, FragmentManager mFragmentManager, ArrayList<Fragment> fragmentList) {
        super(mFragmentManager);
        mFragmentList = fragmentList;
        tabTitles = context.getResources().getStringArray(R.array.transfer_tab);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }


    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;//返回这个表示该对象已改变,需要刷新
    }
}
