package com.gionee.secretary.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps.model.Text;
import com.gionee.secretary.R;
import com.gionee.secretary.bean.BankSchedule;
import com.gionee.secretary.bean.BaseSchedule;
import com.gionee.secretary.bean.FlightSchedule;
import com.gionee.secretary.bean.HotelSchedule;
import com.gionee.secretary.bean.MovieSchedule;
import com.gionee.secretary.bean.SelfCreateSchedule;
import com.gionee.secretary.bean.TrainSchedule;
import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.dao.ScheduleInfoDao;
import com.gionee.secretary.utils.CardDetailsUtils;
import com.gionee.secretary.utils.DateUtils;
import com.gionee.secretary.utils.LogUtils;
import com.gionee.secretary.utils.NavigateUtil;
import com.gionee.secretary.utils.TextUtilTools;
import com.gionee.secretary.utils.TextViewSnippet;
import com.gionee.secretary.utils.TravelModeUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by zhuboqin on 10/05/16.
 */
public class CalendarCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<BaseSchedule> mList;
    private static final int DATE_CARD = 0;
    private static final int TIME_CARD = 1;

    private ClickItemListener clickItemListener;
    private LongClickItemListener longClickItemListener;

    private boolean isOpen = false;
    private boolean mNeedHighLight = false;
    private String mTitleHighLightCompile = "";
    private String mTodatTips;

    private ScheduleInfoDao mDao;

    public CalendarCardAdapter(Context context, List<BaseSchedule> list) {
        this.mContext = context;
        this.mList = list;
    }

    public CalendarCardAdapter(Context context, List<BaseSchedule> list, String todatTips) {
        this.mContext = context;
        this.mList = list;
        mTodatTips = todatTips;
    }

    public interface ClickItemListener {
        void onClick(BaseSchedule event);
    }

    public interface LongClickItemListener {
        void onLongCLick(BaseSchedule event);
    }

    public void setClickItemListener(ClickItemListener clickItemListener) {
        this.clickItemListener = clickItemListener;
    }

    public void setLongClickItemListener(LongClickItemListener longClickItemListener) {
        this.longClickItemListener = longClickItemListener;
    }

    @Override
    public int getItemViewType(int position) {
        String thisDate = DateUtils.date2String(mList.get(position).date);
        if (position - 1 >= 0) {
            String lastDate = DateUtils.date2String(mList.get(position - 1).date);
            if (thisDate.equals(lastDate)) {
                return TIME_CARD;
            } else {
                return DATE_CARD;
            }
        } else {
            return DATE_CARD;
        }
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemCount() {
        Iterator<BaseSchedule> it = mList.iterator();
        while (it.hasNext()) {
            BaseSchedule event = it.next();
            if (event.type == Constants.EXPRESS_TYPE) {
                it.remove();
            }
        }
        return mList.size();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        BaseSchedule event = mList.get(position);
        if (holder instanceof DateViewHolder) {
            if (mTodatTips != null) {
//            modify by zjl at 2017-01-14
//            在首页部分添加了日期显示，解决没有日程就不显示日期的问题，所以卡片页就不显示了
//        		((DateViewHolder) holder).tv_date.setText(mTodatTips);
            } else {
                ((DateViewHolder) holder).tv_date.setVisibility(View.GONE);
            }
            ((DateViewHolder) holder).tv_week.setVisibility(View.GONE);
            if (position == 0) {
                ((DateViewHolder) holder).card_date_line.setVisibility(View.GONE);
                ((DateViewHolder) holder).card_date_empty.setVisibility(View.GONE);
            } else {
                ((DateViewHolder) holder).card_date_line.setVisibility(View.VISIBLE);
                ((DateViewHolder) holder).card_date_empty.setVisibility(View.VISIBLE);
            }
            ((DateViewHolder) holder).fl_card_time.removeAllViews();
            addCard((TimeViewHolder) holder, event);
        } else {
            ((TimeViewHolder) holder).fl_card_time.removeAllViews();
            addCard((TimeViewHolder) holder, event);
        }
    }

    private void addCard(TimeViewHolder holder, BaseSchedule event) {
        boolean isAllDay = event.getIsAllDay();
        if (!isAllDay) {
            holder.tv_time.setText(DateUtils.time2String(event.date));
        } else {
            holder.tv_time.setText("全天");
        }
        if (event.type == Constants.SELF_CREATE_TYPE) {
            setSelfCreateCard(holder, (SelfCreateSchedule) event);
        } else if (event.type == Constants.BANK_TYPE) {
            setBankCard(holder, (BankSchedule) event);
        } else if (event.type == Constants.TRAIN_TYPE) {
            setTrainCard(holder, (TrainSchedule) event);
        } else if (event.type == Constants.FLIGHT_TYPE) {
            setFlightCard(holder, (FlightSchedule) event);
        } else if (event.type == Constants.MOVIE_TYPE) {
            setMovieCard(holder, (MovieSchedule) event);
        } else if (event.type == Constants.HOTEL_TYPE) {
            setHotelCard(holder, (HotelSchedule) event);
        }
        holder.fl_card_time.setTag(event);
        holder.item_layout.setTag(event);

        holder.item_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseSchedule event = (BaseSchedule) v.getTag();
                if (clickItemListener != null)
                    clickItemListener.onClick(event);
            }
        });
        holder.item_layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                BaseSchedule event1 = (BaseSchedule) v.getTag();
                if (longClickItemListener != null)
                    longClickItemListener.onLongCLick(event1);
                return true;
            }
        });
    }

    private boolean isEmpty(String str) {
        if (!TextUtils.isEmpty(str) && !str.equals("null") && !str.equals("无数据")) {
            return false;
        } else {
            return true;
        }
    }

    private void setHotelCard(TimeViewHolder holder, final HotelSchedule event) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_card_content_hotel, holder.fl_card_time);
        HotelHolder hotelHolder = new HotelHolder(view);
        view.setTag(hotelHolder);
        holder.tv_time.setText(Constants.ALL_DAY);

        if (!isEmpty(event.getHotelName())) {
            SpannableStringBuilder title = getHighLightedText(event.getHotelName());
            holder.schdule_title.setText(title);
        }
        if (!isEmpty(event.getRoomStyle())) {
            hotelHolder.tv_bed_type.setText(event.getRoomStyle());
        }
        if (!isEmpty(DateUtils.date2String(event.getDate()))) {
            hotelHolder.tv_date.setText(DateUtils.getDate(event.getDate()));
        }
        if (!isEmpty(event.getServiceNum())) {
            CardDetailsUtils.setTextView(hotelHolder.tv_hotel_tel_num, hotelHolder.tv_hotel_tel_num.getText().toString(), event.getServiceNum());
        }

        if ((!isEmpty(event.getHotelName()) || !isEmpty(event.getHotelAddress())) && NavigateUtil.canNavigate(mContext)) {
            hotelHolder.tv_hotel_address.setText(event.getHotelAddress());
            hotelHolder.iv_hotel_location.setVisibility(View.VISIBLE);
//            hotelHolder.iv_hotel_location.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    NavigateUtil.navigateToDes(mContext, event.getHotelName());
//                }
//            });
        } else {
            hotelHolder.iv_hotel_location.setVisibility(View.GONE);
        }

