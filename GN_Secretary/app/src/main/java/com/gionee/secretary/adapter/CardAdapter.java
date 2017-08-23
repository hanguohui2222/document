package com.gionee.secretary.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gionee.secretary.R;
import com.gionee.secretary.bean.BankSchedule;
import com.gionee.secretary.bean.BaseSchedule;
import com.gionee.secretary.bean.ExpressSchedule;
import com.gionee.secretary.bean.FlightSchedule;
import com.gionee.secretary.bean.HotelSchedule;
import com.gionee.secretary.bean.MovieSchedule;
import com.gionee.secretary.bean.SelfCreateSchedule;
import com.gionee.secretary.bean.TrainSchedule;
import com.gionee.secretary.bean.WeatherSchedule;
import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.dao.ScheduleInfoDao;
import com.gionee.secretary.presenter.SearchPresenter;
import com.gionee.secretary.utils.CardDetailsUtils;
import com.gionee.secretary.utils.DateUtils;
import com.gionee.secretary.utils.KdniaoTrackQueryAPI;
import com.gionee.secretary.utils.LogUtils;
import com.gionee.secretary.utils.NavigateUtil;
import com.gionee.secretary.utils.TextUtilTools;
import com.gionee.secretary.utils.TextViewSnippet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by zhuboqin on 10/05/16.
 */
