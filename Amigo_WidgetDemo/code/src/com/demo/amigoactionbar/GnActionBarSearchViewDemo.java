package com.demo.amigoactionbar;

import com.amigo.widgetdemol.R;

import amigoui.app.AmigoActionBar;
import amigoui.app.AmigoActivity;
import amigoui.changecolors.ChameleonColorManager;
import amigoui.widget.AmigoSearchView;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

public class GnActionBarSearchViewDemo extends AmigoActivity {
	AmigoActionBar mAmigoActionBar;
	AmigoSearchView mAmigoSearchView;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gn_search_view_demo);
		
		mAmigoActionBar = getAmigoActionBar();
		
		Drawable backIcon = getResources().getDrawable(R.drawable.amigo_ic_ab_back_dark);
		if(ChameleonColorManager.isNeedChangeColor()){
			backIcon.setTint(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1());
		}
		
		// 自定义返回键icon
		mAmigoActionBar.setIcon(backIcon);
		mAmigoActionBar.setHomeButtonEnabled(true);
		
		mAmigoActionBar.setDisplayShowCustomEnabled(true);
		mAmigoActionBar.setCustomView(R.layout.gn_searchview_on_actionbar);
		
		mAmigoSearchView = (AmigoSearchView) findViewById(R.id.search_view);
		if(mAmigoSearchView != null) {
			// 应用自行决定是否使用语音搜索
			mAmigoSearchView.setVoiceSearchMode(true, null);
			mAmigoSearchView.setIconifiedByDefault(false);
			// 应用自行决定hint文案
			mAmigoSearchView.setQueryHint("输入搜索内容");
		}
		
	}

}