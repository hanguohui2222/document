package com.gionee.secretary.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.services.busline.BusStationItem;
import com.amap.api.services.route.BusStep;
import com.amap.api.services.route.RailwayStationItem;
import com.gionee.secretary.R;
import com.gionee.secretary.bean.SchemeBusStep;
import com.gionee.secretary.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liyingheng on 2016/12/28.
 */

public class SelectRouteDetailAdapter extends RecyclerView.Adapter<SelectRouteDetailAdapter.DetailListViewHolder> {

    private static String LOG_TAG = SelectRouteDetailAdapter.class.getSimpleName();

    private Context mContext;
    private List<SchemeBusStep> mBusStepList = new ArrayList<>();

    public SelectRouteDetailAdapter(Context context, List<BusStep> list) {
        this.mContext = context;
        SchemeBusStep start = new SchemeBusStep(null);
        start.setStart(true);
        LogUtils.d(LOG_TAG, "list, size=" + list.size());
        mBusStepList.add(start);
        for (BusStep busStep : list) {
            if (busStep.getWalk() != null && busStep.getWalk().getDistance() > 0) {
                SchemeBusStep walk = new SchemeBusStep(busStep);
                walk.setWalk(true);
                mBusStepList.add(walk);
            }
            if (busStep.getBusLine() != null) {
                SchemeBusStep bus = new SchemeBusStep(busStep);
                bus.setBus(true);
                mBusStepList.add(bus);
            }
            if (busStep.getRailway() != null) {
                SchemeBusStep railway = new SchemeBusStep(busStep);
                railway.setRailway(true);
                mBusStepList.add(railway);
            }

            if (busStep.getTaxi() != null) {
                SchemeBusStep taxi = new SchemeBusStep(busStep);
                taxi.setTaxi(true);
                mBusStepList.add(taxi);
            }
        }
        SchemeBusStep end = new SchemeBusStep(null);
        end.setEnd(true);
        mBusStepList.add(end);
        LogUtils.d(LOG_TAG, "mBusStepList size=" + mBusStepList.size());
    }

    @Override
    public DetailListViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LogUtils.d(LOG_TAG, "onCreateViewHolder()");
        DetailListViewHolder holder = new DetailListViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_route_detail, viewGroup, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(DetailListViewHolder holder, int position) {
        LogUtils.d(LOG_TAG, "onBindViewHolder() " + "position=" + position);
        final SchemeBusStep item = mBusStepList.get(position);
        if (position == 0) {
            holder.busDirIcon.setImageResource(R.drawable.navi_dir_start);
            holder.busLineName.setText("出发");
            holder.busDirUp.setVisibility(View.INVISIBLE);
            holder.busDirDown.setVisibility(View.VISIBLE);
            holder.splitLine.setVisibility(View.GONE);
            holder.busStationNum.setVisibility(View.GONE);
            holder.busExpandImage.setVisibility(View.GONE);
        } else if (position == mBusStepList.size() - 1) {
            holder.busDirIcon.setImageResource(R.drawable.navi_dir_end);
            holder.busLineName.setText("到达终点");
            holder.busDirUp.setVisibility(View.VISIBLE);
            holder.busDirDown.setVisibility(View.INVISIBLE);
            holder.busStationNum.setVisibility(View.INVISIBLE);
            holder.busExpandImage.setVisibility(View.INVISIBLE);
        } else {
            if (item.isWalk() && item.getWalk() != null && item.getWalk().getDistance() > 0) {
                holder.busDirIcon.setImageResource(R.drawable.navi_dir13);
                holder.busDirUp.setVisibility(View.VISIBLE);
                holder.busDirDown.setVisibility(View.VISIBLE);
                holder.busLineName.setText("步行"
                        + (int) item.getWalk().getDistance() + "米");
                holder.busStationNum.setVisibility(View.GONE);
                holder.busExpandImage.setVisibility(View.GONE);
            } else if (item.isBus() && item.getBusLines().size() > 0) {
                holder.busDirIcon.setImageResource(R.drawable.navi_dir14);
                holder.busDirUp.setVisibility(View.VISIBLE);
                holder.busDirDown.setVisibility(View.VISIBLE);
                holder.busLineName.setText(item.getBusLines().get(0).getBusLineName());
                holder.busStationNum.setVisibility(View.VISIBLE);
                holder.busStationNum
                        .setText((item.getBusLines().get(0).getPassStationNum() + 1) + "站");
                holder.busExpandImage.setVisibility(View.VISIBLE);
                ArrowClick arrowClick = new ArrowClick(holder, item);
                holder.parent.setTag(position);
                holder.parent.setOnClickListener(arrowClick);
            } else if (item.isRailway() && item.getRailway() != null) {
                holder.busDirIcon.setImageResource(R.drawable.navi_dir16);
                holder.busDirUp.setVisibility(View.VISIBLE);
                holder.busDirDown.setVisibility(View.VISIBLE);
                holder.busLineName.setText(item.getRailway().getName());
                holder.busStationNum.setVisibility(View.VISIBLE);
                holder.busStationNum
                        .setText((item.getRailway().getViastops().size() + 1) + "站");
                holder.busExpandImage.setVisibility(View.VISIBLE);
                ArrowClick arrowClick = new ArrowClick(holder, item);
                holder.parent.setTag(position);
                holder.parent.setOnClickListener(arrowClick);
            } else if (item.isTaxi() && item.getTaxi() != null) {
                holder.busDirIcon.setImageResource(R.drawable.navi_dir14);
                holder.busDirUp.setVisibility(View.VISIBLE);
                holder.busDirDown.setVisibility(View.VISIBLE);
                holder.busLineName.setText("打车到终点");
                holder.busStationNum.setVisibility(View.GONE);
                holder.busExpandImage.setVisibility(View.GONE);
            }

        }
    }