public class CardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<BaseSchedule> mList;
    private static final int DATE_CARD = 0;
    private static final int TIME_CARD = 1;

    private ClickItemListener clickItemListener;
    private LongClickItemListener longClickItemListener;
    private List<ExpressSchedule> mExpressList;

    //modify by zhengjl at 2017-2-23 for GNSPR #69251 begin
    private static boolean isOpen = false;
    //modify by zhengjl at 2017-2-23 for GNSPR #69251 begin
    private boolean mNeedHighLight = false;
    private String mTitleHighLightCompile = "";
    private RecyclerView mRecyclerView;
    private SearchPresenter mSearchPresent;
    private ScheduleInfoDao mDao;

    public CardAdapter(Context context, List<BaseSchedule> list) {
        this.mContext = context;
        this.mList = list;
    }

    public CardAdapter(Context context, List<BaseSchedule> list, List<ExpressSchedule> expressSchedules) {
        this.mContext = context;
        this.mList = list;
        this.mExpressList = expressSchedules;
    }

    public CardAdapter(Context context, RecyclerView recyclerView, List<BaseSchedule> list, List<ExpressSchedule> expressSchedules) {
        this.mContext = context;
        this.mList = list;
        this.mExpressList = expressSchedules;
        this.mRecyclerView = recyclerView;
    }

    public CardAdapter(Context context, RecyclerView recyclerView, List<BaseSchedule> list, List<ExpressSchedule> expressSchedules, SearchPresenter searchPresent) {
        this.mContext = context;
        this.mList = list;
        this.mExpressList = expressSchedules;
        this.mSearchPresent = searchPresent;
        this.mRecyclerView = recyclerView;
        this.mDao = ScheduleInfoDao.getInstance(context);

    }

    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            ExpressSchedule event = (ExpressSchedule) msg.obj;
            int id = msg.arg1;
            LogUtils.e("zhu--", "handleMessage==============");
            View view = mRecyclerView.findViewWithTag(id);
            if (view == null) {
                return;
            }
            if (event == null) {
                return;
            }
            int state = event.getState();
            LogUtils.e("zhu--", state + "==============");
            ExpressSchedule.Trace trace = null;
            String time = "";
            String info = "";
            String rawtime = "";
            //added by luorw for s10c 终端项目Bug #88111  2017-03-23 begin
            if (event.getTraces() != null) {
                if (event.getTraces().size() > 1) {
                    trace = event.getTraces().get(event.getTraces().size() - 1);
                } else if (event.getTraces().size() == 1) {
                    trace = event.getTraces().get(0);
                }
            }
            if (trace != null) {
                rawtime = trace.getAcceptTime();
                time = DateUtils.formatDate2String2(DateUtils.formatDate(rawtime));
                LogUtils.e("zhu--", time + "==============");
                info = trace.getAcceptStation();
                LogUtils.e("zhu--", info + "==============");
            }
            String reason = event.getReason();
            LogUtils.e("zhu--", reason + "==============");

            DeliveryHolder holder = new DeliveryHolder(view);
            holder.tv_delivery_company_name.setVisibility(View.VISIBLE);
            holder.tv_delivery_company_name.setText(event.getExpressCompany());

            if (!isEmpty(reason)) {
                holder.tv_delivery_info.setVisibility(View.GONE);
                holder.tv_delivery_datetime.setVisibility(View.GONE);
                holder.tv_delivery_reason.setVisibility(View.VISIBLE);
                holder.tv_delivery_reason.setText(reason);
            } else {
                if (!isEmpty(time) || !isEmpty(info)) {
                    holder.tv_delivery_datetime.setText(time);
                    holder.tv_delivery_datetime.setVisibility(View.VISIBLE);
                    holder.tv_delivery_info.setVisibility(View.VISIBLE);
                    holder.tv_delivery_reason.setVisibility(View.GONE);

                    holder.tv_delivery_info.setText(info);
                    holder.tv_delivery_info.setVisibility(View.VISIBLE);
                    holder.tv_delivery_datetime.setVisibility(View.VISIBLE);
                    holder.tv_delivery_reason.setVisibility(View.GONE);
//                    saveExpressState2DB(rawtime, info, state, id);
                    new SaveExpressThread(rawtime, info, state, id).start();
                }
                setUIExpressState(holder, state);
            }
        }

    };

    class SaveExpressThread extends Thread {
        String rawtime;
        String info;
        int state;
        int id;

        private SaveExpressThread(String rawtime, String info, int state, int id) {
            this.rawtime = rawtime;
            this.state = state;
            this.info = info;
            this.id = id;
        }

        @Override
        public void run() {
            super.run();
            saveExpressState2DB(rawtime, info, state, id);
        }
    }


    private void saveExpressState2DB(String time, String info, int state, int id) {

        mSearchPresent.updateExpressSchedule(time, info, state, id);
    }

    private void saveExpressInfo2DB(String code, String name, int id) {
        mDao.updateExpressInfo(code, name, id);
    }

    private ExecutorService executorService = Executors.newFixedThreadPool(5);

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
        return TIME_CARD;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    //TODO 暂时禁用复用 复用的原因不知到
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        BaseSchedule event = mList.get(position);
//		((TimeViewHolder) holder).fl_card_time.removeAllViews();
        addCard((TimeViewHolder) holder, event);
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
        } else if (event.type == Constants.WEATHER_TYPE) {
            setWeatherCard(holder, (WeatherSchedule) event);
        } else if (event.type == Constants.TRAIN_TYPE) {
            setTrainCard(holder, (TrainSchedule) event);
        } else if (event.type == Constants.FLIGHT_TYPE) {
            setFlightCard(holder, (FlightSchedule) event);
        } else if (event.type == Constants.MOVIE_TYPE) {
            setMovieCard(holder, (MovieSchedule) event);
        } else if (event.type == Constants.HOTEL_TYPE) {
            setHotelCard(holder, (HotelSchedule) event);
        } else if (event.type == Constants.EXPRESS_TYPE && mExpressList != null) {
            setExpressCard(holder, (ExpressSchedule) event);
        }
        holder.fl_card_time.setTag(event);
        holder.item_layout.setTag(event);

        if (event.type != Constants.EXPRESS_TYPE) {
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

    }

    private void setUIExpressState(DeliveryHolder holder, int event) {
        if (event == 3 || (event == 4 && holder.tv_delivery_info.getText().toString().contains("签收"))) { //签收
            holder.iv_end.setImageResource(R.drawable.express_now);
            holder.iv_on_the_way.setImageResource(R.drawable.express_past);
            holder.iv_start.setImageResource(R.drawable.express_past);
        } else if (event == 2 || event == 4) {
            holder.iv_on_the_way.setImageResource(R.drawable.express_now);
            holder.iv_end.setImageResource(R.drawable.express_past);
            holder.iv_start.setImageResource(R.drawable.express_past);
        } else {
            holder.iv_start.setImageResource(R.drawable.express_past);
            holder.iv_on_the_way.setImageResource(R.drawable.express_past);
            holder.iv_end.setImageResource(R.drawable.express_past);
        }
    }

    private boolean isEmpty(String str) {
        if (!TextUtils.isEmpty(str) && !str.equals("null") && !str.equals("无数据")) {
            return false;
        } else {
            return true;
        }
    }


    class RequestExpressTrace implements Runnable {
        private String expressCode;
        private String expressNum;
        private int id;

        private ExpressSchedule schedule;
        private DeliveryHolder deliveryHolder;

        public RequestExpressTrace(String expressCode, String expressNum, int id) {
            this.expressCode = expressCode;
            this.expressNum = expressNum;
            this.id = id;
        }

        public RequestExpressTrace(ExpressSchedule schedule, DeliveryHolder deliveryHolder) {
            this.schedule = schedule;
            this.deliveryHolder = deliveryHolder;
        }

        @Override
        public void run() {
            KdniaoTrackQueryAPI api = new KdniaoTrackQueryAPI();
            String shipperCode = "";
            String shipperName = "";
            try {
                //1.
                if (isEmpty(schedule.getExpressCompany()) || isEmpty(schedule.getExpressCode())) {
                    String codeResult = api.getOrderShipperCode(schedule.getExpressNum());
                    JSONObject jsonObject = new JSONObject(codeResult);
                    JSONArray jsonArray = jsonObject.getJSONArray("Shippers");
                    if (jsonArray != null && jsonArray.length() > 0) {
                        JSONObject jShipper = (JSONObject) jsonArray.get(0);
                        shipperCode = jShipper.getString("ShipperCode");
                        shipperName = jShipper.getString("ShipperName");
                    }

                    schedule.setExpressCompany(shipperName);
                    schedule.setExpressCode(shipperCode);
                    schedule.setTitle(shipperName);

                    saveExpressInfo2DB(shipperCode, shipperName, schedule.getId());
                }

                //2.
                String result = api.getOrderTracesByJson(schedule.getExpressCode(), schedule.getExpressNum());
                LogUtils.e("zhu--", result);
                Gson gson = new Gson();
                ExpressSchedule expressBean = gson.fromJson(result, ExpressSchedule.class);
                expressBean.setId(schedule.getId());
                expressBean.setExpressCode(schedule.getExpressCode());
                expressBean.setTitle(schedule.getTitle());
                expressBean.setExpressCompany(schedule.getExpressCompany());

                Message msg = Message.obtain();
                msg.obj = expressBean;
                msg.arg1 = schedule.getId();
                handler.sendMessage(msg);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    //modify by zhengjl at 2017-1-19 调试快递卡片界面
    private void setExpressCard(TimeViewHolder holder, ExpressSchedule event) {
        View view = View.inflate(mContext, R.layout.item_card_content_delivery, null);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, 1);
        holder.fl_card_time.addView(view, params);
        final DeliveryHolder deliveryHolder = new DeliveryHolder(view);

        holder.schdule_title.setText("快递信息");
        holder.type_image.setImageResource(R.drawable.express_img);

        deliveryHolder.card_view.setTag(event);
        view.setTag(event.getId());
        RequestExpressTrace runnable = new RequestExpressTrace(event, deliveryHolder);
        executorService.submit(runnable);
        if (null != event.getTrace_date() && !isEmpty(DateUtils.formatDate2String2(event.getTrace_date()))) {
            deliveryHolder.tv_delivery_datetime.setVisibility(View.VISIBLE);
            deliveryHolder.tv_delivery_datetime.setText(DateUtils.formatDate2String2(event.getTrace_date()));
        } else {
            deliveryHolder.tv_delivery_datetime.setVisibility(View.GONE);
        }
        if (!isEmpty(event.getExpressProgress())) {
            deliveryHolder.tv_delivery_info.setVisibility(View.VISIBLE);
            deliveryHolder.tv_delivery_info.setText(event.getExpressProgress());
        } else {
            deliveryHolder.tv_delivery_info.setVisibility(View.GONE);
        }

        if (!isEmpty(event.getExpressCompany())) {
            deliveryHolder.tv_delivery_company_name.setVisibility(View.VISIBLE);
            deliveryHolder.tv_delivery_company_name.setText(event.getExpressCompany());
        } else {
            deliveryHolder.tv_delivery_company_name.setVisibility(View.GONE);
        }
        if (!isEmpty(event.getExpressNum())) {
            deliveryHolder.tv_order_num.setText(event.getExpressNum());
        }

        deliveryHolder.view_gap.setVisibility(View.VISIBLE);
        int state = event.getState();
        setUIExpressState(deliveryHolder, state);

        deliveryHolder.card_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseSchedule event = (BaseSchedule) v.getTag();
                if (clickItemListener != null)
                    clickItemListener.onClick(event);
            }
        });

        deliveryHolder.card_view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                BaseSchedule event1 = (BaseSchedule) v.getTag();
                if (longClickItemListener != null)
                    longClickItemListener.onLongCLick(event1);
                return true;
            }
        });

        if (mExpressList != null && mExpressList.size() > 1) {
            //如果刷新列表时，导致item不在视图窗口内，那么下次再显示，就会重新加载视图
            //只有第一次加载.setIsOpen是false，其他时候应该根据当前状态来设置isOpen状态
            //modify by zhengjl at 2017-2-22 for GNSPR #68832 begin
            setIsOpen(isOpen, deliveryHolder);
//            deliveryHolder.card_view_more.setVisibility(View.VISIBLE);
            //modify by zhengjl at 2017-2-22 for GNSPR #68832 end
            deliveryHolder.card_view_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isOpen) {
                        setIsOpen(true, deliveryHolder);
                    } else {
                        setIsOpen(false, deliveryHolder);
                    }
                }
            });
        } else {
            deliveryHolder.card_view_more.setVisibility(View.GONE);
            deliveryHolder.ll_more_order.setVisibility(View.GONE);
        }
    }

    public void setIsOpen(boolean isOpen, final DeliveryHolder deliveryHolder) {
        this.isOpen = isOpen;
        if (isOpen) {
            deliveryHolder.ll_more_order.setVisibility(View.VISIBLE);
            deliveryHolder.card_view_more.setVisibility(View.GONE);
            deliveryHolder.ll_more_order.removeAllViews();
            for (int i = 1; i < mExpressList.size(); i++) {
                ExpressSchedule expressSchedule = mExpressList.get(i);
                View view = LayoutInflater.from(mContext).inflate(R.layout.item_card_content_delivery, deliveryHolder.ll_more_order, false);
                final DeliveryHolder holder1 = new DeliveryHolder(view);
                view.setTag(expressSchedule.getId());
                RequestExpressTrace runnable = new RequestExpressTrace(expressSchedule, holder1);
                executorService.submit(runnable);
                holder1.card_view.setTag(expressSchedule);
                holder1.view_gap.setVisibility(View.VISIBLE);
                if (null != expressSchedule.getTrace_date() && !isEmpty(DateUtils.formatDate2String2(expressSchedule.getTrace_date()))) {
                    holder1.tv_delivery_datetime.setVisibility(View.VISIBLE);
                    holder1.tv_delivery_datetime.setText(DateUtils.formatDate2String2(expressSchedule.getTrace_date()));
                } else {
                    holder1.tv_delivery_datetime.setVisibility(View.GONE);
                }
                if (!isEmpty(expressSchedule.getExpressProgress())) {
                    holder1.tv_delivery_info.setVisibility(View.VISIBLE);
                    holder1.tv_delivery_info.setText(expressSchedule.getExpressProgress());
                } else {
                    holder1.tv_delivery_info.setVisibility(View.GONE);
                }

                //modify by zhengjl at 2017-1-19 调快递卡片
                if (!isEmpty(expressSchedule.getExpressCompany())) {
                    holder1.tv_delivery_company_name.setVisibility(View.VISIBLE);
                    holder1.tv_delivery_company_name.setText(expressSchedule.getExpressCompany());
                } else {
                    holder1.tv_delivery_company_name.setVisibility(View.GONE);
                }
                if (!isEmpty(expressSchedule.getExpressNum())) {
                    holder1.tv_order_num.setVisibility(View.VISIBLE);
                    holder1.tv_order_num.setText(expressSchedule.getExpressNum());
                } else {
                    holder1.tv_order_num.setVisibility(View.GONE);
                }
                int state = expressSchedule.getState();
                setUIExpressState(holder1, state);
                deliveryHolder.ll_more_order.addView(view);
                holder1.card_view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ExpressSchedule expressSchedule1 = (ExpressSchedule) v.getTag();
                        if (clickItemListener != null)
                            clickItemListener.onClick(expressSchedule1);
                    }
                });
                holder1.card_view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        ExpressSchedule expressSchedule1 = (ExpressSchedule) v.getTag();
                        if (longClickItemListener != null)
                            longClickItemListener.onLongCLick(expressSchedule1);
                        return true;
                    }
                });
                if (i == mExpressList.size() - 1) {

                    holder1.card_view_more.setVisibility(View.VISIBLE);
                    holder1.tv_more_order.setText("收起");

                    holder1.card_view_more.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            setIsOpen(false, deliveryHolder);

                        }
                    });


                }
            }

        } else {
            deliveryHolder.ll_more_order.setVisibility(View.GONE);
            deliveryHolder.card_view_more.setVisibility(View.VISIBLE);
            deliveryHolder.tv_more_order.setText("更多");
//            this.isOpen = false;
            if (mRecyclerView.findViewHolderForAdapterPosition(0) != null) {
                View view = mRecyclerView.findViewHolderForAdapterPosition(0).itemView;
                view.setTranslationY(0);
            }
        }
    }

    private void setHotelCard(TimeViewHolder holder, final HotelSchedule event) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_card_content_hotel, holder.fl_card_time);
        HotelHolder hotelHolder = new HotelHolder(view);
        view.setTag(hotelHolder);

        holder.tv_time.setText(Constants.ALL_DAY);
        if (!isEmpty(event.getHotelName())) {
//            SpannableStringBuilder title = getHighLightedText(event.getHotelName());
            holder.schdule_title.setText(event.getHotelName());
//            TextUtilTools.setHighLightText(event.getHotelName(), mTitleHighLightCompile, holder.schdule_title, mContext);
        } else {
            holder.schdule_title.setText("酒店");
        }
        holder.type_image.setImageResource(R.drawable.hotel_img);
        if (!isEmpty(event.getRoomStyle())) {
            hotelHolder.tv_bed_type.setVisibility(View.VISIBLE);
            hotelHolder.tv_bed_type.setText(event.getRoomStyle());
        } else {
            hotelHolder.tv_bed_type.setVisibility(View.GONE);
        }
        if (!isEmpty(DateUtils.date2String(event.getDate()))) {
            hotelHolder.tv_date.setVisibility(View.VISIBLE);
            hotelHolder.tv_date.setText(DateUtils.getDate(event.getDate()));
            //hotelHolder.tv_week.setText(DateUtils.getWeekOfDate(event.getDate()));
        } else {
            hotelHolder.tv_date.setVisibility(View.GONE);
        }
        if (!isEmpty(event.getServiceNum())) {
            hotelHolder.tv_hotel_tel_num.setVisibility(View.VISIBLE);
            CardDetailsUtils.setTextView(hotelHolder.tv_hotel_tel_num, hotelHolder.tv_hotel_tel_num.getText().toString(), event.getServiceNum());
        } else {
            hotelHolder.tv_hotel_tel_num.setVisibility(View.GONE);
        }
