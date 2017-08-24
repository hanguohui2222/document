package com.demo.amigoactionbar;

import java.util.ArrayList;
import java.util.List;

import amigoui.app.AmigoActionBar;
import amigoui.app.AmigoActivity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.amigo.widgetdemol.R;
import com.demo.amigolistitem.AmigoListItemAdapter;
import com.demo.amigolistitem.ItemData;

public class GnActionBarTabDemo extends AmigoActivity implements
        AmigoActionBar.TabListener {
    ViewPager mViewPager;
    MyPagerAdapter mMypagerAdapter;
    List<View> mSections = new ArrayList<View>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gn_actionbar_tab_demo);

        final AmigoActionBar actionBar = getAmigoActionBar();

        // maxw modify begin
        actionBar.setNavigationMode(AmigoActionBar.NAVIGATION_MODE_TABS); // 设置tab模式
        actionBar.setDisplayShowTitleEnabled(true); // 当tab模式时，此属性为false，则不显示actoinbar，若为true，则同时显示actionbar和tabbar
        actionBar.setDisplayHomeAsUpEnabled(true); // 此属性为true时，显示actionbar左侧返回键，否则不显示
        actionBar.setDisplayShowHomeEnabled(true); // 当同时显示tabbar和actionbar时，若此属性为true，actionbar显示在上面，tabbar显示在下面，若为false，则相反
        // maxw modify end

        // actionBar.setIndicatorBackgroundColor(Color.parseColor("#ffffff"));

        createDummySection();

        mMypagerAdapter = new MyPagerAdapter();
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mMypagerAdapter);
        mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

                actionBar.onPageScrolled(arg0, arg1, arg2);
            }
        });

        for (int i = 0; i < mMypagerAdapter.getCount(); i++) {
            actionBar.addTab(actionBar.newTab().setText("Favorate")
                    .setTabListener(this));
        }
    }

    public class MyPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mSections.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mSections.get(position));
            return mSections.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mSections.get(position));
        }
    }

    private void createDummySection() {
        for (int i = 0; i < 4; i++) {

            View rootView = LayoutInflater.from(this).inflate(
                    R.layout.gn_actionbar_tab_demo_fragment_2, null);
            TextView dummyTextView = (TextView) rootView
                    .findViewById(R.id.section_label);
            dummyTextView.setText("page_" + i);

            ListView listView = (ListView) rootView
                    .findViewById(R.id.section_list);
            initListData();
            if (i == 0) {
                listView.setAdapter(new AmigoListItemAdapter(this, mListData0));
            } else if (i == 1) {
                listView.setAdapter(new AmigoListItemAdapter(this, mListData1));
            } else if (i == 2) {
                listView.setAdapter(new AmigoListItemAdapter(this, mListData2));
            } else {
                listView.setAdapter(new AmigoListItemAdapter(this, mListData3));
            }

            mSections.add(rootView);
        }
    }

    private ArrayList<ItemData> mListData0 = new ArrayList<ItemData>();
    private ArrayList<ItemData> mListData1 = new ArrayList<ItemData>();
    private ArrayList<ItemData> mListData2 = new ArrayList<ItemData>();
    private ArrayList<ItemData> mListData3 = new ArrayList<ItemData>();

    private void initListData() {
        ItemData data10 = new ItemData();
        data10.mType = AmigoListItemAdapter.TYPE_10;
        data10.mFirstSummary = getString(R.string.type10_level1);
        mListData0.add(data10);

        ItemData data11 = new ItemData();
        data11.mType = AmigoListItemAdapter.TYPE_11;
        data11.mFirstSummary = getString(R.string.type11_level1);
        mListData0.add(data11);

        ItemData data12 = new ItemData();
        data12.mType = AmigoListItemAdapter.TYPE_12;
        data12.mFirstSummary = getString(R.string.type12_level1);
        mListData1.add(data12);

        ItemData data20 = new ItemData();
        data20.mType = AmigoListItemAdapter.TYPE_20;
        data20.mFirstSummary = getString(R.string.type20_level1);
        mListData1.add(data20);

        ItemData data21 = new ItemData();
        data21.mType = AmigoListItemAdapter.TYPE_21;
        data21.mFirstSummary = getString(R.string.type21_level1);
        mListData2.add(data21);

        ItemData data22 = new ItemData();
        data22.mType = AmigoListItemAdapter.TYPE_22;
        data22.mFirstSummary = getString(R.string.type22_level1);
        mListData2.add(data22);

        ItemData data23 = new ItemData();
        data23.mType = AmigoListItemAdapter.TYPE_23;
        data23.mFirstSummary = getString(R.string.type23_level1);
        mListData3.add(data23);

        ItemData data30 = new ItemData();
        data30.mType = AmigoListItemAdapter.TYPE_30;
        data30.mFirstSummary = getString(R.string.type30_level1);
        mListData3.add(data30);

    }

    @Override
    public void onTabSelected(amigoui.app.AmigoActionBar.Tab tab,
            FragmentTransaction ft) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(amigoui.app.AmigoActionBar.Tab tab,
            FragmentTransaction ft) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onTabReselected(amigoui.app.AmigoActionBar.Tab tab,
            FragmentTransaction ft) {
        // TODO Auto-generated method stub

    }
}