//        if(!TravelModeUtil.isMapSupportMode(mContext)){
//            hotelHolder.iv_hotel_location.setVisibility(View.GONE);
//        }
    }

    private void setMovieCard(TimeViewHolder holder, MovieSchedule event) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_card_content_movie, holder.fl_card_time);
        MovieHolder movieHolder = new MovieHolder(view);
        view.setTag(movieHolder);

        holder.tv_time.setText(Constants.ALL_DAY);

        if (!isEmpty(event.getCinemaName())) {
            movieHolder.iv_cinema_location.setVisibility(View.VISIBLE);
//            SpannableStringBuilder cInemaName = getHighLightedText(event.getCinemaName());
            movieHolder.tv_cinema_name.setText(event.getCinemaName());
        } else {
            movieHolder.tv_cinema_name.setVisibility(View.GONE);
        }

        if (!isEmpty(event.getMovieName())) {
            SpannableStringBuilder title = getHighLightedText(event.getMovieName());
            holder.schdule_title.setText(title);
        }
        if (!isEmpty(DateUtils.date2String(event.getDate()))) {
            movieHolder.tv_date.setText(DateUtils.getDate(event.getDate()));
            movieHolder.tv_time.setText(DateUtils.time2String(event.getDate()));
            movieHolder.tv_week_date.setText(DateUtils.getWeekOfDate(event.getDate()));
        }
        if (!isEmpty(event.getSeatDesc())) {
            LogUtils.e("zjl", "getSeatDesc:" + event.getSeatDesc());
            movieHolder.tv_cell_num.setText(event.getSeatDesc());
        }

    }

    private void setFlightCard(TimeViewHolder holder, FlightSchedule event) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_card_content_flight, holder.fl_card_time);
        FlightHolder flightHolder = new FlightHolder(view);
        view.setTag(flightHolder);

        holder.schdule_title.setText(Constants.TRIP_INFO);

