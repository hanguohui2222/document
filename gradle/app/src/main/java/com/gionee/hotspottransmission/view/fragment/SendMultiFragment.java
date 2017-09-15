package com.gionee.hotspottransmission.view.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.gionee.hotspottransmission.R;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.utils.LogUtil;
import com.gionee.hotspottransmission.view.BaseTransferActivity;
import com.gionee.hotspottransmission.view.SelectFilesActivity;
import com.gionee.hotspottransmission.view.viewholder.MultiSendViewHolder;

/**
 * Created by luorw on 5/23/17.
 */
public class SendMultiFragment extends Fragment implements View.OnClickListener {
    private Context mContext;
    private BaseTransferActivity mActivity;
    //    private boolean isTransferBreak = false;
    private RelativeLayout mLayoutSendFiles;
    private RelativeLayout mLayoutCancelAll;
    private ImageView mIvSendFiles;
    private TextView mTvSendFiles;
    private ImageView mIvCancelAll;
    private TextView mTvCancelAll;
    private LinearLayout mLayoutBlankPage;
    private LinearLayout svMultiSendContent;

    public static SendMultiFragment newInstance() {
        SendMultiFragment pageFragment = new SendMultiFragment();
        return pageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.i("SendFragment-----onCreate");
        mContext = getActivity();
        mActivity = (BaseTransferActivity) getActivity();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_multi_send, container, false);
        return view;
    }

    private void initView() {
        svMultiSendContent = (LinearLayout) getView().findViewById(R.id.scrollView_multi_send_content);
        mLayoutBlankPage = (LinearLayout)getView().findViewById(R.id.blank_page_layout);
        mLayoutSendFiles = (RelativeLayout) getView().findViewById(R.id.rl_send);
        mLayoutCancelAll = (RelativeLayout) getView().findViewById(R.id.rl_clear);
        mIvSendFiles = (ImageView) getView().findViewById(R.id.iv_send);
        mIvCancelAll = (ImageView) getView().findViewById(R.id.iv_clear);
        mTvSendFiles = (TextView) getView().findViewById(R.id.tv_send);
        mTvCancelAll = (TextView) getView().findViewById(R.id.tv_clear);
        mLayoutSendFiles.setOnClickListener(this);
//        setSendFilesEnable(false,true);
//        if (getActivity() instanceof GoTransferActivity) {
//            mLayoutBlankPage.setVisibility(View.GONE);
//        } else {
//            mPbLoading.setVisibility(View.GONE);
//        }
    }


    public void setSendFilesEnable(boolean isEnable, boolean isVisible) {
        if (isVisible) {
            mLayoutSendFiles.setVisibility(View.VISIBLE);
        } else {
            mLayoutSendFiles.setVisibility(View.GONE);
        }
        mLayoutSendFiles.setClickable(isEnable);
        mIvSendFiles.setEnabled(isEnable);
        if (isEnable) {
            mTvSendFiles.setTextColor(getResources().getColor(R.color.menu_enable_text_color));
        } else {
            mTvSendFiles.setTextColor(getResources().getColor(R.color.menu_disable_text_color));
        }
    }

    private void setCancelAllEnable(boolean isEnable, boolean isVisible) {
        if (isVisible) {
            mLayoutCancelAll.setVisibility(View.VISIBLE);
        } else {
            mLayoutCancelAll.setVisibility(View.GONE);
        }
        mLayoutCancelAll.setClickable(isEnable);
        mIvCancelAll.setEnabled(isEnable);
        if (isEnable) {
            mTvCancelAll.setTextColor(getResources().getColor(R.color.menu_enable_text_color));
        } else {
            mTvCancelAll.setTextColor(getResources().getColor(R.color.menu_disable_text_color));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_send:
                startSend();
                break;
            default:
                break;
        }
    }

    private void startSend() {
        Intent intent = new Intent();
        intent.setClass(mContext, SelectFilesActivity.class);
        intent.setAction(Constants.ACTION_MULTI_SEND_FILES);
        intent.putExtra(Constants.IS_GROUP_OWNER, mActivity.getMultiService().isGroupOwner());
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
//        isStartNotification = false;
//        cancelNoti();
//        if (!FileSendData.getInstance().isAllSendComplete() && isStartAmimation) {
//            mHandler.post(mFileSendRunnable);
//        }
    }

    @Override
    public void onStop() {
        super.onStop();
//        isStartNotification = true;
//        startNoti();
//        mHandler.removeCallbacks(mFileSendRunnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        mHandler.removeCallbacks(mFileSendRunnable);
    }

//    public void refreshForResend() {
//        mtvTotalProgress.setText(getResources().getString(R.string.file_aready_transfer) + "0B" + getResources().getString(R.string.files));
//        mNumberProgressBar.setProgress(0);
//        FileSendData.getInstance().clearAllFiles();
//        mListAdapter.notifyDataSetChanged();
//        FileSendData.getInstance().setCancelAllSend(false);
//        isStartAmimation = false;
//    }

    //    public IDeviceCallBack mDeviceCallBack = new DeviceCallBackAdapter() {
//        @Override
//        public void onRefreshMenu(boolean isTransferable,boolean isVisible) {
//            setSendFilesEnable(isTransferable,isVisible);
//            setCancelAllEnable(!isTransferable,!isVisible);
//        }
//
//        @Override
//        public void onWifiUnAvailable() {
//            Toast.makeText(mContext, getResources().getString(R.string.connected_interrupt), Toast.LENGTH_SHORT).show();
//            mActivity.exit(false);
//        }
//
//        @Override
//        public void onExit() {
//            //added by luorw for GNSPR #41659 begin
////            SharedPreferences sharedPreferences = getSharedPreferences(Constants.CONNECT_STATUS, Context.MODE_PRIVATE);
////            SharedPreferences.Editor editor = sharedPreferences.edit();
////            editor.putBoolean(Constants.IS_CONNECTED, false);
////            editor.commit();
//            //added by luorw for GNSPR #41659 end
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
////                    if (clientService != null && clientService.isAllComplete) {
//                        LogUtil.i("SendFragment,客户端传输已经中断---------");
//                        //toast放在内部，避免多次弹出toast
//                        Toast.makeText(mContext, getResources().getString(R.string.connected_interrupt), Toast.LENGTH_SHORT).show();
//                        if(mActivity != null){
//                            mActivity.exit(false);
//                        }
////                    }
//                }
//            });
//        }
//    };
    public MultiSendViewHolder addSendLayout(String key) {
        mLayoutBlankPage.setVisibility(View.GONE);
        MultiSendViewHolder viewHolder = new MultiSendViewHolder(mContext, key,mActivity);
        View view = viewHolder.createSendLayout();
        svMultiSendContent.addView(view);
        return viewHolder;
    }

}
