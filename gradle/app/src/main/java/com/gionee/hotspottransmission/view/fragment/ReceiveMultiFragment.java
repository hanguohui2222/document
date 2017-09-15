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
import com.gionee.hotspottransmission.view.viewholder.MultiReceiveViewHolder;
import com.gionee.hotspottransmission.view.SelectFilesActivity;

/**
 * Created by luorw on 5/23/17.
 */
public class ReceiveMultiFragment extends Fragment implements View.OnClickListener{
    private Context mContext;
    private BaseTransferActivity mActivity;
    private RelativeLayout mLayoutSendFiles;
    private RelativeLayout mLayoutCancelAll;
    private ImageView mIvSendFiles;
    private TextView mTvSendFiles;
    private ImageView mIvCancelAll;
    private TextView mTvCancelAll;
    private LinearLayout mLayoutBlankPage;
    private LinearLayout svMultiReceiveContent;

    public static ReceiveMultiFragment newInstance() {
        ReceiveMultiFragment pageFragment = new ReceiveMultiFragment();
        return pageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.i("ReceiveFragment-----onCreate");
        mContext = getActivity();
        mActivity = (BaseTransferActivity)getActivity();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_multi_receive, container, false);
        return view;
    }

    private void initView() {
        svMultiReceiveContent = (LinearLayout)getView().findViewById(R.id.scrollView_multi_receive_content);
        mLayoutBlankPage = (LinearLayout)getView().findViewById(R.id.blank_page_layout);
        mLayoutSendFiles = (RelativeLayout)getView().findViewById(R.id.rl_send);
        mLayoutCancelAll = (RelativeLayout)getView().findViewById(R.id.rl_clear);
        mIvSendFiles = (ImageView)getView().findViewById(R.id.iv_send);
        mIvCancelAll = (ImageView)getView().findViewById(R.id.iv_clear);
        mTvSendFiles = (TextView)getView().findViewById(R.id.tv_send);
        mTvCancelAll = (TextView)getView().findViewById(R.id.tv_clear);
        mLayoutSendFiles.setOnClickListener(this);
        mLayoutCancelAll.setOnClickListener(this);
        setSendFilesEnable(true,true);
    }

    public void setSendFilesEnable(boolean isEnable,boolean isVisible){
        if(isVisible){
            mLayoutSendFiles.setVisibility(View.VISIBLE);
        }else{
            mLayoutSendFiles.setVisibility(View.GONE);
        }
        mLayoutSendFiles.setClickable(isEnable);
        mIvSendFiles.setEnabled(isEnable);
        if(isEnable){
            mTvSendFiles.setTextColor(getResources().getColor(R.color.menu_enable_text_color));
        }else{
            mTvSendFiles.setTextColor(getResources().getColor(R.color.menu_disable_text_color));
        }
    }

    private void setCancelAllEnable(boolean isEnable,boolean isVisible){
        if(isVisible){
            mLayoutCancelAll.setVisibility(View.VISIBLE);
        }else{
            mLayoutCancelAll.setVisibility(View.GONE);
        }
        mLayoutCancelAll.setClickable(isEnable);
        mIvCancelAll.setEnabled(isEnable);
        if(isEnable){
            mTvCancelAll.setTextColor(getResources().getColor(R.color.menu_enable_text_color));
        }else{
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
//        if (!mFileReceiveData.isAllReceiveComplete() && isStartAmimation) {
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

//    public IDeviceCallBack mDeviceCallBack = new DeviceCallBackAdapter() {
//        @Override
//        public void onRefreshMenu(boolean isTransferable,boolean isVisible) {
//            setSendFilesEnable(isTransferable,isVisible);
//            setCancelAllEnable(!isTransferable,!isVisible);
//        }
//
//        @Override
//        public void onFullStorage() {
//            //更新所有未传输的文件为传输失败的状态，并保存到历史纪录
//            LogUtil.i("ReceiveFragment,onFullStorage---------------");
//            FileReceiveData.getInstance().updateDisconnectAllState();
//            mListAdapter.notifyDataSetChanged();
//            mReceiveListener.onTransferAllComplete();
//            mActivity.exit(true);
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
////            mHandler.post(new Runnable() {
////                @Override
////                public void run() {
////                    Toast.makeText(mContext, getResources().getString(R.string.connected_interrupt), Toast.LENGTH_SHORT).show();
////                    if (serverService != null && serverService.isAllComplete) {
////                        stopService(intent);
////                    }
////                    mActivity.exit(false);
////                }
////            });
//        }
//    };


    public MultiReceiveViewHolder addReceiveLayout(String key){
        LogUtil.i("luorw , addReceiveLayout --------------");
        mLayoutBlankPage.setVisibility(View.GONE);
        MultiReceiveViewHolder viewHolder = new MultiReceiveViewHolder(mContext,key,mActivity);
        View view = viewHolder.createReceiveLayout();
        svMultiReceiveContent.addView(view);
        return viewHolder;
    }

}
