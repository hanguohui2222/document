package com.gionee.secretary.ui.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.gionee.secretary.R;
import com.gionee.secretary.adapter.TrackAdapter;
import com.gionee.secretary.bean.ExpressSchedule;
import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.utils.CardDetailsUtils;
import com.gionee.secretary.utils.KdniaoTrackQueryAPI;
import com.gionee.secretary.utils.NetWorkUtil;
import com.gionee.secretary.ui.activity.CardDetailsActivity;
import com.gionee.secretary.widget.ListViewForScrollView;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

import amigoui.widget.AmigoProgressBar;

/**
 * Created by zhuboqin on 26/05/16.
 */
public class ExpressDetailsFragment extends Fragment {
    private static final String TAG = "ExpressDetailsFragment";
    private ListViewForScrollView lv_tracklist;
    ExpressSchedule schedule;
    Context mContext;
    private static final int REQUEST_EXPRESS = 501;
    private static final int REQUEST_UI = 502;
    private TextView tv_express_company;
    private TextView tv_express_num;
    private TextView track_reason;
    private ImageView iv_start;
    private ImageView iv_on_the_way;
    private ImageView iv_end;
    private RelativeLayout rl_no_network;
    private TextView click_load;
    private AmigoProgressBar pg;
    private boolean net_work;
    private int mainStatus;
    private ScrollView mScrollView;
    private final MyHandler mHandler = new MyHandler(this);

    public ExpressDetailsFragment() {
    }

