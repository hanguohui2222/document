package com.gionee.hotspottransmission.view.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.gionee.hotspottransmission.adapter.FileListAdapter;
import com.gionee.hotspottransmission.adapter.FilesGridAdapter;
import com.gionee.hotspottransmission.bean.FileInfo;
import com.gionee.hotspottransmission.callback.IRefreshFileSelectedCallBack;
import com.gionee.hotspottransmission.callback.ISelectSortFiles;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.dao.FileDao;
import com.gionee.hotspottransmission.callback.IDataQueryCallBack;
import com.gionee.hotspottransmission.manager.SelectFilesManager;
import com.gionee.hotspottransmission.callback.ISelectFiles;
import com.gionee.hotspottransmission.view.SelectFilesActivity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.gionee.hotspottransmission.R;
import amigoui.app.AmigoProgressDialog;

public class FilesFragment extends Fragment implements SelectFilesActivity.IBackPressedListener, ISelectSortFiles, OnItemClickListener {
    private final String TAG = "FilesFragment";
    private GridView mPanel;
    private LinearLayout mNaviTab;
    private final static int FIRST_PAGE = 0;
    private final static int SECOND_PAGE = 1;
    private int mCurrentPage = FIRST_PAGE;
    ListView mDocumentListview;
    ListView mCompressListview;
    ListView mEbookListview;
    ListView mAPKListview;
    private FileListAdapter mDocumentAdapter;
    private FileListAdapter mCompressAdapter;
    private FileListAdapter mEbookAdapter;
    private FileListAdapter mAPKAdapter;
    private CheckBox mSelect;
    private FileDao dao;
    private TextView mFirstDirBtn;
    private TextView mSecondDirBtn;
    private SelectFilesManager mSelectFilesManager;
    private Map<Integer, List<FileInfo>> mDataInfoMap;
    private LinearLayout layoutNoFiles;
    private AmigoProgressDialog pd;

    public static FilesFragment newInstance() {
        FilesFragment pageFragment = new FilesFragment();
        return pageFragment;
    }

    public Handler imageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.SET_IMAGE_BITMAP:
                    ImageView imageView = (ImageView) mAPKListview.findViewWithTag(msg.arg1);
                    if (imageView != null) {
                        imageView.setImageBitmap((Bitmap) msg.obj);
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
        View root = inflater.inflate(R.layout.fragment_files, container, false);
        mSelectFilesManager = new SelectFilesManager();
        initview(root);
        initListener();
        swithToPanel(FIRST_PAGE);
        dao = new FileDao(getActivity(), new IDataQueryCallBack() {
            @Override
            public void onQueryEnd(List<FileInfo> fileInfoList) {
                if(pd!=null){
                    pd.dismiss();
                }
                if (fileInfoList.size() > 0) {
                    int loaderId = fileInfoList.get(0).getLoaderId();
                    mDataInfoMap.put(loaderId, fileInfoList);
                    layoutNoFiles.setVisibility(View.GONE);
                    mDocumentListview.setVisibility(View.GONE);
                    mCompressListview.setVisibility(View.GONE);
                    mEbookListview.setVisibility(View.GONE);
                    mAPKListview.setVisibility(View.GONE);
                    // #18777 start
                    if(((SelectFilesActivity)getActivity()).mFilesFragment != FilesFragment.this){
                        ((SelectFilesActivity)getActivity()).mFilesFragment = FilesFragment.this;
                    }
                    // #18777 end
                    switch (loaderId) {
                        case Constants.TYPE_DOCUMENT:
                            mDocumentListview.setVisibility(View.VISIBLE);
                            mDocumentAdapter = new FileListAdapter(getActivity(), fileInfoList, null);
                            mDocumentListview.setAdapter(mDocumentAdapter);
                            break;
                        case Constants.TYPE_COMPRESS:
                            mCompressListview.setVisibility(View.VISIBLE);
                            mCompressAdapter = new FileListAdapter(getActivity(), fileInfoList, null);
                            mCompressListview.setAdapter(mCompressAdapter);
                            break;
                        case Constants.TYPE_EBOOK:
                            mEbookListview.setVisibility(View.VISIBLE);
                            mEbookAdapter = new FileListAdapter(getActivity(), fileInfoList, null);
                            mEbookListview.setAdapter(mEbookAdapter);
                            break;
                        case Constants.TYPE_APK:
                            mAPKListview.setVisibility(View.VISIBLE);
                            mAPKAdapter = new FileListAdapter(getActivity(), fileInfoList, imageHandler);
                            mAPKListview.setAdapter(mAPKAdapter);
                            break;
                    }
                } else {
                    layoutNoFiles.setVisibility(View.VISIBLE);
                    //added by luorw for  GNSPR #20358 begin
                    mDocumentListview.setVisibility(View.GONE);
                    mCompressListview.setVisibility(View.GONE);
                    mEbookListview.setVisibility(View.GONE);
                    mAPKListview.setVisibility(View.GONE);
                    //added by luorw for  GNSPR #20358 end
                }
            }
        });
        return root;
    }