//        if(( !isEmpty(event.getHotelName()) || !isEmpty(event.getHotelAddress()) )){
        if ((!isEmpty(event.getHotelAddress()))) {
            hotelHolder.tv_hotel_address.setText(event.getHotelAddress());
            hotelHolder.tv_hotel_address.setVisibility(View.VISIBLE);
            hotelHolder.iv_hotel_location.setVisibility(View.VISIBLE);
//            hotelHolder.iv_hotel_location.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    NavigateUtil.navigateToDes(mContext, event.getHotelName());
//                }
//            });
        } else {
            hotelHolder.iv_hotel_location.setVisibility(View.GONE);
            hotelHolder.tv_hotel_address.setVisibility(View.GONE);
        }
    }

    private void setMovieCard(TimeViewHolder holder, final MovieSchedule event) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_card_content_movie, holder.fl_card_time);
        MovieHolder movieHolder = new MovieHolder(view);
        view.setTag(movieHolder);

        /*modify by zhengjl at 2017-2-5 for GNSPR #66206*/
//        holder.tv_time.setText(Constants.ALL_DAY);
        holder.tv_time.setText(DateUtils.time2String(event.getDate()));
        if (!isEmpty(event.getCinemaName())) {
            movieHolder.iv_cinema_location.setVisibility(View.VISIBLE);
//            SpannableStringBuilder cInemaName = getHighLightedText(event.getCinemaName());
            movieHolder.tv_cinema_name.setText(event.getCinemaName());
        } else {
            movieHolder.tv_cinema_name.setVisibility(View.GONE);
        }

        if (!isEmpty(event.getMovieName())) {
            holder.schdule_title.setText(event.getMovieName());

//            TextUtilTools.setHighLightText(event.getMovieName(),mTitleHighLightCompile,holder.schdule_title,mContext);
        }
        holder.type_image.setImageResource(R.drawable.movie_img);
        if (event.getPlayTime() != null && !"null".equals(event.getPlayTime())) {
            CardDetailsUtils.setTextView(movieHolder.tv_date, event.getPlayTime());
        } else {
            movieHolder.tv_date.setText(DateUtils.getDate(event.getDate()));
            movieHolder.tv_time.setText(DateUtils.time2String(event.getDate()));
            movieHolder.tv_week_date.setText(DateUtils.getWeekOfDate(event.getDate()));
        }
        if (!isEmpty(event.getSeatDesc())) {
            movieHolder.tv_cell_num.setVisibility(View.VISIBLE);
            movieHolder.tv_cell_num.setText(event.getSeatDesc());
        } else {
            movieHolder.tv_cell_num.setVisibility(View.GONE);
        }

    }

    private void setFlightCard(TimeViewHolder holder, FlightSchedule event) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_card_content_flight, holder.fl_card_time);
        FlightHolder flightHolder = new FlightHolder(view);
        view.setTag(flightHolder);

        holder.schdule_title.setText(Constants.TRIP_INFO);
        holder.type_image.setImageResource(R.drawable.flight_img);
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
//            TextUtilTools.setHighLightText(event.getStartAddress(), mTitleHighLightCompile, flightHolder.tv_start_station, mContext);
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
//            TextUtilTools.setHighLightText(event.getDestination(), mTitleHighLightCompile, flightHolder.tv_end_station, mContext);
        }
        if (!isEmpty(event.getFlightNum())) {
            flightHolder.tv_flight_num.setText(event.getFlightNum());
        }

        CardDetailsUtils.setTextView(flightHolder.tv_order_number, flightHolder.tv_order_number.getText().toString(), event.getTicketNum());

    }

    /*modify by zhengjl at 2017-2-9
    * 修改火车类卡片UI
    * */
    private void setTrainCard(TimeViewHolder holder, TrainSchedule event) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_card_content_train, holder.fl_card_time);
        TrainHolder trainHolder = new TrainHolder(view);
        view.setTag(trainHolder);
        LogUtils.e("TrainCard", "setTrainCard--------------------");
        holder.schdule_title.setText(Constants.TRIP_INFO);
        holder.type_image.setImageResource(R.drawable.train_img);

        if (!isEmpty(event.getOrdernumber())) {
            trainHolder.tv_order_num.setVisibility(View.VISIBLE);
            CardDetailsUtils.setTextView(trainHolder.tv_order_num, trainHolder.tv_order_num.getText().toString(), event.getOrdernumber());
        } else {
            trainHolder.tv_order_num.setVisibility(View.GONE);
        }
        if (!isEmpty(event.getDeparture())) {
//            SpannableStringBuilder title = getHighLightedText(event.getDeparture());
            trainHolder.tv_start_station.setText(event.getDeparture());
//            TextUtilTools.setHighLightText(event.getDeparture(), mTitleHighLightCompile, trainHolder.tv_start_station, mContext);
        }
        if (!isEmpty(event.getDestination())) {
//            SpannableStringBuilder title = getHighLightedText(event.getDestination());
            trainHolder.tv_end_station.setText(event.getDestination());
//            TextUtilTools.setHighLightText(event.getDestination(), mTitleHighLightCompile, trainHolder.tv_end_station, mContext);
        }
        if (!isEmpty(event.getTrainnumber())) {
            trainHolder.tv_train_num.setVisibility(View.VISIBLE);
            trainHolder.tv_train_num.setText(event.getTrainnumber());
        } else {
            trainHolder.tv_train_num.setVisibility(View.GONE);
        }
        if (!isEmpty(DateUtils.date2String(event.getDate()))) {
            trainHolder.tv_start_date.setText(DateUtils.date2String2(event.getDate()) + " " + DateUtils.getWeekOfDate(event.getDate()) + " " + DateUtils.time2String(event.getDate()));
//            trainHolder.tv_start_time.setText(DateUtils.time2String(event.getDate()));
        }

        /*
        String arrivalTime = event.getArrivaltime();
        if (!isEmpty(arrivalTime)) {
            if(arrivalTime.contains("日")){
                String subTime = arrivalTime.substring(arrivalTime.indexOf("日")+1,arrivalTime.length());
                trainHolder.tv_arrival_time.setText(subTime);

                String subDate = arrivalTime.substring(0,arrivalTime.indexOf("日")+1);
                if(subDate.contains("月") && subDate.contains("日")){
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM月dd日");
                    if(event.getDate() != null){
                        String subYear = DateUtils.date2String2(event.getDate()).substring(0,4);
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

            }else{
                trainHolder.tv_arrival_time.setText(arrivalTime);
            }
        }
        */
    }

    private void setBankCard(TimeViewHolder holder, BankSchedule event) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_card_content_bank, holder.fl_card_time);
        BankHolder bankHolder = new BankHolder(view);
        view.setTag(bankHolder);

        String money = null;
        /*
        modify by zhengjl at 2017-2-4 for GNSPR #66025 begin
         */
        if (!isEmpty(event.getRepaymentAmount())) {
            //LogUtils.e("zjl", "event.getRepaymentAmount()" + event.getRepaymentAmount());
            String regEx = "[0-9]\\d*\\.?\\d*";
//            String regEx= "\\d";
            Pattern p = Pattern.compile(regEx);
            Matcher m = p.matcher(event.getRepaymentAmount());
            StringBuilder strBuilder = new StringBuilder();
            StringBuilder renMingBing = new StringBuilder();
            StringBuilder meiYuan = new StringBuilder();
            int i = 0;
            while (m.find()) {
                String temp = m.group();
                strBuilder.append(temp);
                if (i == 0) {
                    renMingBing.append(temp);
                } else if (i == 1) {
                    meiYuan.append(temp);
                }
                i++;
            }
            if (strBuilder.length() != 0)
                money = strBuilder.toString();
            if ((event.getRepaymentAmount().contains("美元") || event.getRepaymentAmount().contains("$")) && (event.getRepaymentAmount().contains("人民币") || event.getRepaymentAmount().contains("￥"))) {
                money = "$" + meiYuan;
                CardDetailsUtils.setTextViewWithTimeTable(bankHolder.tv_dollar_bill, bankHolder.tv_dollar_bill.getText().toString(), money);
                money = "￥" + renMingBing;
            } else if (event.getRepaymentAmount().contains("美元") || event.getRepaymentAmount().contains("$")) {
                money = "$" + money;
                CardDetailsUtils.setTextViewWithTimeTable(bankHolder.tv_dollar_bill, bankHolder.tv_dollar_bill.getText().toString(), money);
            } else {
                //美元账单
                CardDetailsUtils.setTextViewWithTimeTable(bankHolder.tv_dollar_bill, bankHolder.tv_dollar_bill.getText().toString(), "$0.00");
                money = "￥" + money;
            }
        }
