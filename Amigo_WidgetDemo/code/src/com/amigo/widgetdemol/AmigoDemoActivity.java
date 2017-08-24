package com.amigo.widgetdemol;

import java.util.ArrayList;

import amigoui.app.AmigoListActivity;
import amigoui.forcetouch.AmigoForceTouchConstant;
import amigoui.forcetouch.AmigoForceTouchListView.AmigoListViewForceTouchMenuCallback;
import amigoui.forcetouch.AmigoForceTouchListView.AmigoListViewForceTouchPreviewCallback;
import amigoui.widget.AmigoListView;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.amigoactionbar.GnActionBarWindow;
import com.demo.amigolistitem.ItemListDemo;
import com.demo.amigoscrollbar.GnScrollbarActivity;
import com.demo.amigoseekbar.GnSeekbarActivity;
import com.demo.androidl.EditModeDemo;
import com.demo.preference.GnPreferenceWindow;

public class AmigoDemoActivity extends AmigoListActivity {

    private static final String LOGTAG = "AmigoDemoActivity";

    ArrayList<String> mMultiViewArray = null;
    String[] mMultiViewStrings = {
            "action bar", 
            "dialog", 
            "context menu", 
            "preference", 
            "edit text",
            "checkbox and radiobutton in ListView", 
            "Switch", 
            "button", 
            "progress bar", 
            "expandablelistview",
            "fastscrollbar", 
            "seekbar", 
//            "fragment and viewpager", 
            "spiner demo",
            "list item layout",
            "edit mode",
            "text view",
            "alphbet",
            "AmigoTabHost",
            "change color with original activity",
			"ForceTouch",
			"AmigoNumberPicker",
			"AmigoListFragment"
    };
    private final int AMIGO_ACTION_BAR = 0;
    private final int AMIGO_ALERT_DIALOG = 1;
    private final int AMIGO_CONTEXT_MENU = 2;
    private final int AMIGO_PREFERENCE = 3;
    private final int AMIGO_EDIT_TEXT = 4;
    private final int AMIGO_LIST_VIEW2 = 5;
    private final int AMIGO_SWITCH = 6;
    private final int AMIGO_BUTTON = 7;
    private final int AMIGO_PROGRESS = 8;
    private final int AMIGO_EXPAND_LISTVIEW = 9;
    private final int AMIGO_TEST_SCROLLBAR = 10;
    private final int AMIGO_SEEKBAR = 11;
//    private final int AMIGO_FRAGMENT_VIEWPAGER = 12;
    private final int AMIGO_SPINER = 12;
    private final int AMIGO_LIST_ITME_LAYOUT = 13;
    private final int AMIGO_EDIT_MODE = 14;
    private final int AMIGO_TEXT_VIEW = 15;
    private final int AMIGO_ALPHBET = 16;
    private final int AMIGO_TAB_HOST = 17;
    private final int AMIGO_CHANGE_COLOR_ACTIVITY = 18;
	private final int AMIGO_FORCE_TOUCH = 19;
	private final int AMIGO_NUMBER_PICKER = 20;
	private final int AMIGO_LIST_FRAGMENT = 21;
//    private final int AMIGO_TEXT_VIEW       = 0;
//    
//    private final int AMIGO_LIST_VIEW      = 2;
//    

//    private final int AMIGO_SMART_GUIDE = 9;

//    private final int AMIGO_MULTI_SELECT = 12;

//    private final int AMIGO_SEARCH_VIEW = 16 ;
//    private final int AMIGO_IMAGE_VIEW = 17 ;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initArrayList();
        setTheme(R.style.AppTheme);
        setListAdapter(new SimpleListAdapter(this, mMultiViewArray));
        getListView().setDividerPaddingStart((int)getResources().getDimension(R.dimen.amigo_preference_child_padding_side));
        getListView().setForceTouchMenuCallback(
                new AmigoListViewForceTouchMenuCallback() {

                    @Override
                    public void onPrepareMenu(int position,
                            Menu menu) {

                        if(position == 2) {
                            menu.findItem(R.id.title1).setEnabled(false);
                            menu.findItem(R.id.title3).setVisible(false);
                        }
                    }

                    @Override
                    public int onForceTouchMenuType(int position) {
                        if (position % 2 == 0) {
                            return AmigoForceTouchConstant.MENU_TYPE_CONTENT_PREVIEW;
                        } else {
                            return AmigoForceTouchConstant.MENU_TYPE_QUICK_MENU;
                        }
                    }

                    @Override
                    public void onForceTouchMenuItemClick(int position,
                            MenuItem menuItem) {
                        Toast.makeText(AmigoDemoActivity.this,
                                "" + menuItem.getItemId() + ";pos=" + position,
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCreateMenu(int position,
                            Menu menu) {
                        Log.e(LOGTAG, "onCreateMenu position=" + position);
                        if (position % 2 == 1) {
                            getMenuInflater().inflate(R.menu.gn_forcetouch_menu,
                                    menu);
                        }else {
                            getMenuInflater().inflate(R.menu.gn_forcetouch_menu_without_submenu,
                                    menu);
                        }
                    }

                });
        getListView().setForceTouchPreviewCallback(
                new AmigoListViewForceTouchPreviewCallback() {

                    @Override
                    public View onCreatePreviewView(final int position) {
                        if (position % 2 == 0) {
                            TextView txtView = new TextView(AmigoDemoActivity.this);
                            txtView.setText("TestPostion3View");
                            txtView.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    Toast.makeText(AmigoDemoActivity.this,
                                            mMultiViewStrings[position],
                                            Toast.LENGTH_SHORT).show();
                                    ;
                                }
                            });
                            return txtView;
                        } else {
                            TextView txtView = new TextView(AmigoDemoActivity.this);
                            txtView.setText("TestPostionOthers");
                            txtView.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    Toast.makeText(AmigoDemoActivity.this,
                                            mMultiViewStrings[position],
                                            Toast.LENGTH_SHORT).show();
                                    ;
                                }
                            });
                            return txtView;
                        }
                    }

                    @Override
                    public void onClickPreviewView(int position) {
                        Toast.makeText(AmigoDemoActivity.this, "onClickPreviewView click position="+position, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initArrayList() {
        mMultiViewArray = new ArrayList<String>();
        for (int i = 0; i < mMultiViewStrings.length; i++) {
            mMultiViewArray.add(mMultiViewStrings[i]);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(LOGTAG,"onPause start");
        getListView().dismissForceTouchWindow();
    }

    @Override
    protected void onListItemClick(AmigoListView l, View view, int position, long id) {
        switch (position) {
//            case AMIGO_TEXT_VIEW:
//                Intent textIntent = new Intent();
//                textIntent.setClass(this, GnTextWindow.class);
//                startActivity(textIntent);
//                break;
            case AMIGO_EDIT_TEXT:
                Intent edittextIntent = new Intent();
                edittextIntent.setClass(this, GnEditTextWindow.class);
                startActivity(edittextIntent);
                break;
//            case AMIGO_LIST_VIEW:
//                Intent listviewIntent = new Intent();
//                listviewIntent.setClass(this, GnListViewWindow.class);
//                startActivity(listviewIntent);
//                break;
            case AMIGO_LIST_FRAGMENT:
                Intent listviewIntent = new Intent();
                 listviewIntent.setClass(this, GnListViewWindow.class);
                 startActivity(listviewIntent);
                break;
            case AMIGO_LIST_VIEW2:
                Intent listview2Intent = new Intent();
                listview2Intent.setClass(this, GnListView2Window.class);
                startActivity(listview2Intent);
                break;
            case AMIGO_BUTTON:
                Intent buttonIntent = new Intent();
                buttonIntent.setClass(this, GnButtonWindow.class);
                startActivity(buttonIntent);
                break;
            case AMIGO_PROGRESS:
                Intent progressIntent = new Intent();
                progressIntent.setClass(this, GnProgressBarWindow.class);
                startActivity(progressIntent);
                break;
            case AMIGO_SWITCH:
                Intent switchIntent = new Intent();
                switchIntent.setClass(this, GnSwitchButtonWindow.class);
                startActivity(switchIntent);
                break;
            case AMIGO_ALERT_DIALOG:
                Intent alertdialogIntent = new Intent();
                alertdialogIntent.setClass(this, GnAlertDialogWindow.class);
                startActivity(alertdialogIntent);
                break;
            case AMIGO_PREFERENCE:
                Intent preferenceIntent = new Intent();
                preferenceIntent.setClass(this, GnPreferenceWindow.class);
                startActivity(preferenceIntent);
                break;
//            case AMIGO_SMART_GUIDE:
//                break;
            case AMIGO_EXPAND_LISTVIEW:
                Intent expandlistIntent = new Intent();
                expandlistIntent.setClass(this, AmigoExpandableListViewDemo.class);
                startActivity(expandlistIntent);
                break;
            case AMIGO_ACTION_BAR:
                Intent actionbarIntent = new Intent();
                actionbarIntent.setClass(this, GnActionBarWindow.class);
                startActivity(actionbarIntent);
                break;
//            case AMIGO_MULTI_SELECT:
//                break;
            case AMIGO_CONTEXT_MENU:
                Intent contextMenuIntent = new Intent(this, GnContextMenuDemo.class);
                startActivity(contextMenuIntent);
                break;
//            case AMIGO_FRAGMENT_VIEWPAGER:
//                Intent fragmentIntent = new Intent(this, GnFragmentDemo.class);
//                startActivity(fragmentIntent);
//                break;
            case AMIGO_SPINER:
                Intent spinerIntent = new Intent(this, GnSpinerDemo.class);
                startActivity(spinerIntent);
                break;
//            case AMIGO_SEARCH_VIEW :
//                Intent searchIntent = new Intent(this, GnSearchViewDemo.class);
//                startActivity(searchIntent);
//                break;
//            case AMIGO_IMAGE_VIEW :
//                Intent imageIntent = new Intent(this, GnImageViewDemo.class);
//                startActivity(imageIntent);
//                break;
            case AMIGO_TEST_SCROLLBAR:
                Intent scrollbarIntent = new Intent();
                scrollbarIntent.setClass(this, GnScrollbarActivity.class);
                startActivity(scrollbarIntent);
                break;
            case AMIGO_SEEKBAR:
                Intent seekbarIntent = new Intent();
                seekbarIntent.setClass(this, GnSeekbarActivity.class);
                startActivity(seekbarIntent);
                break;
            case AMIGO_LIST_ITME_LAYOUT:
                Intent listitemIntent = new Intent();
                listitemIntent.setClass(this, ItemListDemo.class);
                startActivity(listitemIntent);
                break;
            case AMIGO_EDIT_MODE:
                Intent originalEdittextIntent = new Intent();
                originalEdittextIntent.setClass(this, EditModeDemo.class);
                startActivity(originalEdittextIntent);
                break;
            case AMIGO_TEXT_VIEW:
                Intent textIntent = new Intent();
                textIntent.setClass(this, GnTextViewWindow.class);
                startActivity(textIntent);
                break;
            case AMIGO_ALPHBET:
                Intent alphbetIntent = new Intent();
                alphbetIntent.setClass(this, AmigoAlphabetDemo.class);
                startActivity(alphbetIntent);
                break;
            case AMIGO_TAB_HOST:
                Intent tabHostIntent = new Intent(this, GnTabHostDemo.class);
                startActivity(tabHostIntent);
                break;
            case AMIGO_CHANGE_COLOR_ACTIVITY:
                Intent changeColorIntent = new Intent(this, GnChangeColorActivity.class);
                startActivity(changeColorIntent);
                break;
            case AMIGO_FORCE_TOUCH:
                Intent forceIntent = new Intent(this, GnForceTouchWindow.class);
                startActivity(forceIntent);
                break;
            case AMIGO_NUMBER_PICKER:
                Intent pickerIntent = new Intent(this, GNNumberPickerActivity.class);
                startActivity(pickerIntent);
                break;
            default:
                break;
        }
    }
}
