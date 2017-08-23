package com.gionee.secretary.ui.fragment;


import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gionee.secretary.adapter.VoiceNoteAdapter;
import com.gionee.secretary.bean.VoiceNoteBean;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import com.gionee.secretary.R;
import com.gionee.secretary.dao.VoiceNoteDao;
import com.gionee.secretary.utils.LogUtils;
import com.gionee.secretary.utils.RemindUtils;
import com.gionee.secretary.ui.activity.NoteDetailActivity;

import amigoui.app.AmigoAlertDialog;

/**
 * Created by hangh on 6/4/16.
 */
public class VoiceNoteListFragment extends Fragment {
    private static final String TAG = "VoiceNoteListFragment";
    private VoiceNoteAdapter mAdapter;
    private List<VoiceNoteBean> mVoiceNoteBeanList;
    private static final int QUERY_OK = 1;
    private static final int DELETE_NOTE_SUCCESS = 2;
    private View mEmptyView;
    //private TextView mFloatTv;
    AmigoAlertDialog.Builder builder = null;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private boolean mShowDialog = false;
    AmigoAlertDialog dialog = null;
    private final MyHandler mHandler = new MyHandler(this);

    private static class MyHandler extends Handler{
        private final WeakReference<VoiceNoteListFragment> mFragment;
        public MyHandler(VoiceNoteListFragment fragment){
            mFragment = new WeakReference<VoiceNoteListFragment>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            final VoiceNoteListFragment voiceNoteListFragment = mFragment.get();
            if(voiceNoteListFragment != null){
                switch (msg.what) {
                    case QUERY_OK:
                        voiceNoteListFragment.mVoiceNoteBeanList = (List<VoiceNoteBean>) msg.obj;
                        voiceNoteListFragment.mAdapter.setVoiceNoteList(voiceNoteListFragment.mVoiceNoteBeanList);
                        break;
                    case DELETE_NOTE_SUCCESS:
                        voiceNoteListFragment.mVoiceNoteBeanList = (List<VoiceNoteBean>) msg.obj;
                        voiceNoteListFragment.mAdapter.setVoiceNoteList(voiceNoteListFragment.mVoiceNoteBeanList);
                }
            }
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_voice_note, null);
        initView(root);
        initData();
//        registerRefreshReceiver();
        return root;
    }

    private void initView(View root) {
        mRecyclerView = (RecyclerView) root.findViewById(R.id.recycle_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mEmptyView = (View) root.findViewById(R.id.empty_view);
        //mFloatTv = (TextView) root.findViewById(R.id.float_tv);
        //showTips();
    }

   /* private void showTips(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean showTips = sp.getBoolean(Constants.SHOWVOICETIPS, true);
        if(!showTips){
            mFloatTv.setVisibility(View.GONE);
            return;
        }
        mFloatTv.setVisibility(View.GONE);

    }*/


    private void initData() {
        mVoiceNoteBeanList = new ArrayList<VoiceNoteBean>();
        mAdapter = new VoiceNoteAdapter(getActivity(), mVoiceNoteBeanList);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(mOnItemClickListener);
        mAdapter.setOnItemLongClickListener(mOnItemLongClickListener);
        mAdapter.registerAdapterDataObserver(observer);
       /* mFloatTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean(Constants.SHOWVOICETIPS, false);
                editor.commit();
                mFloatTv.setVisibility(View.GONE);
            }
        });*/
    }

    private VoiceNoteAdapter.OnItemClickListener mOnItemClickListener = new VoiceNoteAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            Intent intent = new Intent();
            VoiceNoteBean bean = (VoiceNoteBean) mAdapter.getItem(position);
            intent.setClass(getActivity(), NoteDetailActivity.class);
            intent.putExtra("noteid", bean.getId());
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);
        }
    };

    private VoiceNoteAdapter.OnItemLongClickListener mOnItemLongClickListener = new VoiceNoteAdapter.OnItemLongClickListener() {
        @Override
        public void onItemLongClick(View view, final int position) {
            if (!mShowDialog) {
                builder = new AmigoAlertDialog.Builder(getActivity());
                builder.setTitle(getActivity().getString(R.string.delete));
                builder.setMessage(getActivity().getString(R.string.delete_voice_note));
                builder.setNegativeButton(R.string.cancel, null);
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mShowDialog = false;
                    }
                });
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mShowDialog = false;
                    }
                });
                builder.setPositiveButton(R.string.sec_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mAdapter.getItemCount() > 0 && mAdapter.getItem(position) != null) {
                            new DeleteThread(VoiceNoteListFragment.this, mAdapter.getItem(position)).start();
                        }
                        mShowDialog = false;
                    }
                });
                mShowDialog = true;
                builder.show();
            }

        }
    };

    private static class DeleteThread extends Thread {
        private WeakReference<VoiceNoteListFragment> mFragment;
        private VoiceNoteBean mNoteBean;

        public DeleteThread(VoiceNoteListFragment voiceNoteListFragment, VoiceNoteBean bean) {
            mFragment = new WeakReference<VoiceNoteListFragment>(voiceNoteListFragment);
            mNoteBean = bean;
        }

        @Override
        public void run() {
            final VoiceNoteListFragment fragment = mFragment.get();
            if(fragment != null){
                //added by luorw for GNSPR #70074 begin
                RemindUtils.noteAlarmCancel(fragment.getContext(), mNoteBean);
                //added by luorw for GNSPR #70074 end
                VoiceNoteDao.getInstance(fragment.getContext()).deleteOneNote(mNoteBean);
                List<VoiceNoteBean> beanList = VoiceNoteDao.getInstance(fragment.getContext()).getVoiceNoteListByDate();
                Message message = fragment.mHandler.obtainMessage(DELETE_NOTE_SUCCESS, beanList);
                fragment.mHandler.sendMessage(message);
            }

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtils.e(TAG, "onResume");
        new VoiceNoteQuery(this).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAdapter.unregisterAdapterDataObserver(observer);
    }

    private void showEmptyView(boolean isShow) {
        if (isShow) {
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.GONE);
        }
    }


    final RecyclerView.AdapterDataObserver observer = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            LogUtils.e(TAG, "onchanged");
            showEmptyView(mAdapter.getItemCount() == 0);
            super.onChanged();
        }
    };

    private static class VoiceNoteQuery extends Thread {
        private final WeakReference<VoiceNoteListFragment> mFragment;

        public VoiceNoteQuery(VoiceNoteListFragment voiceNoteListFragment) {
            mFragment = new WeakReference<VoiceNoteListFragment>(voiceNoteListFragment);
        }

        @Override
        public void run() {
            final VoiceNoteListFragment fragment = mFragment.get();
            if(fragment != null){
                List<VoiceNoteBean> beanList = VoiceNoteDao.getInstance(fragment.getContext()).getVoiceNoteListByDate();
                Message message = fragment.mHandler.obtainMessage(QUERY_OK, beanList);
                fragment.mHandler.sendMessage(message);
            }
        }
    }
}
