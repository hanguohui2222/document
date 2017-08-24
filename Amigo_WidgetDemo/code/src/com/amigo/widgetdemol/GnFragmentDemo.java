package com.amigo.widgetdemol;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.app.FragmentManager;


import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.adapter.GnFragAdapter;
import com.demo.ui.GnMyFragment;
import amigoui.app.AmigoActivity;

public class GnFragmentDemo extends AmigoActivity implements OnClickListener {

    private ViewPager vp;
    private TextView tv_no1;
    private TextView tv_no2;
    private TextView tv_no3;
    private List<Fragment> fragments;

    private GnFragAdapter adapter;
    private Menu mMenu ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gn_fragment_demo);
        vp = (ViewPager) findViewById(R.id.vp_main);
        tv_no1 = (TextView) findViewById(R.id.tv_no1);
        tv_no2 = (TextView) findViewById(R.id.tv_no2);
        tv_no3 = (TextView) findViewById(R.id.tv_no3);
        
        tv_no1.setOnClickListener(this);
        tv_no2.setOnClickListener(this);
        tv_no3.setOnClickListener(this);
        initViewPage();
    }


    private void initViewPage() {

        fragments = new ArrayList<Fragment>();
        fragments.add(GnMyFragment.newInstance("TAB1"));
        fragments.add(GnMyFragment.newInstance("TAB2"));
        fragments.add(GnMyFragment.newInstance("TAB3"));

        adapter = new GnFragAdapter(getFragmentManager(), fragments);
        vp.setAdapter(adapter);
        vp.setCurrentItem(0);
        changeTextColor(0);
        vp.setOnPageChangeListener(new MyVPageChangeListener());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        
        Log.i("Dazme", "vp.getCurrentItem()"+vp.getCurrentItem());
        /*switch (vp.getCurrentItem()) {
        case 1:
            getMenuInflater().inflate(R.menu.menu1, menu);
            break;
        case 2:
            getMenuInflater().inflate(R.menu.menu2, menu);
            break;
        case 0:
        default:
            getMenuInflater().inflate(R.menu.menu3, menu);
            break;
        }*/
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
       Log.i("Dazme", "onOptionsItemSelected-FragMent");
        final Fragment fragment = GnFragmentDemo.this.getFragmentAt(vp.getCurrentItem());
        if (fragment != null) {
            fragment.onOptionsItemSelected(menuItem);
        }
        return super.onOptionsItemSelected(menuItem);

    }
    
@Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.i("Dazme", "onPrepareOptionsMenu-FragMent");
        /*if (vp != null) {
            final int position = vp.getCurrentItem(); 
            final Fragment fragment = getFragmentAt(position);
            if (fragment != null) {
                fragment.onPrepareOptionsMenu(menu);
            }
        }*/
        return super.onPrepareOptionsMenu(menu);
    }

    private class MyVPageChangeListener implements OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onPageSelected(int location) {
            changeTextColor(location);
            onCreateOptionsMenu(mMenu);
        }

    }

    private void changeTextColor(int location) {
        switch (location) {
        case 0:
            tv_no1.setTextColor(Color.RED);
            tv_no2.setTextColor(Color.CYAN);
            tv_no3.setTextColor(Color.YELLOW);
            break;
        case 1:
            tv_no2.setTextColor(Color.RED);
            tv_no1.setTextColor(Color.CYAN);
            tv_no3.setTextColor(Color.YELLOW);
            break;
        case 2:
            tv_no3.setTextColor(Color.RED);
            tv_no1.setTextColor(Color.CYAN);
            tv_no2.setTextColor(Color.YELLOW);
            break;
        default:
            break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.tv_no1:
            vp.setCurrentItem(0);
            break;
        case R.id.tv_no2:
            vp.setCurrentItem(1);
            break;
        case R.id.tv_no3:
            vp.setCurrentItem(2);
            break;
        default:
            break;
        }
    }

    private Fragment getFragmentAt(int position) {
        switch (position) {
        case 0:
        case 1:
        case 2:
            return fragments.get(position);
        default:
            throw new IllegalStateException("Unknown fragment index: "
                    + position);
        }
    }
}
