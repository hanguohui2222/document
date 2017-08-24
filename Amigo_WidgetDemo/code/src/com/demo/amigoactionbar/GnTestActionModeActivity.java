package com.demo.amigoactionbar;

import java.util.ArrayList;

import amigoui.app.AmigoActionBar;
import amigoui.app.AmigoActivity;
import amigoui.widget.AmigoListView;
import amigoui.widget.AmigoMultiChoiceBaseAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.amigo.widgetdemol.R;
import com.demo.adapter.GnTestActionModeAdapter;

public class GnTestActionModeActivity extends AmigoActivity implements OnItemClickListener {

	private final static String TAG = "GnTestActionModeActivity";
	private ArrayList<String> testList = new ArrayList<String>();
	private AmigoMultiChoiceBaseAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gn_action_mode_demo);

		AmigoActionBar actionBar = getAmigoActionBar();
		actionBar.setOnActionBarDoubleClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				getListView().returnTop();
				Toast.makeText(GnTestActionModeActivity.this, "展示list回到顶部效果", Toast.LENGTH_LONG).show();
			}
		});

		buildList(savedInstanceState);
	}

	private void buildList(Bundle savedInstanceState) {
		testList.add("双击actionbar返回列表顶部");
		testList.add("展示不可选择Item项");
		testList.add("展示item内容特别长时的显示情况是否出现异常");
		for (int i = 0; i <= 10000; i++) {
			testList.add("test" + i);
		}
		testList.add("双击actionbar返回列表顶部");
		mAdapter = new GnTestActionModeAdapter(savedInstanceState, testList, this);
		mAdapter.setOnItemClickListener(this);
		mAdapter.setAdapterView(getListView());
		getListView().setFastScrollAlwaysVisible(true);
		getListView().setFastScrollEnabled(true);
		getListView().setSmoothScrollbarEnabled(true);
	}

	private AmigoListView getListView() {
		AmigoListView view = (AmigoListView) findViewById(android.R.id.list);
		Log.e(TAG, "getListView view=" + view);
		return view;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		mAdapter.save(outState);
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
		Toast.makeText(this, "Item click: " + mAdapter.getItem(position), Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.gn_actionmenu_demo, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		Toast.makeText(this, menuItem.getTitle(), Toast.LENGTH_SHORT).show();
		mAdapter.enterMultiChoiceMode();
		return super.onOptionsItemSelected(menuItem);
	}
}