//        if (!isEmpty(event.getStartAddress())) {
//            SpannableStringBuilder title = getHighLightedText(event.getStartAddress());
//            flightHolder.tv_start_city.setText(title);
//        }
        if (!isEmpty(DateUtils.date2String(event.getDate()))) {
            flightHolder.tv_start_date.setText(DateUtils.date2String2(event.getDate()) + " " + DateUtils.getWeekOfDate(event.getDate()));
            flightHolder.tv_start_time.setText(DateUtils.time2String(event.getDate()));
        }
        if (!isEmpty(event.getStartAddress())) {
            flightHolder.tv_start_station.setText(event.getStartAddress());
        }
//        if (!isEmpty(event.getDestination())) {
//            SpannableStringBuilder title = getHighLightedText(event.getDestination());
//            flightHolder.tv_end_city.setText(title);
//        }
        String arrivalTime = event.getArrivalTime();
        if (!isEmpty(arrivalTime)) {
            LogUtils.e("zhubq", arrivalTime + "arrivalTime");
            if (arrivalTime.contains("日")) {
                String subTime = arrivalTime.substring(arrivalTime.indexOf("日") + 1, arrivalTime.length());
                flightHolder.tv_end_time.setText(subTime);

                String subDate = arrivalTime.substring(0, arrivalTime.indexOf("日") + 1);
                LogUtils.e("zhubq", subDate + "subDate");
                if (subDate.contains("月") && subDate.contains("日")) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM月dd日");
                    if (event.getDate() != null) {
                        LogUtils.e("zhubq", DateUtils.date2String2(event.getDate()) + "DateUtils.date2String2(event.getDate())");
                        String subYear = DateUtils.date2String2(event.getDate()).substring(0, 4);
                        LogUtils.e("zhubq", subYear + "subYear");
                        try {
                            Date date = simpleDateFormat.parse(subDate);
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(date);
                            calendar.set(Calendar.YEAR, Integer.valueOf(subYear));
                            flightHolder.tv_end_date.setText(DateUtils.formatDate2DateString(new Date(calendar.getTimeInMillis())));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }

            } else {
                flightHolder.tv_end_time.setText(arrivalTime);
            }
        }
        if (!isEmpty(event.getDestination())) {
            flightHolder.tv_end_station.setText(event.getDestination());
        }
        if (!isEmpty(event.getFlightNum())) {
            flightHolder.tv_flight_num.setText(event.getFlightNum());
        }

        LogUtils.e("zjl", "........." + flightHolder.tv_order_number.getText().toString());
        CardDetailsUtils.setTextView(flightHolder.tv_order_number, flightHolder.tv_order_number.getText().toString(), event.getTicketNum());

    }

    private void setTrainCard(TimeViewHolder holder, TrainSchedule event) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_card_content_train, holder.fl_card_time);
        TrainHolder trainHolder = new TrainHolder(view);
        view.setTag(trainHolder);
        LogUtils.e("TrainCard", "setTrainCard--------------------");
        holder.schdule_title.setText(Constants.TRIP_INFO);

        if (!isEmpty(event.getOrdernumber())) {
            CardDetailsUtils.setTextView(trainHolder.tv_order_num, trainHolder.tv_order_num.getText().toString(), event.getOrdernumber());
        }
        if (!isEmpty(event.getDeparture())) {
            trainHolder.tv_start_station.setText(event.getDeparture());
        }
        if (!isEmpty(event.getDestination())) {
            trainHolder.tv_end_station.setText(event.getDestination());
        }
        if (!isEmpty(event.getTrainnumber())) {
            trainHolder.tv_train_num.setText(event.getTrainnumber());
        }

        if (!isEmpty(DateUtils.date2String(event.getDate()))) {
            trainHolder.tv_start_date.setText(DateUtils.date2String2(event.getDate()) + " " + DateUtils.getWeekOfDate(event.getDate()));
            trainHolder.tv_start_time.setText(DateUtils.time2String(event.getDate()));
        }

        LogUtils.e("zjl", "getStartTime:" + event.getStarttime());
        LogUtils.e("zjl", "getArrivaltime" + event.getArrivaltime());

        String arrivalTime = event.getArrivaltime();
        if (!isEmpty(arrivalTime)) {
            LogUtils.e("zjl", arrivalTime + "arrivalTime");
            if (arrivalTime.contains("日")) {
                String subTime = arrivalTime.substring(arrivalTime.indexOf("日") + 1, arrivalTime.length());
                trainHolder.tv_arrival_time.setText(subTime);

                String subDate = arrivalTime.substring(0, arrivalTime.indexOf("日") + 1);
                LogUtils.e("zjl", subDate + "subDate");
                if (subDate.contains("月") && subDate.contains("日")) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM月dd日");
                    if (event.getDate() != null) {
                        LogUtils.e("zjl", DateUtils.date2String2(event.getDate()) + "DateUtils.date2String2(event.getDate())");
                        String subYear = DateUtils.date2String2(event.getDate()).substring(0, 4);
                        LogUtils.e("zjl", subYear + "subYear");
                        try {
                            Date date = simpleDateFormat.parse(subDate);
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(date);
                            calendar.set(Calendar.YEAR, Integer.valueOf(subYear));
                            trainHolder.tv_arrival_date.setText(DateUtils.formatDate2DateString(new Date(calendar.getTimeInMillis())));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }

            } else {
                trainHolder.tv_arrival_time.setText(arrivalTime);
            }
        }
    }

    private void setBankCard(TimeViewHolder holder, BankSchedule event) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_card_content_bank, holder.fl_card_time);
        BankHolder bankHolder = new BankHolder(view);
        view.setTag(bankHolder);

        String money = null;
        if (!isEmpty(event.getRepaymentAmount())) {
            if (event.getRepaymentAmount().contains("美元")) {
                int index = event.getRepaymentAmount().indexOf("美");
                String rmb = event.getRepaymentAmount().substring(0, index);
                String doll = event.getRepaymentAmount().substring(index, event.getRepaymentAmount().length());
                money = rmb + "\n" + doll;

            } else
                money = event.getRepaymentAmount();
        }

        LogUtils.e("zjl", "money" + money);
        LogUtils.e("zjl", "tv_last_date:" + event.getRepaymentMonth());
        String divider = ":";
        CardDetailsUtils.setTextView(holder.schdule_title, event.title);
        CardDetailsUtils.setTextView(bankHolder.tv_bank_name, event.getBankName() + divider);
        CardDetailsUtils.setTextView(bankHolder.tv_card_number, event.getCardNum());
        CardDetailsUtils.setTextViewWithTimeTable(bankHolder.tv_bill_date, bankHolder.tv_bill_date.getText().toString(), event.getBillMonth());
        CardDetailsUtils.setTextViewWithTimeTable(bankHolder.tv_last_date, bankHolder.tv_last_date.getText().toString(), event.getRepaymentMonth());
        CardDetailsUtils.setTextView(bankHolder.tv_money, money);
    }

    private void setSelfCreateCard(TimeViewHolder holder, SelfCreateSchedule event) {
        LogUtils.e("zjl", "setSelfCreateCard");
        final SelfCreateSchedule selfCreateEvent = event;
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_card_content_self_create, holder.fl_card_time);
        SelfCreateHolder selfCreateHolder = new SelfCreateHolder(view);
        view.setTag(selfCreateHolder);

        if (!isEmpty(selfCreateEvent.getTitle()) && !selfCreateEvent.getTitle().equals(Constants.NEW_SCHDULE)) {
            holder.schdule_title.setText(selfCreateEvent.getTitle());
        } else if (isEmpty(selfCreateEvent.getTitle()) || selfCreateEvent.getTitle().equals(Constants.NEW_SCHDULE)) {
            holder.schdule_title.setText(selfCreateEvent.getDescription());
        }

        //显示address
        String address = selfCreateEvent.getAddress();
        if (!isEmpty(address)) {
            if (NavigateUtil.canNavigate(mContext)) {
                holder.location_img.setVisibility(View.VISIBLE);
            }
        } else {
            holder.location_img.setVisibility(View.GONE);
        }
        //自定义日程只显示一行
        holder.divider_img.setVisibility(View.GONE);
        holder.fl_card_time.setVisibility(View.GONE);