    @Override
    public int getItemCount() {
        LogUtils.d(LOG_TAG, "getItemCount(), mBusStepList Size=" + mBusStepList.size());
        return mBusStepList.size();
    }

    class DetailListViewHolder extends RecyclerView.ViewHolder {
        //        TextView tvTitle;
//        TextView tvDesc;
        RelativeLayout parent;
        TextView busLineName;
        ImageView busDirIcon;
        TextView busStationNum;
        ImageView busExpandImage;
        ImageView busDirUp;
        ImageView busDirDown;
        ImageView splitLine;
        LinearLayout expandContent;
        boolean arrowExpend = false;


        DetailListViewHolder(View view) {
            super(view);
//            tvTitle = (TextView) view.findViewById(R.id.select_route_detail_tv_title);
//            tvDesc = (TextView) view.findViewById(R.id.select_route_detail_tv_desc);
            parent = (RelativeLayout) view.findViewById(R.id.bus_item);
            busLineName = (TextView) view.findViewById(R.id.bus_line_name);
            busDirIcon = (ImageView) view.findViewById(R.id.bus_dir_icon);
            busStationNum = (TextView) view.findViewById(R.id.bus_station_num);
            busExpandImage = (ImageView) view.findViewById(R.id.bus_expand_image);
            busDirUp = (ImageView) view.findViewById(R.id.bus_dir_icon_up);
            busDirDown = (ImageView) view.findViewById(R.id.bus_dir_icon_down);
            splitLine = (ImageView) view.findViewById(R.id.bus_seg_split_line);
            expandContent = (LinearLayout) view.findViewById(R.id.expand_content);

        }
    }

    private class ArrowClick implements View.OnClickListener {
        private DetailListViewHolder mHolder;
        private SchemeBusStep mItem;

        private ArrowClick(final DetailListViewHolder holder, final SchemeBusStep item) {
            mHolder = holder;
            mItem = item;
        }

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            int position = Integer.parseInt(String.valueOf(v.getTag()));
            mItem = mBusStepList.get(position);
            if (mItem.isBus()) {
                if (mHolder.arrowExpend == false) {
                    mHolder.arrowExpend = true;
                    mHolder.busExpandImage
                            .setImageResource(R.drawable.navi_up);
                    addBusStation(mItem.getBusLine().getDepartureBusStation());
                    for (BusStationItem station : mItem.getBusLine()
                            .getPassStations()) {
                        addBusStation(station);
                    }
                    addBusStation(mItem.getBusLine().getArrivalBusStation());

                } else {
                    mHolder.arrowExpend = false;
                    mHolder.busExpandImage
                            .setImageResource(R.drawable.navi_down);
                    mHolder.expandContent.removeAllViews();
                }
            } else if (mItem.isRailway()) {
                if (mHolder.arrowExpend == false) {
                    mHolder.arrowExpend = true;
                    mHolder.busExpandImage
                            .setImageResource(R.drawable.navi_up);
                    addRailwayStation(mItem.getRailway().getDeparturestop());
                    for (RailwayStationItem station : mItem.getRailway().getViastops()) {
                        addRailwayStation(station);
                    }
                    addRailwayStation(mItem.getRailway().getArrivalstop());

                } else {
                    mHolder.arrowExpend = false;
                    mHolder.busExpandImage
                            .setImageResource(R.drawable.navi_down);
                    mHolder.expandContent.removeAllViews();
                }
            }


        }

        private void addBusStation(BusStationItem station) {
            LinearLayout ll = (LinearLayout) View.inflate(mContext,
                    R.layout.item_bus_segment_ex, null);
            TextView tv = (TextView) ll
                    .findViewById(R.id.bus_line_station_name);
            tv.setText(station.getBusStationName());
            mHolder.expandContent.addView(ll);
        }

        private void addRailwayStation(RailwayStationItem station) {
            LinearLayout ll = (LinearLayout) View.inflate(mContext,
                    R.layout.item_bus_segment_ex, null);
            TextView tv = (TextView) ll
                    .findViewById(R.id.bus_line_station_name);
            tv.setText(station.getName() + " " + getRailwayTime(station.getTime()));
            mHolder.expandContent.addView(ll);
        }
    }

    private static String getRailwayTime(String time) {
        return time.substring(0, 2) + ":" + time.substring(2, time.length());
    }
}
