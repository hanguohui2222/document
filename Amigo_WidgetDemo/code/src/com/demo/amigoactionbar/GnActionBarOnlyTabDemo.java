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
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.amigo.widgetdemol.R;

public class GnActionBarOnlyTabDemo extends AmigoActivity  implements AmigoActionBar.TabListener {
	ViewPager mViewPager;
	MyPagerAdapter mMypagerAdapter;
	List<ListView> mSections = new ArrayList<ListView>();
	
	private static final String TAG = "GnActionBarOnlyTabDemo";

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gn_actionbar_tab_demo);

		final AmigoActionBar actionBar = getAmigoActionBar();
		// maxw modify begin
		actionBar.setNavigationMode(AmigoActionBar.NAVIGATION_MODE_TABS); // 设置tab模式
		
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayShowHomeEnabled(false);
		
		actionBar.setIndicatorBackgroundColor(Color.parseColor("#00ff00"));
		// maxw modify end
		
		createDummySection();
		
		mMypagerAdapter = new MyPagerAdapter();
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mMypagerAdapter);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				if (actionBar.getSelectedNavigationIndex() != position) {
					actionBar.setSelectedNavigationItem(position);
				}
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
               
			}

			@Override
			public void onPageScrolled(int position, float positionOffset,
		            int positionOffsetPixels) {
		    	Log.v(TAG, "position:" + position + " offset:" + positionOffset + " offsetPixels:" + positionOffsetPixels);
				actionBar.onPageScrolled(position, positionOffset, positionOffsetPixels);
			}
		});
		mViewPager.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				actionBar.onScrollToEnd(v, event);
				return false;
			}
		});

		for (int i = 0; i < mMypagerAdapter.getCount(); i++) {
			actionBar.addTab(actionBar.newTab().setText("tab_" + i).setTextColor(getResources().getColorStateList(R.color.gn_tab_text_color)).setTabListener(this));
		}
		mViewPager.setOffscreenPageLimit(3);
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

			ListView listView = (ListView) LayoutInflater.from(this).inflate(
					R.layout.gn_demo_actionbar_onlytab, null);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1);
			for (int j = 0; j < 30; j++) {
				adapter.add("page_" + i + "_item_" + j);
			}
			listView.setAdapter(adapter);
			mSections.add(listView);
		}
	}

	
    ActionMode mActionMode;
    Callback mCallback = new Callback() {

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }
    };
	
    private void showAtionMode() {
        TextView textView = new TextView(this);
        textView.setText("asfdasfs");
        mActionMode = startActionMode(mCallback);
        mActionMode.setCustomView(textView);
    }

    @Override
    public void onTabSelected(amigoui.app.AmigoActionBar.Tab tab, FragmentTransaction ft) {
    	Log.v(TAG, "TabListener onTabSelected " + tab.getText());
        mViewPager.setCurrentItem(tab.getPosition());
        
    }

    @Override
    public void onTabUnselected(amigoui.app.AmigoActionBar.Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub
    	Log.v(TAG, "TabListener onTabUnselected " + tab.getText());
    }

    @Override
    public void onTabReselected(amigoui.app.AmigoActionBar.Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub
    	Log.v(TAG, "TabListener onTabReselected " + tab.getText());
    	int position = tab.getPosition();
    	if (position > -1) {
    		mSections.get(position).smoothScrollToPosition(0);
		}
    }
}
