package com.demo.amigoactionbar;

import java.util.ArrayList;
import java.util.List;

import amigoui.app.AmigoActionBar;
import amigoui.app.AmigoActivity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.amigo.widgetdemol.R;

public class GnActionBarOvlayDemo extends AmigoActivity  implements AmigoActionBar.TabListener {
	ViewPager mViewPager;
	MyPagerAdapter mMypagerAdapter;
	List<View> mSections = new ArrayList<View>();
    AmigoActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // AmigoActionBar会浮在所有布局的最顶层
        // 1、在theme中：<item name="android:windowActionBarOverlay">true</item>
        // setTheme(R.style.DemoTheme);

        // 2、代码实现
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gn_actionbar_overlay_demo);

        mActionBar = getAmigoActionBar();
        mActionBar.setNavigationMode(AmigoActionBar.NAVIGATION_MODE_TABS);
        //mActionBar.setIndicatorBackgroundColor(Color.parseColor("#ffffff"));
        
		createDummySection();
		
		mMypagerAdapter = new MyPagerAdapter();
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mMypagerAdapter);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
			    mActionBar.setSelectedNavigationItem(position);
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			    mActionBar.onPageScrolled(arg0, arg1, arg2);
			}
		});
		mViewPager.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
			    mActionBar.onScrollToEnd(v, event);
				return false;
			}
		});

		for (int i = 0; i < mMypagerAdapter.getCount(); i++) {
		    mActionBar.addTab(mActionBar.newTab().setText("tab_" + i).setTabListener(this));
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

	public static class DummySectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";

		public DummySectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.gn_actionbar_tab_demo_fragment, container,
					false);
			TextView dummyTextView = (TextView) rootView.findViewById(R.id.section_label);
			dummyTextView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
			return rootView;
		}
	}

	private void createDummySection() {
		for (int i = 0; i < 4; i++) {

			View rootView = LayoutInflater.from(this).inflate(
					R.layout.gn_actionbar_tab_demo_fragment, null);
			TextView dummyTextView = (TextView) rootView.findViewById(R.id.section_label);
			dummyTextView.setText("page_" + i);
			mSections.add(rootView);
		}
	}

    @Override
    public void onTabSelected(amigoui.app.AmigoActionBar.Tab tab, FragmentTransaction ft) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(amigoui.app.AmigoActionBar.Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onTabReselected(amigoui.app.AmigoActionBar.Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gn_actionmenu_demo, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