    public static ExpressDetailsFragment newInstance(ExpressSchedule schedule) {
        ExpressDetailsFragment fragment = new ExpressDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.SCHEDULE_KEY, schedule);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CardDetailsActivity activity = (CardDetailsActivity) getActivity();
        mContext = activity;
        schedule = (ExpressSchedule) activity.getSchedule();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_express_details, container, false);
        initView(root);
        net_work = NetWorkUtil.isNetworkAvailable(getActivity());
        if (net_work) {
            requestTrace();
            pg.setVisibility(View.VISIBLE);
            rl_no_network.setVisibility(View.GONE);
        } else {
            rl_no_network.setVisibility(View.VISIBLE);
            pg.setVisibility(View.GONE);
        }
        if(schedule != null){
            mainStatus = schedule.getState();
            setUIState(schedule);
        }
        return root;
    }

    private void initView(View root) {
        lv_tracklist = (ListViewForScrollView) root.findViewById(R.id.tracklist);
        tv_express_company = (TextView) root.findViewById(R.id.tv_express_company);
        tv_express_num = (TextView) root.findViewById(R.id.tv_express_num);
        track_reason = (TextView) root.findViewById(R.id.track_reason);
        click_load = (TextView) root.findViewById(R.id.click_load);
        iv_start = (ImageView) root.findViewById(R.id.iv_start);
        iv_on_the_way = (ImageView) root.findViewById(R.id.iv_on_the_way);
        iv_end = (ImageView) root.findViewById(R.id.iv_end);
        rl_no_network = (RelativeLayout) root.findViewById(R.id.rl_no_network);
        pg = (AmigoProgressBar) root.findViewById(R.id.pg);
        if (schedule != null) {
            if (!CardDetailsUtils.isEmpty(schedule.getExpressCompany())) {
                tv_express_company.setText(schedule.getExpressCompany());
            }
            tv_express_num.setText(schedule.getExpressNum());
        }
        click_load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestTrace();
                rl_no_network.setVisibility(View.GONE);
                pg.setVisibility(View.VISIBLE);
            }
        });
        mScrollView = (ScrollView) root.findViewById(R.id.express_scroll_view);
    }

    public ScrollView getmScrollView() {
        return mScrollView;
    }

    private String shipperName = "";

    private void requestTrace() {
        net_work = NetWorkUtil.isNetworkAvailable(getActivity());
        if (!net_work) {
            rl_no_network.setVisibility(View.VISIBLE);
            pg.setVisibility(View.GONE);
            return;
        }
        new GetTraceThread(this).start();
    }

    private static class GetTraceThread extends Thread{
        private WeakReference<ExpressDetailsFragment> mFragment;
        public GetTraceThread(ExpressDetailsFragment expressDetailsFragment){
            mFragment = new WeakReference<ExpressDetailsFragment>(expressDetailsFragment);
        }

        @Override
        public void run() {
            ExpressDetailsFragment fragment = mFragment.get();
            if(fragment != null){
                KdniaoTrackQueryAPI api = new KdniaoTrackQueryAPI();
                try {
                    if(fragment.schedule == null)
                        return;
                    String expCode = fragment.schedule.getExpressCode();
                    String expID = fragment.schedule.getExpressNum();

                    if (CardDetailsUtils.isEmpty(expCode)) {
                        String codeResult = api.getOrderShipperCode(fragment.schedule.getExpressNum());
                        JSONObject jsonObject = new JSONObject(codeResult);
                        JSONArray jsonArray = jsonObject.getJSONArray("Shippers");
                        if (jsonArray != null && jsonArray.length() > 0) {
                            JSONObject jShipper = (JSONObject) jsonArray.get(0);
                            expCode = jShipper.getString("ShipperCode");
                            fragment.shipperName = jShipper.getString("ShipperName");
                        }
                        Message message = fragment.mHandler.obtainMessage(REQUEST_UI,fragment.shipperName);
                        fragment.mHandler.sendMessage(message);
                    }
                    String result = api.getOrderTracesByJson(expCode, expID);
                    Message message = fragment.mHandler.obtainMessage();
                    message.obj = result;
                    message.what = REQUEST_EXPRESS;
                    fragment.mHandler.sendMessage(message);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            super.run();
        }
    }

    private static class MyHandler extends Handler{
        private final WeakReference<ExpressDetailsFragment> mFragment;
        public MyHandler(ExpressDetailsFragment fragment){
            mFragment = new WeakReference<ExpressDetailsFragment>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            ExpressDetailsFragment expressDetailsFragment = mFragment.get();
            if(expressDetailsFragment != null){
                switch (msg.what) {
                    case REQUEST_EXPRESS:
                        try {
                            String result = (String) msg.obj;
                            Gson gson = new Gson();
                            ExpressSchedule expressBean = gson.fromJson(result, ExpressSchedule.class);
                            expressDetailsFragment.setListAdapter(expressBean);
                        } catch (JsonSyntaxException e) {
                            e.printStackTrace();
                            Toast.makeText(expressDetailsFragment.mContext,"文件格式出现了异常",Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case REQUEST_UI:
                        expressDetailsFragment.tv_express_company.setText((String)msg.obj);
                }
            }
            super.handleMessage(msg);
        }
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    public void setListAdapter(ExpressSchedule expressBean) {
        if (null != expressBean) {
            if (!TextUtils.isEmpty(expressBean.getReason())) {
                track_reason.setVisibility(View.VISIBLE);
                lv_tracklist.setVisibility(View.GONE);
                track_reason.setText(expressBean.getReason());
            } else {
                track_reason.setVisibility(View.GONE);
                lv_tracklist.setVisibility(View.VISIBLE);
                lv_tracklist.setAdapter(new TrackAdapter(mContext, expressBean.getTraces()));
            }
            rl_no_network.setVisibility(View.GONE);
            pg.setVisibility(View.GONE);
            if (mainStatus != expressBean.getState() && getActivity() != null) {
                getActivity().sendBroadcast(new Intent(Constants.REFRESH_FOR_MAIN_UI));
            }
            setUIState(expressBean);
        } else {
            rl_no_network.setVisibility(View.VISIBLE);
            pg.setVisibility(View.GONE);
        }
    }

    private void setUIState(ExpressSchedule expressBean) {
        if (expressBean != null) {
            StringBuilder stringBuilder = new StringBuilder();
            if (expressBean.getTraces() != null) {
                for (ExpressSchedule.Trace trace : expressBean.getTraces()) {
                    stringBuilder.append(trace.getAcceptStation());
                }
            }
            if (expressBean.getState() == 3 || (expressBean.getState() == 4 && stringBuilder.toString().contains("已签收"))) { //签收
                iv_end.setImageResource(R.drawable.express_now);
                iv_on_the_way.setImageResource(R.drawable.express_past);
                iv_start.setImageResource(R.drawable.express_past);
            } else if (expressBean.getState() == 2 || expressBean.getState() == 4) {
                iv_on_the_way.setImageResource(R.drawable.express_now);
                iv_end.setImageResource(R.drawable.express_past);
                iv_start.setImageResource(R.drawable.express_past);
            } else {
                iv_start.setImageResource(R.drawable.express_past);
                iv_on_the_way.setImageResource(R.drawable.express_past);
                iv_end.setImageResource(R.drawable.express_past);
            }
        }
    }
}
