package com.demo.amigoactionbar;

import java.util.ArrayList;

import amigoui.app.AmigoListActivity;
import amigoui.changecolors.ChameleonColorManager;
import amigoui.widget.AmigoListView;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.amigo.widgetdemol.GnActionMenuViewDemo;
import com.amigo.widgetdemol.SimpleListAdapter;


public class GnActionBarWindow extends AmigoListActivity {

    ArrayList<String> mMultiViewArray = null;
    String[] mMultiViewStrings = {"AppBar", "带返回按钮的AppBar", "AppBar＋TabBar","底部ActionBar","多选模式BaseAdapter", "多选模式CursorAdapter", "覆盖模式", "OptionsMenu显示在顶部ActionBar上", "只显示tabbar", "搜索条", "GridView多选模式"};

    enum ActionBarEnum {
        NORMAL, BACK, TAB, AMIGO_ActionMenuView, MULTIPLE_SELECT, MULTIPLE_SELECT_CURSOR, OVER_LAY, OPTIONS_MENU_AS_UP, ONLY_TAB, SEARCH_BAR, GRID_VIEW_MULTICHOICE 
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ChameleonColorManager.getInstance().registerNoChangeColor(this);
        super.onCreate(savedInstanceState);
        initArrayList();
        setListAdapter(new SimpleListAdapter(this, mMultiViewArray));

        setTitle("AmigoActionBar用法");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ChameleonColorManager.getInstance().unregisterNoChangeColor(this);
    }
    
    private void initArrayList() {
        mMultiViewArray = new ArrayList<String>();
        for (int i = 0; i < mMultiViewStrings.length; i++) {
            mMultiViewArray.add(mMultiViewStrings[i]);
        }
    }

    @Override
    protected void onListItemClick(AmigoListView l, View v, int position, long id) {
        switch (ActionBarEnum.values()[position]) {
            case NORMAL:
                Intent actionNormalIntent = new Intent(this, GnActionBarNormalDemo.class);
                startActivity(actionNormalIntent);
                break;
//            case CUSTOM:
//                Intent customIntent = new Intent(this, GnActionBarCustomViewDemo.class);
//                startActivity(customIntent);
//                break;
            case BACK:
                Intent actionBackIntent = new Intent(this, GnActionBarBackDemo.class);
                startActivity(actionBackIntent);
                break;
            case TAB:
                Intent actionTabIntent = new Intent(this, GnActionBarTabDemo.class);
                startActivity(actionTabIntent);
                break;
            case AMIGO_ActionMenuView:
                Intent actionMenuViewIntent = new Intent(this, GnActionMenuViewDemo.class);
                startActivity(actionMenuViewIntent);
                break;
            case OVER_LAY:
                Intent overlayIntent = new Intent(this, GnActionBarOvlayDemo.class);
                startActivity(overlayIntent);
                break;
            case MULTIPLE_SELECT:
                Intent actionModeIntent = new Intent(this, GnTestActionModeActivity.class);
                startActivity(actionModeIntent);
                break;
            case OPTIONS_MENU_AS_UP:
                Intent optionsMenuIntent = new Intent(this, GnOptionsMenuAsUpDemo.class);
                startActivity(optionsMenuIntent);
                break;
            case ONLY_TAB:
            	 Intent onlyTabIntent = new Intent(this, GnActionBarOnlyTabDemo.class);
                 startActivity(onlyTabIntent);
                 break;
            case SEARCH_BAR:
            	 Intent searchBarIntent = new Intent(this, GnActionBarSearchViewDemo.class);
                 startActivity(searchBarIntent);
                 break;
            case MULTIPLE_SELECT_CURSOR:
                Intent actionModeIntent2 = new Intent(this, GnTestCursorActionModeActivity.class);
                startActivity(actionModeIntent2);
                break;
                
            case GRID_VIEW_MULTICHOICE:
                Intent gridViewIntent = new Intent(this, GnGridViewActivity.class);
                startActivity(gridViewIntent);
                break;
            default:
                break;
        }
    }
}
