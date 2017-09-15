package com.gionee.hotspottransmission.history.view;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore.Images;
import android.util.ArrayMap;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;

import com.gionee.hotspottransmission.R;
import com.gionee.hotspottransmission.bean.FileInfo;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.history.adapter.ReceivedFileListAdapter;
import com.gionee.hotspottransmission.history.biz.ReceivedFileBiz;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import amigoui.app.AmigoActionBar;
import amigoui.app.AmigoActivity;

public class SpecReceivedFileActivity extends AmigoActivity {
    private Map<String,List<FileInfo>> mFileReceiveMap = new ArrayMap<>();

    private int mType;
    private ExpandableListView mListView;
    private int imageCount = 0;

    private ReceivedFileBiz mFileBiz;
    private ReceivedFileListAdapter mAdapter;
    private LinearLayout ll_no_file;

    private AmigoActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_file);
        mFileBiz = new ReceivedFileBiz(this);
        initView();
        initTabActionBar();

        registImageObserver();
    }

    ContentObserver mImageObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            initTabActionBar();
        }
    };

    public void registImageObserver(){
        Uri uri = Images.Media.getContentUri("external");
        getContentResolver().registerContentObserver(uri, false, mImageObserver);
    }

    private void unregisterObserver(ContentObserver contentObserver) {
        getContentResolver().unregisterContentObserver(contentObserver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterObserver(mImageObserver);
    }

    private void initView() {
        ll_no_file = (LinearLayout) findViewById(R.id.ll_no_file);
        mListView = (ExpandableListView) findViewById(R.id.list_receive);
    }

    private void initTabActionBar() {
        mType = getIntent().getIntExtra("type", Constants.TYPE_FILE);
        mActionBar = getAmigoActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(true);
        mActionBar.setNavigationMode(amigoui.app.AmigoActionBar.DISPLAY_HOME_AS_UP);
        mActionBar.setDisplayShowCustomEnabled(true);
        switch (mType){
            case Constants.TYPE_FILE:
                mActionBar.setTitle(R.string.files);
                break;
            case Constants.TYPE_APPS:
                mActionBar.setTitle(R.string.apps);
                break;
            case Constants.TYPE_IMAGE:
                mActionBar.setTitle(R.string.picture);
                break;
            case Constants.TYPE_MUSIC:
                mActionBar.setTitle(R.string.music);
                break;
            case Constants.TYPE_VIDEO:
                mActionBar.setTitle(R.string.video);
                break;
            default:
                break;
        }
        getReceivedFileMap();

        mActionBar.show();

        mActionBar.setOnBackClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void getReceivedFileMap() {
        mFileReceiveMap = mFileBiz.findFileByType(mType);
        if(mFileReceiveMap.size() <= 0){
            ll_no_file.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
        }else {
            ll_no_file.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
//            if(mType == Constants.TYPE_IMAGE){
//                for (String key : mFileReceiveMap.keySet()) {
//                    List<FileInfo> files = mFileReceiveMap.get(key);
//                    imageCount += files.size();
//                }
//            }

        }
        setListAdapter();
    }

    private void setListAdapter() {
        mAdapter = new ReceivedFileListAdapter(this, mFileReceiveMap, mType, mListView);
        mListView.setAdapter(mAdapter);
        for(int i=0; i<mAdapter.getGroupCount(); i++){
            mListView.expandGroup(i);
        }
        mListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return true;
            }
        });
//        mListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
//            @Override
//            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
//                FileInfo fileInfo = mAdapter.getFileInfos(groupPosition).get(childPosition);
//                FileTransferUtils.openFileFromUri(SpecReceivedFileActivity.this,fileInfo);
//                return true;
//            }
//        });
    }

}