//        if (!isEmpty(event.getRepaymentAmount())) {
//            if(event.getRepaymentAmount().contains("美元")){
//                int index = event.getRepaymentAmount().indexOf("美");
//                String rmb = event.getRepaymentAmount().substring(0, index);
//                String doll = event.getRepaymentAmount().substring(index,event.getRepaymentAmount().length());
//                money = rmb + "\n" + doll;
//            }else
//                money = event.getRepaymentAmount();
//        }

        if (!isEmpty(event.title)) {
            holder.schdule_title.setText(event.title);
//            TextUtilTools.setHighLightText(event.title, mTitleHighLightCompile,holder.schdule_title, mContext);
        }
        holder.type_image.setImageResource(R.drawable.bank_img);
        String divider = ":";
        if (!TextUtils.isEmpty(event.getBankName())) {
            bankHolder.tv_bank_name.setVisibility(View.VISIBLE);
            CardDetailsUtils.setTextView(bankHolder.tv_bank_name, event.getBankName());
        } else {
            bankHolder.tv_bank_name.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(event.getCardNum())) {
            bankHolder.tv_card_number.setVisibility(View.VISIBLE);
            CardDetailsUtils.setTextView(bankHolder.tv_card_number, divider + event.getCardNum());
        } else {
            bankHolder.tv_card_number.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(event.getBillMonth())) {
            bankHolder.tv_bill_date.setVisibility(View.VISIBLE);
            CardDetailsUtils.setTextViewWithTimeTable(bankHolder.tv_bill_date, bankHolder.tv_bill_date.getText().toString(), event.getBillMonth());
        } else {
            bankHolder.tv_bill_date.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(event.getRepaymentMonth())) {
            bankHolder.tv_last_date.setVisibility(View.VISIBLE);
            CardDetailsUtils.setTextViewWithTimeTable(bankHolder.tv_last_date, bankHolder.tv_last_date.getText().toString(), event.getRepaymentMonth());
        } else {
            bankHolder.tv_last_date.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(money)) {
            bankHolder.tv_money_tips.setVisibility(View.VISIBLE);
            bankHolder.tv_money.setVisibility(View.VISIBLE);
            CardDetailsUtils.setTextView(bankHolder.tv_money, money);
        } else {
            bankHolder.tv_money_tips.setVisibility(View.GONE);
            bankHolder.tv_money.setVisibility(View.GONE);
        }
        /*
        modify by zhengjl at 2017-2-4 for GNSPR #66025 end
         */
    }

    private void setSelfCreateCard(TimeViewHolder holder, SelfCreateSchedule event) {
        final SelfCreateSchedule selfCreateEvent = event;
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_card_content_self_create, holder.fl_card_time);
        SelfCreateHolder selfCreateHolder = new SelfCreateHolder(view);
        view.setTag(selfCreateHolder);

