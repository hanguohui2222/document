package com.gionee.hotspottransmission.view.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gionee.hotspottransmission.adapter.FileListAdapter;
import com.gionee.hotspottransmission.adapter.ImageDirAdapter;
import com.gionee.hotspottransmission.bean.FileInfo;
import com.gionee.hotspottransmission.bean.FileSendData;
import com.gionee.hotspottransmission.callback.IRefreshFileSelectedCallBack;
import com.gionee.hotspottransmission.callback.ISelectSortFiles;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.manager.QueryImageDataManager;
import com.gionee.hotspottransmission.manager.SelectFilesManager;
import com.gionee.hotspottransmission.callback.ISelectFiles;
import com.gionee.hotspottransmission.view.SelectFilesActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.gionee.hotspottransmission.R;

/**
 * Created by luorw on 4/23/16.
 */
public class ImagesDirFragment extends Fragment implements View.OnClickListener, SelectFilesActivity.IBackPressedListener, AdapterView.OnItemClickListener, ISelectSortFiles {
    private final String TAG = "ImagesDirFragment";
    private GridView mDirGridView;
    private GridView mGridView;
    private ImageDirAdapter mImageDirAdapter;
    private FileListAdapter mMediaListAdapter;
    private Map<String, List<FileInfo>> mDataInfoMap;
    private LinearLayout mNavigationLayout;
    private TextView mFirstDirBtn;
    private TextView mSecondDirBtn;
    private CheckBox mSelect;
    private SelectFilesManager mSelectFilesManager;
    private Context mContext;
    private ImageView mSelectedBg;
    private int mImageMode = Constants.IMAGE_FIRST_DIR;
    private boolean isSelectAll;
    public List<FileInfo> mImageList = new ArrayList<FileInfo>();
    private String mCurrentDirName;

    public void setIsSelectAll(boolean mIsSelectAll) {
        this.isSelectAll = mIsSelectAll;
    }

    public boolean isSelectAll() {
        return isSelectAll;
    }

    public int getmImageMode() {
        return mImageMode;
    }

    public void setmImageMode(int mImageMode) {
        this.mImageMode = mImageMode;
    }

    public static ImagesDirFragment newInstance() {
        ImagesDirFragment pageFragment = new ImagesDirFragment();
        return pageFragment;
    }