    private void initview(View root) {
        mNaviTab = (LinearLayout) root.findViewById(R.id.navi_tab);
        mFirstDirBtn = (TextView) root.findViewById(R.id.first_dir);
        mSecondDirBtn = (TextView) root.findViewById(R.id.second_dir);
        mDocumentListview = (ListView) root.findViewById(R.id.documentlistview);
        mCompressListview = (ListView) root.findViewById(R.id.compresslistview);
        mEbookListview = (ListView) root.findViewById(R.id.ebooklistview);
        mAPKListview = (ListView) root.findViewById(R.id.apklistview);
        layoutNoFiles = (LinearLayout) root.findViewById(R.id.layout_no_files);
        mPanel = (GridView) root.findViewById(R.id.grid_file);
        mPanel.setAdapter(new FilesGridAdapter(getActivity()));
        mDataInfoMap = new ArrayMap<Integer, List<FileInfo>>();
        mPanel.setOnItemClickListener(this);
    }

    private void showSubListView(int loaderId) {
        swithToPanel(SECOND_PAGE);
        if (mDataInfoMap.containsKey(loaderId) && mDataInfoMap.get(loaderId) != null) {
            if (mDataInfoMap.get(loaderId).size() != 0) {
                layoutNoFiles.setVisibility(View.GONE);
                mDocumentListview.setVisibility(View.GONE);
                mCompressListview.setVisibility(View.GONE);
                mEbookListview.setVisibility(View.GONE);
                mAPKListview.setVisibility(View.GONE);
                switch (loaderId) {
                    case Constants.TYPE_DOCUMENT:
                        mDocumentListview.setVisibility(View.VISIBLE);
                        mSecondDirBtn.setText("文档");
                        break;
                    case Constants.TYPE_COMPRESS:
                        mCompressListview.setVisibility(View.VISIBLE);
                        mSecondDirBtn.setText("压缩");
                        break;
                    case Constants.TYPE_EBOOK:
                        mEbookListview.setVisibility(View.VISIBLE);
                        mSecondDirBtn.setText("电子书");
                        break;
                    case Constants.TYPE_APK:
                        mAPKListview.setVisibility(View.VISIBLE);
                        mSecondDirBtn.setText("安装包");
                        break;
                    default:
                        break;
                }
            } else {
                layoutNoFiles.setVisibility(View.VISIBLE);
            }
        } else {
            if (pd == null) {
                pd = new AmigoProgressDialog(getActivity());
            }
            pd.setMessage("正在加载...");
            pd.setCanceledOnTouchOutside(false);
            pd.setCancelable(false);
            pd.show();
            dao.queryFileData(loaderId);
            switch (loaderId) {
                case Constants.TYPE_DOCUMENT:
                    mSecondDirBtn.setText("文档");
                    break;
                case Constants.TYPE_COMPRESS:
                    mSecondDirBtn.setText("压缩");
                    break;
                case Constants.TYPE_EBOOK:
                    mSecondDirBtn.setText("电子书");
                    break;
                case Constants.TYPE_APK:
                    mSecondDirBtn.setText("安装包");
                    break;
                default:
                    break;
            }
        }
    }