//        if (!isEmpty(selfCreateEvent.getTitle()) && !selfCreateEvent.getTitle().equals(Constants.NEW_SCHDULE)) {
//            TextUtilTools.setHighLightText(selfCreateEvent.getTitle(), mTitleHighLightCompile, holder.schdule_title, mContext);
//        } else if (isEmpty(selfCreateEvent.getTitle()) || selfCreateEvent.getTitle().equals(Constants.NEW_SCHDULE)) {
//            holder.schdule_title.setText(selfCreateEvent.getDescription());
//            TextUtilTools.setHighLightText(selfCreateEvent.getDescription(), mTitleHighLightCompile, holder.schdule_title, mContext);
//        }

        if (!isEmpty(selfCreateEvent.getTitle())) {
            //modify by zhengjl at 2017-1-22
            holder.schdule_title.setText(selfCreateEvent.getTitle());
//            TextUtilTools.setHighLightText(selfCreateEvent.getTitle(), mTitleHighLightCompile, holder.schdule_title, mContext);
        }
        holder.type_image.setImageResource(R.drawable.self_schedule_img);
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

//        if(!TravelModeUtil.isMapSupportMode(mContext,selfCreateEvent.getTripMode())){
//            holder.location_img.setVisibility(View.GONE);
//        }
    }

    private void setWeatherCard(TimeViewHolder holder, WeatherSchedule event) {
        WeatherSchedule weatherSchedule = event;
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_card_content_weather, holder.fl_card_time);
        WeatherHolder weatherHolder = new WeatherHolder(view);
        view.setTag(weatherHolder);
        if (!isEmpty(weatherSchedule.getAddress())) {
            weatherHolder.tv_address.setText(weatherSchedule.getAddress());
        }
        String weather = weatherSchedule.getWeather();
        if (weather != null && !isEmpty(weather.trim())) {
            weatherHolder.tv_weather.setText(weather);
            if (weather.contains("晴")) {
                setWeatherIcon(weatherHolder.iv_weather_icon, R.drawable.sunny);
            }
            if (weather.contains("阴")) {
                setWeatherIcon(weatherHolder.iv_weather_icon, R.drawable.cloudy);
            }
            if (weather.contains("阴") && weather.contains("多云")) {
                setWeatherIcon(weatherHolder.iv_weather_icon, R.drawable.sunny_cloudy);
            }
            if (weather.contains("雨")) {
                setWeatherIcon(weatherHolder.iv_weather_icon, R.drawable.rain);
            }
            if (weather.contains("小雨")) {
                setWeatherIcon(weatherHolder.iv_weather_icon, R.drawable.rain_small);
            }
            if (weather.contains("大雨")) {
                setWeatherIcon(weatherHolder.iv_weather_icon, R.drawable.rain_big);
            }
            if (weather.contains("中雨")) {
                setWeatherIcon(weatherHolder.iv_weather_icon, R.drawable.rain_middle);
            }
            if (weather.contains("冻雨")) {
                setWeatherIcon(weatherHolder.iv_weather_icon, R.drawable.freezing_rain);
            }
            if (weather.contains("大暴雨")) {
                setWeatherIcon(weatherHolder.iv_weather_icon, R.drawable.heavy_rain);
            }
            if (weather.contains("特大暴雨")) {
                setWeatherIcon(weatherHolder.iv_weather_icon, R.drawable.heavy_rain_so_much);
            }
            if (weather.contains("阵雨")) {
                setWeatherIcon(weatherHolder.iv_weather_icon, R.drawable.shower);
            }
            if (weather.contains("雷阵雨")) {
                setWeatherIcon(weatherHolder.iv_weather_icon, R.drawable.thunder_shower);
            }
            if (weather.contains("小雪")) {
                setWeatherIcon(weatherHolder.iv_weather_icon, R.drawable.snow);
            }
            if (weather.contains("中雪")) {
                setWeatherIcon(weatherHolder.iv_weather_icon, R.drawable.snow_middle);
            }
            if (weather.contains("大雪")) {
                setWeatherIcon(weatherHolder.iv_weather_icon, R.drawable.snow_big);
            }
            if (weather.contains("暴雪")) {
                setWeatherIcon(weatherHolder.iv_weather_icon, R.drawable.blizzard);
            }
            if (weather.contains("雨夹雪")) {
                setWeatherIcon(weatherHolder.iv_weather_icon, R.drawable.snow_rain);
            }
            if (weather.contains("沙尘暴")) {
                setWeatherIcon(weatherHolder.iv_weather_icon, R.drawable.sand_storm);
            }
            if (weather.contains("霜冻")) {
                setWeatherIcon(weatherHolder.iv_weather_icon, R.drawable.frost);
            }
            if (weather.contains("雾")) {
                setWeatherIcon(weatherHolder.iv_weather_icon, R.drawable.fog);
            }
            if (weather.contains("冰雹")) {
                setWeatherIcon(weatherHolder.iv_weather_icon, R.drawable.hail);
            }
        }
        if (!isEmpty(weatherSchedule.getTemp())) {
            weatherHolder.tv_temp.setText(weatherSchedule.getTemp());
        } else {
            weatherHolder.tv_temp.setVisibility(View.INVISIBLE);
        }
        if (!isEmpty(weatherSchedule.getDressing()) && !isEmpty(weatherSchedule.getUmbrella())) {
            weatherHolder.tv_dressing_umbrella.setText(weatherSchedule.getDressing() + weatherSchedule.getUmbrella());
        }

    }

    private void setWeatherIcon(ImageView iv, int resId) {
        iv.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(), resId));
    }

    /**
     * 刷新天气
     *
     * @param eventList
     */
    public void notifyWeather(List<BaseSchedule> eventList) {
        if (null != eventList) {
            this.mList = eventList;
            notifyDataSetChanged();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = null;
//        switch (viewType) {
//            case DATE_CARD:
//                holder = new TimeViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_card_date, parent, false));
//                break;
//            case TIME_CARD:
//                holder = new TimeViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_card_time, parent, false));
//                break;
//        }
        holder = new TimeViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_card_time, parent, false));
        return holder;
    }

    public void setNeedHighLight(boolean needHighLight) {
        this.mNeedHighLight = needHighLight;
    }

    public void setTitlehighLightCompile(String compile) {
        this.mTitleHighLightCompile = compile;
    }

    public SpannableStringBuilder getHighLightedText(String title) {
        LogUtils.i("liyy", "mneedHight:adapter....." + mNeedHighLight + " ,title:" + title + "  ,hightText:" + mTitleHighLightCompile);
        if (mNeedHighLight) {
            return TextUtilTools.hightLightText(title, mTitleHighLightCompile, R.color.search_highlight_color);
        } else {
            return new SpannableStringBuilder(title);
        }
    }

    class DateViewHolder extends TimeViewHolder {
        private TextView tv_date;
        private View card_date_line;
        private TextView tv_week;
        private View card_date_empty;
        private TextView tv_card_title;

        public DateViewHolder(View itemView) {
            super(itemView);
            tv_date = (TextView) itemView.findViewById(R.id.tv_date);
            card_date_line = itemView.findViewById(R.id.card_date_line);
            tv_week = (TextView) itemView.findViewById(R.id.tv_week);
            card_date_empty = itemView.findViewById(R.id.card_date_empty);
            tv_card_title = (TextView) itemView.findViewById(R.id.tv_card_title);
        }
    }

    class TimeViewHolder extends RecyclerView.ViewHolder {
        protected LinearLayout item_layout;
        protected RelativeLayout time_layout;
        protected ImageView type_image;
        protected TextView schdule_title;
        protected TextView tv_time;
        protected FrameLayout fl_card_time;
        protected ImageView location_img;
        protected View divider_img;

        public TimeViewHolder(View itemView) {
            super(itemView);
            item_layout = (LinearLayout) itemView.findViewById(R.id.item_card);
            schdule_title = (TextView) itemView.findViewById(R.id.schdule_title);
            tv_time = (TextView) itemView.findViewById(R.id.tv_time);
            fl_card_time = (FrameLayout) itemView.findViewById(R.id.fl_card_time);
            location_img = (ImageView) itemView.findViewById(R.id.iv_manual_create_location_in_title);
            time_layout = (RelativeLayout) itemView.findViewById(R.id.time_layout);
            divider_img = (View) itemView.findViewById(R.id.divider_img);
            type_image = (ImageView) itemView.findViewById(R.id.type_img);
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
        //        private TextView tv_arrival_date;
//        private TextView tv_start_time;
//        private TextView tv_arrival_time;
        private TextView tv_seat;

        public TrainHolder(View itemView) {
            super(itemView);
            tv_order_num = (TextView) itemView.findViewById(R.id.tv_order_num);
            tv_train_num = (TextView) itemView.findViewById(R.id.tv_train_num);
            tv_start_station = (TextViewSnippet) itemView.findViewById(R.id.tv_start_station);
            tv_end_station = (TextViewSnippet) itemView.findViewById(R.id.tv_end_station);
//            tv_start_time = (TextView) itemView.findViewById(R.id.tv_start_time);
//            tv_arrival_time = (TextView) itemView.findViewById(R.id.tv_end_time);
            tv_start_date = (TextView) itemView.findViewById(R.id.tv_start_date);
//            tv_arrival_date = (TextView) itemView.findViewById(R.id.tv_end_date);

            //tv_seat = (TextView) itemView.findViewById(R.id.tv_seat);
        }
    }

    class BankHolder extends RecyclerView.ViewHolder {
        private TextView tv_money;
        private TextView tv_bank_name;
        private TextView tv_card_number;
        private TextView tv_last_date;
        private TextView tv_bill_date;
        //add by zhengjl at 2017-2-4
        private TextView tv_dollar_bill;
        private TextView tv_money_tips;

        public BankHolder(View itemView) {
            super(itemView);
            tv_money = (TextView) itemView.findViewById(R.id.tv_money);
            tv_bank_name = (TextView) itemView.findViewById(R.id.tv_bank_name);
            tv_card_number = (TextView) itemView.findViewById(R.id.tv_card_number);
            tv_last_date = (TextView) itemView.findViewById(R.id.tv_last_date);
            tv_bill_date = (TextView) itemView.findViewById(R.id.tv_bill_date);
            tv_dollar_bill = (TextView) itemView.findViewById(R.id.tv_dollar_bill);
            tv_money_tips = (TextView) itemView.findViewById(R.id.repayment_money_tips);
        }
    }

    class MovieHolder extends RecyclerView.ViewHolder {
        private TextView tv_cinema_name;
        private TextView tv_date;
        private TextView tv_week_date;
        private TextView tv_time;
        private TextView tv_cell_num;
        private ImageView iv_cinema_location;
//        private TextView tv_seat_num;

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

    //modify by zhengjl at 2017-01-19 调快递卡片 not end
    class DeliveryHolder extends RecyclerView.ViewHolder {
        private TextViewSnippet tv_delivery_company_name;
        private TextView tv_order_num;
        private TextView tv_delivery_datetime;
        private TextView tv_delivery_info;
        private TextView tv_delivery_reason;
        private TextView tv_hotel_tel_num;
        private FrameLayout card_view;
        private View view_gap;
        private TextView tv_start;
        private TextView tv_on_the_way;
        private TextView tv_end;

        private LinearLayout ll_more_order;
        //        private CardView card_view_more;
        private RelativeLayout card_view_more;
        private TextView tv_more_order;

        private ImageView iv_end;
        private ImageView iv_on_the_way;
        private ImageView iv_start;

        public DeliveryHolder(View itemView) {
            super(itemView);

            tv_delivery_company_name = (TextViewSnippet) itemView.findViewById(R.id.tv_delivery_company_name);
            tv_order_num = (TextView) itemView.findViewById(R.id.tv_order_num);
            tv_delivery_datetime = (TextView) itemView.findViewById(R.id.tv_delivery_datetime);
            tv_delivery_info = (TextView) itemView.findViewById(R.id.tv_delivery_info);
            tv_delivery_reason = (TextView) itemView.findViewById(R.id.tv_delivery_reason);
            ll_more_order = (LinearLayout) itemView.findViewById(R.id.ll_more_order);
            //            card_view_more = (CardView)itemView.findViewById(R.id.card_view_more);
            card_view_more = (RelativeLayout) itemView.findViewById(R.id.card_view_more);
            card_view = (FrameLayout) itemView.findViewById(R.id.card_view);
            tv_more_order = (TextView) itemView.findViewById(R.id.tv_more_order);
            view_gap = itemView.findViewById(R.id.view_gap);

            iv_start = (ImageView) itemView.findViewById(R.id.iv_start);

            iv_on_the_way = (ImageView) itemView.findViewById(R.id.iv_on_the_way);
            iv_end = (ImageView) itemView.findViewById(R.id.iv_end);

            tv_start = (TextView) itemView.findViewById(R.id.tv_start);
            tv_on_the_way = (TextView) itemView.findViewById(R.id.tv_on_the_way);
            tv_end = (TextView) itemView.findViewById(R.id.tv_end);
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

    class WeatherHolder extends RecyclerView.ViewHolder {
        private TextView tv_address;
        private TextView tv_weather;
        private TextView tv_temp;
        private TextView tv_dressing_umbrella;
        private ImageView iv_weather_icon;

        public WeatherHolder(View itemView) {
            super(itemView);
            tv_address = (TextView) itemView.findViewById(R.id.tv_address);
            tv_weather = (TextView) itemView.findViewById(R.id.tv_weather);
            tv_temp = (TextView) itemView.findViewById(R.id.tv_temp);
            tv_dressing_umbrella = (TextView) itemView.findViewById(R.id.tv_dressing_umbrella);
            iv_weather_icon = (ImageView) itemView.findViewById(R.id.iv_weather_icon);
        }
    }

}