//        View.OnClickListener locationClickListener = new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                NavigateUtil.navigateToDes(mContext, selfCreateEvent.getAddress());
//            }
//        };
//        holder.location_img.setOnClickListener(locationClickListener);
//
//        if(!TravelModeUtil.isMapSupportMode(mContext,selfCreateEvent.getTripMode())){
//            holder.location_img.setVisibility(View.GONE);
//        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = null;
        switch (viewType) {
            case DATE_CARD:
                holder = new DateViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_card_date, parent, false));
                break;
            case TIME_CARD:
                holder = new TimeViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_card_time, parent, false));
                break;
        }
        return holder;
    }

    public void setNeedHighLight(boolean needHighLight) {
        this.mNeedHighLight = needHighLight;
    }

    public void setTitlehighLightCompile(String compile) {
        this.mTitleHighLightCompile = compile;
    }

    public SpannableStringBuilder getHighLightedText(String title) {
        LogUtils.i("liyy", "mneedHight:adapter....." + mNeedHighLight);
        if (mNeedHighLight) {
            return TextUtilTools.hightLightText(title, mTitleHighLightCompile, Color.RED);
        } else {
            return new SpannableStringBuilder(title);
        }
    }


    class DateViewHolder extends TimeViewHolder {
        private TextView tv_date;
        private View card_date_line;
        private TextView tv_week;
        private View card_date_empty;

        public DateViewHolder(View itemView) {
            super(itemView);
            tv_date = (TextView) itemView.findViewById(R.id.tv_date);
            card_date_line = itemView.findViewById(R.id.card_date_line);
            tv_week = (TextView) itemView.findViewById(R.id.tv_week);
            card_date_empty = itemView.findViewById(R.id.card_date_empty);
        }
    }

    class TimeViewHolder extends RecyclerView.ViewHolder {
        protected LinearLayout item_layout;
        protected RelativeLayout time_layout;
        protected TextView schdule_title;
        protected TextView tv_time;
        protected FrameLayout fl_card_time;
        protected ImageView location_img;
        protected ImageView divider_img;

        public TimeViewHolder(View itemView) {
            super(itemView);
            item_layout = (LinearLayout) itemView.findViewById(R.id.item_card);
            schdule_title = (TextView) itemView.findViewById(R.id.schdule_title);
            tv_time = (TextView) itemView.findViewById(R.id.tv_time);
            fl_card_time = (FrameLayout) itemView.findViewById(R.id.fl_card_time);
            location_img = (ImageView) itemView.findViewById(R.id.iv_manual_create_location_in_title);
            time_layout = (RelativeLayout) itemView.findViewById(R.id.time_layout);
            divider_img = (ImageView) itemView.findViewById(R.id.divider_img);
        }
    }

    class FlightHolder extends RecyclerView.ViewHolder {
        private TextView tv_start_time;
        private TextView tv_start_date;
        private TextViewSnippet tv_start_station;
        private TextView tv_end_time;
        private TextView tv_end_date;
        private TextViewSnippet tv_end_station;
        private TextView tv_flight_num;
        private TextView tv_order_number;

        public FlightHolder(View itemView) {
            super(itemView);
            tv_start_time = (TextView) itemView.findViewById(R.id.tv_start_time);
            tv_start_date = (TextView) itemView.findViewById(R.id.tv_start_date);
            tv_start_station = (TextViewSnippet) itemView.findViewById(R.id.tv_start_station);
            tv_flight_num = (TextView) itemView.findViewById(R.id.tv_flight_num);
            tv_end_time = (TextView) itemView.findViewById(R.id.tv_end_time);
            tv_end_date = (TextView) itemView.findViewById(R.id.tv_end_date);
            tv_end_station = (TextViewSnippet) itemView.findViewById(R.id.tv_end_station);
            tv_order_number = (TextView) itemView.findViewById(R.id.ticket_number);
        }
    }

    class TrainHolder extends RecyclerView.ViewHolder {
        private TextView tv_order_num;
        private TextView tv_train_num;
        private TextViewSnippet tv_start_station;
        private TextViewSnippet tv_end_station;
        private TextView tv_start_date;
        private TextView tv_arrival_date;
        private TextView tv_start_time;
        private TextView tv_arrival_time;
        private TextView tv_seat;

        public TrainHolder(View itemView) {
            super(itemView);
            tv_order_num = (TextView) itemView.findViewById(R.id.tv_order_num);
            tv_train_num = (TextView) itemView.findViewById(R.id.tv_train_num);
            tv_start_station = (TextViewSnippet) itemView.findViewById(R.id.tv_start_station);
            tv_end_station = (TextViewSnippet) itemView.findViewById(R.id.tv_end_station);
            tv_start_time = (TextView) itemView.findViewById(R.id.tv_start_time);
            tv_arrival_time = (TextView) itemView.findViewById(R.id.tv_end_time);
            tv_start_date = (TextView) itemView.findViewById(R.id.tv_start_date);
            tv_arrival_date = (TextView) itemView.findViewById(R.id.tv_end_date);

            //tv_seat = (TextView) itemView.findViewById(R.id.tv_seat);
        }
    }

    class BankHolder extends RecyclerView.ViewHolder {
        private TextView tv_money;
        private TextView tv_bank_name;
        private TextView tv_card_number;
        private TextView tv_last_date;
        private TextView tv_bill_date;

        public BankHolder(View itemView) {
            super(itemView);
            tv_money = (TextView) itemView.findViewById(R.id.tv_money);
            tv_bank_name = (TextView) itemView.findViewById(R.id.tv_bank_name);
            tv_card_number = (TextView) itemView.findViewById(R.id.tv_card_number);
            tv_last_date = (TextView) itemView.findViewById(R.id.tv_last_date);
            tv_bill_date = (TextView) itemView.findViewById(R.id.tv_bill_date);
        }
    }

    class MovieHolder extends RecyclerView.ViewHolder {
        private TextView tv_cinema_name;
        private TextView tv_date;
        private TextView tv_week_date;
        private TextView tv_time;
        private TextView tv_cell_num;
        private ImageView iv_cinema_location;
        private TextView tv_seat_num;

        public MovieHolder(View itemView) {
            super(itemView);
            tv_cinema_name = (TextView) itemView.findViewById(R.id.tv_cinema_name);
            tv_date = (TextView) itemView.findViewById(R.id.tv_date);
            tv_week_date = (TextView) itemView.findViewById(R.id.tv_week_date);
            tv_time = (TextView) itemView.findViewById(R.id.tv_time);
            tv_cell_num = (TextView) itemView.findViewById(R.id.tv_cell_num);
//            tv_seat_num = (TextView) itemView.findViewById(R.id.tv_seat_num);
            iv_cinema_location = (ImageView) itemView.findViewById(R.id.iv_cinema_location);
        }
    }

    class HotelHolder extends RecyclerView.ViewHolder {
        private TextView tv_hotel_address;
        private TextView tv_date;
        private TextView tv_bed_type;
        private TextView tv_hotel_tel_num;
        private ImageView iv_hotel_location;

        public HotelHolder(View itemView) {
            super(itemView);
            tv_hotel_address = (TextView) itemView.findViewById(R.id.tv_hotel_address);
            tv_date = (TextView) itemView.findViewById(R.id.tv_date);
            tv_bed_type = (TextView) itemView.findViewById(R.id.tv_bed_type);
            tv_hotel_tel_num = (TextView) itemView.findViewById(R.id.tv_hotel_tel_num);
            iv_hotel_location = (ImageView) itemView.findViewById(R.id.iv_hotel_location);
        }
    }

    class DeliveryHolder extends RecyclerView.ViewHolder {
        private TextView tv_delivery_company_name;
        private TextView tv_order_num;
        private TextView tv_delivery_datetime;
        private TextView tv_delivery_info;
        private TextView tv_hotel_tel_num;
        private FrameLayout card_view;

        private LinearLayout ll_more_order;
        //        private CardView card_view_more;
        private RelativeLayout card_view_more;
        private TextView tv_more_order;

        private ImageView iv_end;
        private ImageView iv_on_the_way;
        private ImageView iv_start;

        public DeliveryHolder(View itemView) {
            super(itemView);

//            tv_delivery_company_name = (TextView)itemView.findViewById(R.id.tv_delivery_company_name);
//            tv_order_num = (TextView)itemView.findViewById(R.id.tv_order_num);
            tv_delivery_datetime = (TextView) itemView.findViewById(R.id.tv_delivery_datetime);
            tv_delivery_info = (TextView) itemView.findViewById(R.id.tv_delivery_info);
            ll_more_order = (LinearLayout) itemView.findViewById(R.id.ll_more_order);
//            card_view_more = (CardView)itemView.findViewById(R.id.card_view_more);
            card_view_more = (RelativeLayout) itemView.findViewById(R.id.card_view_more);
            card_view = (FrameLayout) itemView.findViewById(R.id.card_view);
            tv_more_order = (TextView) itemView.findViewById(R.id.tv_more_order);

            iv_start = (ImageView) itemView.findViewById(R.id.iv_start);
            iv_on_the_way = (ImageView) itemView.findViewById(R.id.iv_on_the_way);
            iv_end = (ImageView) itemView.findViewById(R.id.iv_end);
        }
    }

    class SelfCreateHolder extends RecyclerView.ViewHolder {
        private TextViewSnippet tv_content;
        private ImageView iv_manual_create_location;

        public SelfCreateHolder(View itemView) {
            super(itemView);
            tv_content = (TextViewSnippet) itemView.findViewById(R.id.tv_content);
            iv_manual_create_location = (ImageView) itemView.findViewById(R.id.iv_manual_create_location);
        }
    }
}
