package com.amigo.widgetdemol;

import java.lang.reflect.Field;

import amigoui.app.AmigoActivity;
import amigoui.widget.AmigoTabHost;
import amigoui.widget.AmigoTabWidget;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;

public class GnTabHostDemo extends AmigoActivity {

	private AmigoTabHost tabHost;
	private AmigoTabWidget tabWidget;
	Field mBottomLeftStrip;
	Field mBottomRightStrip;
	
	private static final String TAG = "GnTabHostDemo";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView();
		setContentView(R.layout.gn_tab_host_demo);

		tabHost = (AmigoTabHost) findViewById(R.id.tabHost);
		tabHost.setup();

		tabWidget = (AmigoTabWidget) tabHost.getTabWidget();

		TabSpec spec1 = tabHost.newTabSpec("Tab1");
		spec1.setIndicator("标签0");
		spec1.setContent(new TabContentFactory() {
			
			@Override
			public View createTabContent(String tag) {
				// TODO Auto-generated method stub
				return LayoutInflater.from(GnTabHostDemo.this).inflate(R.layout.tab_host_1, null);
			}
		});

		TabSpec spec2 = tabHost.newTabSpec("Tab2");
		spec2.setIndicator("标签1");
		spec2.setContent(new TabContentFactory() {
			
			@Override
			public View createTabContent(String tag) {
				// TODO Auto-generated method stub
				return LayoutInflater.from(GnTabHostDemo.this).inflate(R.layout.tab_host_2, null);
			}
		});

		TabSpec spec3 = tabHost.newTabSpec("Tab3");
		spec3.setIndicator("标签2");
		spec3.setContent(new TabContentFactory() {
			
			@Override
			public View createTabContent(String tag) {
				// TODO Auto-generated method stub
				return LayoutInflater.from(GnTabHostDemo.this).inflate(R.layout.tab_host_3, null);
			}
		});

		TabSpec spec4 = tabHost.newTabSpec("Tab4");
		spec4.setIndicator("标签3");
		spec4.setContent(new TabContentFactory() {
			
			@Override
			public View createTabContent(String tag) {
				// TODO Auto-generated method stub
				return LayoutInflater.from(GnTabHostDemo.this).inflate(R.layout.tab_host_4, null);
			}
		});

		tabHost.addTab(spec1);
		tabHost.addTab(spec2);
		tabHost.addTab(spec3);
		tabHost.addTab(spec4);
		tabWidget.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 400));
		//tabHost.setIndicatorBackgroundColor(Color.GREEN);
		//tabWidget.setBackgroundColor(Color.argb(128, 80, 1, 20));
		//tabHost.setBackgroundColor(Color.argb(255, 100, 20, 1));
	}

}