package com.gionee.hotspottransmission.view.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import com.gionee.hotspottransmission.adapter.FileListAdapter;
import com.gionee.hotspottransmission.bean.FileInfo;
import com.gionee.hotspottransmission.bean.FileSendData;
import com.gionee.hotspottransmission.callback.IRefreshFileSelectedCallBack;
import com.gionee.hotspottransmission.callback.ISelectSortFiles;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.dao.MediaDao;
import com.gionee.hotspottransmission.callback.IDataQueryCallBack;
import com.gionee.hotspottransmission.manager.SelectFilesManager;
import com.gionee.hotspottransmission.callback.ISelectFiles;
import com.gionee.hotspottransmission.utils.LogUtil;
import com.gionee.hotspottransmission.view.SelectFilesActivity;
import java.util.List;
import com.gionee.hotspottransmission.R;

public class VideoFragment extends Fragment implements SelectFilesActivity.IBackPressedListener,ISelectSortFiles {
    private final String TAG = "VideoFragment";
    private FileListAdapter mAdapter;
    private CheckBox mSelect;
    private SelectFilesManager mSelectFilesManager;
    private Button mbtnSelected;
    private Button mbtnSend;
    private Context mContext;
    private ListView mListView;
    public List<FileInfo> mVideoList;

    public void setSelectAll(boolean selectAll) {
        this.isSelectAll = selectAll;
    }

    private boolean isSelectAll;

    public boolean isSelectAll() {
        return isSelectAll;
    }

    public static VideoFragment newInstance() {
        VideoFragment pageFragment = new VideoFragment();
        return pageFragment;
    }

    public FileListAdapter getmAdapter() {
        return mAdapter;
    }

    public Handler imageHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.SET_IMAGE_BITMAP:
                    ImageView imageView = (ImageView)mListView.findViewWithTag(msg.arg1);
                    if (imageView != null){
                        imageView.setImageBitmap((Bitmap)msg.obj);
                    }
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();
        mSelectFilesManager = new SelectFilesManager();
        mListView = (ListView) inflater.inflate(R.layout.fragment_video, container, false);
        MediaDao dao = new MediaDao(getActivity(), new IDataQueryCallBack() {
            @Override
            public void onQueryEnd(List<FileInfo> videoList) {
                // #18777 start
                if(((SelectFilesActivity)getActivity()).mVideoFragment != VideoFragment.this){
                    ((SelectFilesActivity)getActivity()).mVideoFragment = VideoFragment.this;
                }
                // #18777 end
                mVideoList = videoList;
                mAdapter = new FileListAdapter(getActivity(), videoList,imageHandler);
                mListView.setAdapter(mAdapter);
            }
        });
        dao.queryMediaData(Constants.TYPE_VIDEO);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FileInfo info = (FileInfo) mAdapter.getItem(position);
                mSelect = (CheckBox) view.findViewById(R.id.file_checkbox);
                selectedItem(!mSelect.isChecked(), info);
            }
        });
        return mListView;
    }
	
	@Override
    public void selectedItem(final boolean isSelected, FileInfo info) {
        mSelectFilesManager.changFileTransferList(isSelected, info, new IRefreshFileSelectedCallBack() {
            @Override
            public void onRefreshCount(List<FileInfo> list) {
                if(getActivity() instanceof ISelectFiles){
                    ((ISelectFiles)getActivity()).refreshMenu(list);
                    if(mVideoList.size() == FileSendData.getInstance().mVideoDatas.size()){
                        isSelectAll = true;
                    }else{
                        isSelectAll = false;
                    }
                    ((ISelectFiles) getActivity()).refreshSelectAllText();
                }
            }

            @Override
            public void onRefreshSelected(boolean isSelected) {
                if (isSelected) {
                    mSelect.setChecked(isSelected);
                    mSelect.setVisibility(View.VISIBLE);
                } else {
                    mSelect.setChecked(isSelected);
                    mSelect.setVisibility(View.GONE);
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
        mSelectFilesManager.changFileTransferList(Constants.TYPE_VIDEO,isSelected, mVideoList, new IRefreshFileSelectedCallBack() {
            @Override
            public void onRefreshCount(List<FileInfo> list) {
                if (getActivity() instanceof ISelectFiles) {
                    ((ISelectFiles) getActivity()).refreshMenu(list);
                }
            }
            //刷新列表中全部view
            @Override
            public void onRefreshSelected(boolean isSelected) {
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onShowSelected(List<FileInfo> list) {

            }
        });
    }

    @Override
    public boolean onBack() {
        return false;
    }
}
