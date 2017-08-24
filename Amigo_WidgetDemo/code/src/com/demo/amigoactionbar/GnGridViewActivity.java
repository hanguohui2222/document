package com.demo.amigoactionbar;

import java.util.ArrayList;

import amigoui.app.AmigoActivity;
import amigoui.widget.AmigoMultiChoiceBaseAdapter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.amigo.widgetdemol.R;
import com.demo.adapter.GnTestGridViewActionModeAdapter;

public class GnGridViewActivity extends AmigoActivity implements OnItemClickListener {
    
    private final static String TAG = "GnGridViewActivity";
    private ArrayList<String> testList = new ArrayList<String>();
    private String[] mDefaultData = {"通讯录", "日历", "相机", "时钟","游戏", "短信", "铃声", "设置","语音", "天气", "浏览器", "视频"};
    private int[] mDefaultIcon = {R.drawable.gn_tool_address_book, R.drawable.gn_tool_calendar, R.drawable.gn_tool_camera, R.drawable.gn_tool_clock,
            R.drawable.gn_tool_games_control, R.drawable.gn_tool_messenger, R.drawable.gn_tool_ringtone, R.drawable.gn_tool_settings,
            R.drawable.gn_tool_speech_balloon, R.drawable.gn_tool_weather, R.drawable.gn_tool_world, R.drawable.gn_tool_youtube,};
    private ArrayList<Integer> testIcon = new ArrayList<Integer>();
    private AmigoMultiChoiceBaseAdapter mAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gn_action_mode_demo_grid_view);
        getAmigoActionBar().setTitle("GridView多选模式");
        buildList(savedInstanceState);
    }
    
    private void buildList(Bundle savedInstanceState) {
        for (int i = 0; i < mDefaultData.length; i++) {
            testList.add(mDefaultData[i]);
            testIcon.add(mDefaultIcon[i]);
        }
        mAdapter = new GnTestGridViewActionModeAdapter(savedInstanceState, testList, testIcon, this);
        mAdapter.setOnItemClickListener(this);
        
        mAdapter.setAdapterView((AdapterView) findViewById(R.id.gn_grid_view));
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
