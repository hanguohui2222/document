package com.amigo.widgetdemol;

import java.util.ArrayList;
import java.util.List;

import amigoui.app.AmigoActionBar;
import amigoui.app.AmigoActionBar.OnExtraViewDragListener;
import amigoui.app.AmigoActionBar.Tab;
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
import android.widget.TextView;
import android.widget.Toast;

import com.amigo.widgetdemol.R;

public class GnChangeColorsDemo extends AmigoActivity  implements AmigoActionBar.TabListener {
	ViewPager mViewPager;
	MyPagerAdapter mMypagerAdapter;
	List<View> mSections = new ArrayList<View>();

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gn_change_colors_demo);

		final AmigoActionBar actionBar = getAmigoActionBar();
		actionBar.setNavigationMode(AmigoActionBar.NAVIGATION_MODE_TABS);
		//shaozj begin
		//actionBar.setDisplayShowHomeEnabled(true);
		//actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		//actionBar.setIndicatorBackgroundColor(Color.parseColor("#ffffff"));
        //shaozj end
		
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
		mViewPager.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				actionBar.onScrollToEnd(v, event);
				return false;
			}
		});

		for (int i = 0; i < mMypagerAdapter.getCount(); i++) {
			actionBar.addTab(actionBar.newTab().setText("tab_" + i).setTabListener(this));
		}
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gn_actionmenu_demo, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        boolean ischecked1 = false;
        boolean ischecked2 = false;
        switch (menuItem.getItemId()) {
        case R.id.title1:
            Toast.makeText(GnChangeColorsDemo.this, "Change colors",
                    Toast.LENGTH_SHORT).show();
            getAmigoActionBar().changeColors();
            break;
        case R.id.title2:
            Toast.makeText(GnChangeColorsDemo.this, "Hide options menu",
                    Toast.LENGTH_SHORT).show();
            setOptionsMenuHideMode(true);
            break;
        case R.id.title3:
            Toast.makeText(GnChangeColorsDemo.this, "you press menu 3",
                    Toast.LENGTH_SHORT).show();
            break;
        case R.id.title4:
            Toast.makeText(GnChangeColorsDemo.this, "you press menu 4",
                    Toast.LENGTH_SHORT).show();
            break;
        case R.id.title5:
            ischecked2 = !ischecked2;
            menuItem.setChecked(ischecked2);
            Toast.makeText(GnChangeColorsDemo.this, "you press menu 5",
                    Toast.LENGTH_SHORT).show();
            break;
        case R.id.title6:
            ischecked1 = !ischecked1;
            menuItem.setChecked(ischecked1);
            Toast.makeText(GnChangeColorsDemo.this, "you press menu 6",
                    Toast.LENGTH_SHORT).show();
            break;
        case R.id.title7:
            Toast.makeText(GnChangeColorsDemo.this, "you press menu 7",
                    Toast.LENGTH_SHORT).show();
            break;
        case R.id.title8:
            Toast.makeText(GnChangeColorsDemo.this, "you press menu 8",
                    Toast.LENGTH_SHORT).show();
            break;
        default:
            break;
        }
        return super.onOptionsItemSelected(menuItem);

    }
}
