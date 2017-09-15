package com.gionee.hotspottransmission.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
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
import com.gionee.hotspottransmission.view.SelectFilesActivity;

import java.util.List;

import com.gionee.hotspottransmission.R;

public class MusicFragment extends Fragment implements SelectFilesActivity.IBackPressedListener, ISelectSortFiles {
    private final String TAG = "MusicFragment";
    private FileListAdapter mAdapter;
    private CheckBox mSelect;
    private SelectFilesManager mSelectFilesManager;
    private Context mContext;
    public List<FileInfo> mMusicList;
    private boolean isSelectAll;

    public void setSelectAll(boolean selectAll) {
        this.isSelectAll = selectAll;
    }

    public boolean isSelectAll() {
        return isSelectAll;
    }

    public static MusicFragment newInstance() {
        MusicFragment pageFragment = new MusicFragment();
        return pageFragment;
    }

    public FileListAdapter getmAdapter() {
        return mAdapter;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();
        mSelectFilesManager = new SelectFilesManager();
        final ListView view = (ListView) inflater.inflate(R.layout.fragment_music, container, false);
        MediaDao dao = new MediaDao(getActivity(), new IDataQueryCallBack() {
            @Override
            public void onQueryEnd(List<FileInfo> musicList) {
                // #18777 start
                if (((SelectFilesActivity) getActivity()).mMusicFragment != MusicFragment.this) {
                    ((SelectFilesActivity) getActivity()).mMusicFragment = MusicFragment.this;
                }
                // #18777 end
                mMusicList = musicList;
                mAdapter = new FileListAdapter(getActivity(), musicList, null);
                view.setAdapter(mAdapter);
            }
        });
        dao.queryMediaData(Constants.TYPE_MUSIC);
        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FileInfo info = (FileInfo) mAdapter.getItem(position);
                mSelect = (CheckBox) view.findViewById(R.id.file_checkbox);
                selectedItem(!mSelect.isChecked(), info);
            }
        });
        return view;
    }

    @Override
    public boolean onBack() {
        return false;
    }

    @Override
    public void selectedItem(boolean isSelected, FileInfo info) {
        mSelectFilesManager.changFileTransferList(isSelected, info, new IRefreshFileSelectedCallBack() {
            @Override
            public void onRefreshCount(List<FileInfo> list) {
                if (getActivity() instanceof ISelectFiles) {
                    ((ISelectFiles) getActivity()).refreshMenu(list);
                    if(mMusicList.size() == FileSendData.getInstance().mMusicDatas.size()){
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
        mSelectFilesManager.changFileTransferList(Constants.TYPE_MUSIC, isSelected, mMusicList, new IRefreshFileSelectedCallBack() {
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

}