    public FileListAdapter getmMediaListAdapter() {
        return mMediaListAdapter;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_images, null);
        mDirGridView = (GridView) rootView.findViewById(R.id.images_dir_gv);
        mGridView = (GridView) rootView.findViewById(R.id.images_gridview);
        mNavigationLayout = (LinearLayout) rootView.findViewById(R.id.navigation_layout);
        mFirstDirBtn = (TextView) rootView.findViewById(R.id.first_dir);
        mSecondDirBtn = (TextView) rootView.findViewById(R.id.second_dir);
        mFirstDirBtn.setOnClickListener(this);
        mContext = getActivity();
        mSelectFilesManager = new SelectFilesManager();
        mDirGridView.setOnItemClickListener(this);
        mGridView.setOnItemClickListener(this);
        mImageDirAdapter = new ImageDirAdapter(getActivity(), refreshUIHandler);
        mDirGridView.setAdapter(mImageDirAdapter);
        QueryImageDataManager queryImageDataManager = new QueryImageDataManager();
        queryImageDataManager.queryDataInfo(getActivity(), Constants.TYPE_IMAGE, refreshUIHandler);
        return rootView;
    }

    Handler refreshUIHandler = new Handler() {
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case Constants.REFRESH_IMAGE_DRI_GV:
                    mDataInfoMap = (Map<String, List<FileInfo>>) msg.obj;
                    mImageDirAdapter.setDataInfoMap(mDataInfoMap);
                    mImageDirAdapter.notifyDataChanged();
                    break;
                case Constants.SET_IMAGE_BITMAP:
                    ImageView imageView = (ImageView) mDirGridView.findViewWithTag(msg.arg1);
                    if (imageView != null) {
                        imageView.setImageBitmap((Bitmap) msg.obj);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public Handler imageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.SET_IMAGE_BITMAP:
                    ImageView imageView = (ImageView) mGridView.findViewWithTag(msg.arg1);
                    if (imageView != null) {
                        imageView.setImageBitmap((Bitmap) msg.obj);
                    }
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        showImageDirView();
    }

    private void showImageDirView() {
        if (mDirGridView != null) {
            mDirGridView.setVisibility(View.VISIBLE);
            mGridView.setVisibility(View.GONE);
            mNavigationLayout.setVisibility(View.GONE);
            mImageMode = Constants.IMAGE_FIRST_DIR;
            if (getActivity() instanceof ISelectFiles) {
                ((ISelectFiles) getActivity()).refreshSelectAllText();
            }
        }
    }


    @Override
    public boolean onBack() {
        if (mDirGridView == null || mDirGridView.getVisibility() == View.VISIBLE) {
            return false;
        } else {
            showImageDirView();
            return true;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.images_dir_gv:
                showSecondDir(position);
                break;
            case R.id.images_gridview:
                FileInfo info = (FileInfo) mMediaListAdapter.getItem(position);
                mSelect = (CheckBox) view.findViewById(R.id.image_show_checkbox);
                mSelectedBg = (ImageView) view.findViewById(R.id.image_show_checkbox_bg);
                selectedItem(!mSelect.isChecked(), info);
                break;
            default:
                break;
        }
    }

    private void showSecondDir(int position) {
        mImageMode = Constants.IMAGE_SECOND_DIR;
        mDirGridView.setVisibility(View.GONE);
        mGridView.setVisibility(View.VISIBLE);
        mNavigationLayout.setVisibility(View.VISIBLE);
        String key = (String) mImageDirAdapter.getItem(position);
        mCurrentDirName = key;
        mSecondDirBtn.setText(key);
        // #18777 start
        if (((SelectFilesActivity) getActivity()).mImagesDirFragment != this) {
            ((SelectFilesActivity) getActivity()).mImagesDirFragment = this;
        }
        // #18777 end
        mImageList.clear();
        mImageList.addAll(mDataInfoMap.get(key));
        mMediaListAdapter = new FileListAdapter(getActivity(), mImageList, imageHandler);
        mGridView.setAdapter(mMediaListAdapter);
        if (getActivity() instanceof ISelectFiles) {
            updateSelectAllText();
        }
    }

    private void updateSelectAllText() {
        List<FileInfo> list = FileSendData.getInstance().mImageDatas.get(mCurrentDirName);
        if (list != null && mImageList.size() == list.size()) {
            isSelectAll = true;
        } else {
            isSelectAll = false;
        }
        ((ISelectFiles) getActivity()).refreshSelectAllText();
    }

    @Override
    public void selectedItem(boolean isSelected, FileInfo info) {
        mSelectFilesManager.changImgTransferList(isSelected, info, mCurrentDirName, new IRefreshFileSelectedCallBack() {
            @Override
            public void onRefreshCount(List<FileInfo> list) {
                if (getActivity() instanceof ISelectFiles) {
                    ((ISelectFiles) getActivity()).refreshMenu(list);
                    updateSelectAllText();
                }
            }

            @Override
            public void onRefreshSelected(boolean isSelected) {
                if (isSelected) {
                    mSelect.setChecked(isSelected);
                    mSelectedBg.setVisibility(View.VISIBLE);
                    mSelectedBg.bringToFront();
                } else {
                    mSelect.setChecked(isSelected);
                    mSelectedBg.setVisibility(View.GONE);
                }
            }

            @Override
            public void onShowSelected(List<FileInfo> list) {

            }
        });
    }

    @Override
    public void selectedAll(boolean isSelected) {
        isSelectAll = isSelected;
        mSelectFilesManager.changImgTransferList(isSelected, mImageList, mCurrentDirName, new IRefreshFileSelectedCallBack() {
            @Override
            public void onRefreshCount(List<FileInfo> list) {
                if (getActivity() instanceof ISelectFiles) {
                    ((ISelectFiles) getActivity()).refreshMenu(list);
                    updateSelectAllText();
                }
            }

            //刷新列表中全部view
            @Override
            public void onRefreshSelected(boolean isSelected) {
                mMediaListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onShowSelected(List<FileInfo> list) {

            }
        });
    }
}
