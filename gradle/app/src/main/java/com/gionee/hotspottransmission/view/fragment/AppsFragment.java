package com.gionee.hotspottransmission.view.fragment;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;

import com.gionee.hotspottransmission.R;
import com.gionee.hotspottransmission.adapter.FileListAdapter;
import com.gionee.hotspottransmission.bean.FileInfo;
import com.gionee.hotspottransmission.bean.FileSendData;
import com.gionee.hotspottransmission.callback.IRefreshFileSelectedCallBack;
import com.gionee.hotspottransmission.callback.ISelectSortFiles;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.manager.SelectFilesManager;
import com.gionee.hotspottransmission.manager.ThreadPoolManager;
import com.gionee.hotspottransmission.utils.LogUtil;
import com.gionee.hotspottransmission.callback.ISelectFiles;
import com.gionee.hotspottransmission.view.SelectFilesActivity;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AppsFragment extends Fragment implements SelectFilesActivity.IBackPressedListener, AdapterView.OnItemClickListener, ISelectSortFiles {
    private final String TAG = "AppsFragment";
    //默认除法运算精度
    private static final int DEF_DIV_SCALE = 10;
    private GridView mGvAppInfo;
    public List<FileInfo> mListAppInfo = new ArrayList<>();
    private FileListAdapter mFileListadapter;
    private SelectFilesManager mSelectFilesManager;
    private ImageView mSelectBg;
    private CheckBox mSelect;
    private Context mContext;
    private boolean isSelectAll;

    public void setIsSelectAll(boolean mIsSelectAll) {
        this.isSelectAll = mIsSelectAll;
    }

    public boolean isSelectAll() {
        return isSelectAll;
    }

    public static AppsFragment newInstance() {
        AppsFragment pageFragment = new AppsFragment();
        return pageFragment;
    }

    public FileListAdapter getmFileListadapter() {
        return mFileListadapter;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.i("AppsFragment-----onCreate");
        mContext = getActivity();
        mSelectFilesManager = new SelectFilesManager();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initView();
        initAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_apps, container, false);
        return view;
    }

    private void initView() {
        mGvAppInfo = (GridView) getView().findViewById(R.id.apps_gridview);
        mGvAppInfo.setOnItemClickListener(this);
    }

    private void initAdapter() {
        // #18777 start
        if (((SelectFilesActivity) getActivity()).mAppsFragment != this) {
            ((SelectFilesActivity) getActivity()).mAppsFragment = this;
        }
        //#18777 end
        mFileListadapter = new FileListAdapter(getActivity(), mListAppInfo, imageHandler);
        mGvAppInfo.setAdapter(mFileListadapter);
    }

    public Handler imageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.SET_IMAGE_BITMAP:
                    ImageView imageView = (ImageView) mGvAppInfo.findViewWithTag(msg.arg1);
                    if (imageView != null) {
                        imageView.setImageBitmap((Bitmap) msg.obj);
                    }
                    break;
            }
        }
    };

    @Override
    public boolean onBack() {
        return false;
    }

    private List<PackageInfo> getInstalledPackages() {
        List<PackageInfo> packageInfoList = new ArrayList<PackageInfo>();
        try {
            packageInfoList = getActivity().getPackageManager().getInstalledPackages(PackageManager.GET_INSTRUMENTATION);
        } catch (Exception e) {
        }
        return packageInfoList;
    }

    private void initData() {
        ThreadPoolManager.getInstance().executeRunnable(new Runnable() {
            @Override
            public void run() {
                List<PackageInfo> allPackages = getInstalledPackages();
                if (allPackages.size() == 0) {
                    return;
                }
                //added by luorw for GNSPR #30747 begin
                if (getActivity() == null) {
                    return;
                }
                //added by luorw for GNSPR #30747 end
                PackageManager packageManager = getActivity().getPackageManager();
                for (PackageInfo packageInfo : allPackages) {
                    if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0
                            && (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0) {
                        FileInfo fileInfo = new FileInfo();
                        fileInfo.setFileName((String) packageManager.getApplicationLabel(packageInfo.applicationInfo));
                        fileInfo.setFileSize(getApkSizeMap(packageInfo));
                        fileInfo.setModifiedDate(packageInfo.lastUpdateTime);
                        fileInfo.setFilePath(packageInfo.applicationInfo.publicSourceDir);
                        fileInfo.setFileType(Constants.TYPE_APPS);
                        fileInfo.setLoaderId(Constants.TYPE_APPS);
                        File file = new File(packageInfo.applicationInfo.publicSourceDir);
                        Uri uri = Uri.fromFile(file);
                        fileInfo.setUriString(uri.toString());
                        mListAppInfo.add(fileInfo);
                    }
                }
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mFileListadapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FileInfo fileInfo = mListAppInfo.get(position);
        mSelect = (CheckBox) view.findViewById(R.id.item_app_checkbox);
        mSelectBg = (ImageView) view.findViewById(R.id.item_iv_app_checkbox_bg);
        selectedItem(!mSelect.isChecked(), fileInfo);
    }

    /**
     * 获取文件大小
     *
     * @param appInfo
     * @return
     */
    private long getApkSizeMap(PackageInfo appInfo) {
        String apkPath = appInfo.applicationInfo.publicSourceDir;
        long apkSize = 0l;
        if (!TextUtils.isEmpty(apkPath)) {
            File file = new File(apkPath);
            long length = file.length();// byte
            if (length != 0) {
                apkSize = file.length();
            }
        }
        return apkSize;
    }


    /**
     * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指 定精度,目前scale=10，以后的数字四舍五入。
     *
     * @param v1    被除数
     * @param v2    除数
     * @param scale 表示表示需要精确到小数点以后几位。
     * @return 两个参数的商
     */
    private double div(double v1, double v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 提供精确的类型转换(Long)
     *
     * @param v 需要被转换的数字
     * @return 返回转换结果
     */
    public static long convertsToLong(double v) {
        BigDecimal b = new BigDecimal(v);
        return b.longValue();
    }

    @Override
    public void selectedItem(boolean isSelected, FileInfo info) {
        mSelectFilesManager.changFileTransferList(isSelected, info, new IRefreshFileSelectedCallBack() {
            @Override
            public void onRefreshCount(List<FileInfo> list) {
                if (getActivity() instanceof ISelectFiles) {
                    ((ISelectFiles) getActivity()).refreshMenu(list);
                    if(mListAppInfo.size() == FileSendData.getInstance().mAppDatas.size()){
                        isSelectAll = true;
                    }else{
                        isSelectAll = false;
                    }
                    ((ISelectFiles) getActivity()).refreshSelectAllText();
                }
            }

            @Override
            public void onRefreshSelected(boolean isSelected) {
                mSelect.setChecked(isSelected);
                if (isSelected) {
                    mSelect.setVisibility(View.VISIBLE);
                    mSelectBg.setVisibility(View.VISIBLE);
                } else {
                    mSelect.setVisibility(View.GONE);
                    mSelectBg.setVisibility(View.GONE);
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
        mSelectFilesManager.changFileTransferList(Constants.TYPE_APPS,isSelected, mListAppInfo, new IRefreshFileSelectedCallBack() {
            @Override
            public void onRefreshCount(List<FileInfo> list) {
                if (getActivity() instanceof ISelectFiles) {
                    ((ISelectFiles) getActivity()).refreshMenu(list);
                }
            }

            //刷新列表中全部view
            @Override
            public void onRefreshSelected(boolean isSelected) {
                mFileListadapter.notifyDataSetChanged();
            }

            @Override
            public void onShowSelected(List<FileInfo> list) {

            }
        });
    }
}


