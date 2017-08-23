package com.gionee.secretary.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amap.api.services.route.BusPath;
import com.gionee.secretary.R;
import com.gionee.secretary.utils.AMapUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liyingheng on 2/3/17.
 */

public class RouteResultAdapter extends RecyclerView.Adapter<RouteResultAdapter.RouteResultViewHolder> {


    private static final String LOG_TAG = RouteResultAdapter.class.getSimpleName();

    private Context mContext;
    private List resultData;
    private OnItemClickListener itemClickListener;

    public RouteResultAdapter(Context context, List resultData) {
        mContext = context;
        if (resultData != null) {
            this.resultData = resultData;
        } else {
            this.resultData = new ArrayList(1);
        }
    }

    public void updateDatasource(List resultData) {
        if (resultData != null) {
            this.resultData = resultData;
            notifyDataSetChanged();
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        itemClickListener = onItemClickListener;
    }

    @Override
    public RouteResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RouteResultViewHolder holder = new RouteResultViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_bus_result, parent, false), itemClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(RouteResultViewHolder holder, int position) {
        Object resultItem = resultData.get(position);
        if (resultItem != null) {
            if (resultItem instanceof BusPath) {
                holder.tvTitle.setText(AMapUtil.getSpanBusPathTitle(mContext, AMapUtil.getBusPathTitle((BusPath) resultItem)));
                holder.tvDescription.setText(AMapUtil.getSpanBusPathDesc(mContext, AMapUtil.getBusPathDes((BusPath) resultItem)));
            }
        }
    }

    @Override
    public int getItemCount() {
        return resultData.size();
    }


    class RouteResultViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final String LOG_TAG = RouteResultViewHolder.class.getSimpleName();

        TextView tvTitle;
        TextView tvDescription;

        private OnItemClickListener mItemClickListener;

        public RouteResultViewHolder(View itemView, OnItemClickListener itemClickListener) {
            super(itemView);
            tvTitle = (TextView) itemView.findViewById(R.id.select_route_bus_title);
            tvDescription = (TextView) itemView.findViewById(R.id.select_route_bus_desc);
            mItemClickListener = itemClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, getAdapterPosition());
            }
        }
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

}