    public void refreshSelectedItemAdapter(FileInfo fileInfo) {
        switch (fileInfo.getLoaderId()) {
            case Constants.TYPE_DOCUMENT:
                if (mDocumentAdapter != null) {
                    mDocumentAdapter.notifyDataSetInvalidated();
                }
                break;
            case Constants.TYPE_COMPRESS:
                if (mCompressAdapter != null) {
                    mCompressAdapter.notifyDataSetInvalidated();
                }
                break;
            case Constants.TYPE_EBOOK:
                if (mEbookAdapter != null) {
                    mEbookAdapter.notifyDataSetInvalidated();
                }
                break;
            case Constants.TYPE_APK:
                if (mAPKAdapter != null) {
                    mAPKAdapter.notifyDataSetInvalidated();
                }
                break;
            default:
                break;
        }
    }

    public void refreshAllAdapter() {
        if (mDocumentAdapter != null) {
            mDocumentAdapter.notifyDataSetInvalidated();
        }
        if (mCompressAdapter != null) {
            mCompressAdapter.notifyDataSetInvalidated();
        }
        if (mEbookAdapter != null) {
            mEbookAdapter.notifyDataSetInvalidated();
        }
        if (mAPKAdapter != null) {
            mAPKAdapter.notifyDataSetInvalidated();
        }
    }

    private void initListener() {
        mDocumentListview.setOnItemClickListener(this);
        mCompressListview.setOnItemClickListener(this);
        mEbookListview.setOnItemClickListener(this);
        mAPKListview.setOnItemClickListener(this);
    }

    @Override
    public boolean onBack() {
        if (mCurrentPage == FIRST_PAGE) {
            return false;
        } else if (mCurrentPage == SECOND_PAGE) {
            swithToPanel(FIRST_PAGE);
            return true;
        }
        return true;
    }

    private void swithToPanel(int page) {
        switch (page) {
            case FIRST_PAGE:
                layoutNoFiles.setVisibility(View.GONE);
                mPanel.setVisibility(View.VISIBLE);
                mNaviTab.setVisibility(View.GONE);
                mDocumentListview.setVisibility(View.GONE);
                mEbookListview.setVisibility(View.GONE);
                mAPKListview.setVisibility(View.GONE);
                mCompressListview.setVisibility(View.GONE);
                mCurrentPage = FIRST_PAGE;
                break;
            case SECOND_PAGE:
                mPanel.setVisibility(View.GONE);
                mNaviTab.setVisibility(View.VISIBLE);
                mCurrentPage = SECOND_PAGE;
                break;
        }
    }


    @Override
    public void selectedItem(boolean isSelected, FileInfo info) {
        mSelectFilesManager.changFileTransferList(isSelected, info, new IRefreshFileSelectedCallBack() {
            @Override
            public void onRefreshCount(List<FileInfo> list) {
                if (getActivity() instanceof ISelectFiles) {
                    ((ISelectFiles) getActivity()).refreshMenu(list);
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

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FileInfo info = null;
        switch (parent.getId()) {
            case R.id.grid_file:
                mNaviTab.setVisibility(View.VISIBLE);
                mFirstDirBtn.setText(getResources().getString(R.string.files));
                mFirstDirBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        swithToPanel(FIRST_PAGE);
                    }
                });
                showSubListView(position + 5);
                break;
            case R.id.documentlistview:
                info = (FileInfo) mDocumentAdapter.getItem(position);
                mSelect = (CheckBox) view.findViewById(R.id.file_checkbox);
                selectedItem(!mSelect.isChecked(), info);
                break;
            case R.id.compresslistview:
                info = (FileInfo) mCompressAdapter.getItem(position);
                mSelect = (CheckBox) view.findViewById(R.id.file_checkbox);
                selectedItem(!mSelect.isChecked(), info);
                break;
            case R.id.ebooklistview:
                info = (FileInfo) mEbookAdapter.getItem(position);
                mSelect = (CheckBox) view.findViewById(R.id.file_checkbox);
                selectedItem(!mSelect.isChecked(), info);
                break;
            case R.id.apklistview:
                info = (FileInfo) mAPKAdapter.getItem(position);
                mSelect = (CheckBox) view.findViewById(R.id.file_checkbox);
                selectedItem(!mSelect.isChecked(), info);
                break;
        }
    }
}
