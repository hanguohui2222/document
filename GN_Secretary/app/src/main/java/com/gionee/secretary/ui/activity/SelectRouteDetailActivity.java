package com.gionee.secretary.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.services.route.BusPath;
import com.gionee.secretary.R;
import com.gionee.secretary.adapter.SelectRouteDetailAdapter;

import amigoui.app.AmigoActionBar;

public class SelectRouteDetailActivity extends PasswordBaseActivity {

    RecyclerView mRvRouteDetail;
    SelectRouteDetailAdapter mRvRouteDetailAdapter;
    LinearLayoutManager mRvRouteDetailLayoutManager;

    //RouteDetail Field
    Object mRouteDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_route_detail);
        initView();
        handleIntentData();

    }

    private void initView() {
        //Init Actionbar
        AmigoActionBar actionBar = getAmigoActionBar();
        RelativeLayout actionBarLayout = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.actionbar_navigation, null);
        TextView title = (TextView) actionBarLayout.findViewById(R.id.tv_title);
        ImageButton btnBack = (ImageButton) actionBarLayout.findViewById(R.id.btn_back);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(actionBarLayout);
        title.setVisibility(View.VISIBLE);
        title.setText("路线信息");
        btnBack.setVisibility(View.VISIBLE);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Init RecyclerView
        mRvRouteDetail = (RecyclerView) findViewById(R.id.rv_route_detail);
        mRvRouteDetailLayoutManager = new LinearLayoutManager(this);
        mRvRouteDetail.setLayoutManager(mRvRouteDetailLayoutManager);
    }

    private void handleIntentData() {
        Intent intent = getIntent();
        mRouteDetail = intent.getParcelableExtra("ROUTE_DETAIL");
        if (mRouteDetail != null) {
            if (mRvRouteDetailAdapter == null) {
                if (mRouteDetail instanceof BusPath) {
                    mRvRouteDetailAdapter = new SelectRouteDetailAdapter(this, ((BusPath) mRouteDetail).getSteps());
                    mRvRouteDetail.setAdapter(mRvRouteDetailAdapter);
                    mRvRouteDetailAdapter.notifyDataSetChanged();
                }
            }
        }
    }


}
