package com.gionee.secretary.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import com.gionee.secretary.bean.BaseNoteSchedule;
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
import com.gionee.secretary.utils.WidgetUtils;
import com.gionee.secretary.ui.activity.NoteDetailActivity;

import java.io.File;
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
public class SearchCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<BaseSchedule> mList;
    private static final int DATE_CARD = 0;
    private static final int TIME_CARD = 1;
    private static final int NOTE_CARD = 2;

    public static final String JPG = "jpg";
    public static final String PNG = "png";
    public static final String AMR = "amr";

    private ClickItemListener clickItemListener;
    private LongClickItemListener longClickItemListener;
    private List<ExpressSchedule> mExpressList;

    private boolean isOpen = false;
    private boolean mNeedHighLight = false;
    private String mTitleHighLightCompile = "";
    private RecyclerView mRecyclerView;
    private SearchPresenter mSearchPresent;
    private ScheduleInfoDao mDao;

    //add by zhengjl at 2017-2-22 for GNSPR #68578 begin
    private Date tagDate;

    public SearchCardAdapter(Context context, List<BaseSchedule> list) {
        this.mContext = context;
        this.mList = list;
    }

    public SearchCardAdapter(Context context, List<BaseSchedule> list, List<ExpressSchedule> expressSchedules) {
        this.mContext = context;
        this.mList = list;
        this.mExpressList = expressSchedules;
    }

    public SearchCardAdapter(Context context, RecyclerView recyclerView, List<BaseSchedule> list, List<ExpressSchedule> expressSchedules) {
        this.mContext = context;
        this.mList = list;
        this.mExpressList = expressSchedules;
        this.mRecyclerView = recyclerView;
    }

    public SearchCardAdapter(Context context, RecyclerView recyclerView, List<BaseSchedule> list, List<ExpressSchedule> expressSchedules, SearchPresenter searchPresent) {
        this.mContext = context;
        this.mList = list;
        this.mExpressList = expressSchedules;
        this.mSearchPresent = searchPresent;
        this.mRecyclerView = recyclerView;
        this.mDao = ScheduleInfoDao.getInstance(context);
        initNoteCard();
        initSchduleCard();

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
            if(event.getTraces() == null){
                return;
            }
            int state = event.getState();
            LogUtils.e("zhu--", state + "==============");
            ExpressSchedule.Trace trace = null;
            String time = "";
            String info = "";
            String rawtime = "";
            if (event.getTraces().size() > 1) {
                trace = event.getTraces().get(event.getTraces().size() - 1);
            } else if (event.getTraces().size() == 1) {
                trace = event.getTraces().get(0);
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
            //holder.tv_delivery_company_name.setText(event.getExpressCompany());
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
                    saveExpressState2DB(rawtime, info, state, id);
                }
                setUIExpressState(holder, state);
            }
            WidgetUtils.updateWidget(mContext);
        }

    };

    private void saveExpressState2DB(String time, String info, int state, int id) {
        LogUtils.e("zhu--", "saveExpressState2DB=============");
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
        String thisDate = DateUtils.date2String(mList.get(position).date);

        if (thisDate.equals("null")) {
            return NOTE_CARD;
        }

        if (position - 1 >= 0) {

            //modify by zhengjl at 2017-2-21 修改搜索界面显示效果  begin
            /*
            String lastDate = DateUtils.date2String(mList.get(position - 1).date);
            LogUtils.i("liyy","position:"+position+"   thisDate:"+thisDate+"    ,lastDate:"+lastDate);

            if (thisDate.equals(lastDate)) {
                LogUtils.i("liyy","position:"+position+"   timeCard");
                return TIME_CARD;
            } else {
                LogUtils.i("liyy","position:"+position+"   dataCard");
                return DATE_CARD;
            }
            */
            return TIME_CARD;
            //modify by zhengjl at 2017-2-21 修改搜索界面显示效果  end
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
        return mList.size();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        BaseSchedule event = mList.get(position);

        if (holder instanceof DateViewHolder) {
            schduleCardItemCount++;
            /* Gionee zhengyt 2016-12-20 add for search not begin */
            LogUtils.i("liyy", "event.date:" + event.date + " position:"
                    + position + "event.type:" + event.type + "event.title:" + event.getTitle());
            if (position == 0) {
                ((DateViewHolder) holder).card_date_line
                        .setVisibility(View.GONE);
                ((DateViewHolder) holder).card_date_empty
                        .setVisibility(View.GONE);
                ((DateViewHolder) holder).tv_card_title.setVisibility(View.VISIBLE);
            } else {
                ((DateViewHolder) holder).card_date_line
                        .setVisibility(View.VISIBLE);
                ((DateViewHolder) holder).card_date_empty
                        .setVisibility(View.VISIBLE);
                ((DateViewHolder) holder).tv_card_title.setVisibility(View.GONE);
            }
            ((DateViewHolder) holder).fl_card_time.removeAllViews();

            //add by zhengjl at 2017-2-22 for GNSPR #68578 not end
            //add by zhengjl at 2017-2-22 for GNSPR #69248
            tagDate = event.getDate();
            //added by luorw for GNSPR #75375 2017-03-22 begin
            String dateString = new SimpleDateFormat("yyyy年M月d日").format(event.getDate());
            ((TimeViewHolder) holder).tv_date_tag.setVisibility(View.VISIBLE);
            if (new Date().getYear() != tagDate.getYear()) {
                ((TimeViewHolder) holder).tv_date_tag.setText(dateString);
            } else {
                ((TimeViewHolder) holder).tv_date_tag.setText(dateString.substring(5));
            }
            //added by luorw for GNSPR #75375 2017-03-22 end
            //add by zhengjl at 2017-2-22 for GNSPR #68578 not end

            addCard((TimeViewHolder) holder, event);
        } else if (holder instanceof NoteViewHolder) {
            noteCardItemCount++;
            LogUtils.i("liyy", "3333--event.date:" + event.date + " position:" + position + "event.type:" + event.type + "event.title:" + event.getTitle());
            ((NoteViewHolder) holder).note_frame.removeAllViews();
            addNoteCard((NoteViewHolder) holder, event);
            if (noteCardItemCount == 1) {
                ((NoteViewHolder) holder).note_type.setVisibility(View.VISIBLE);

                //add by zhengjl at 2017-1-20 for GNSPR #65660 not end
                if (schduleCardItemCount == 0) {
                    ((NoteViewHolder) holder).note_divider_img.setVisibility(View.GONE);
                } else {
                    ((NoteViewHolder) holder).note_divider_img.setVisibility(View.VISIBLE);
                }
                //add by zhengjl at 2017-1-20 for GNSPR #65660 end
            } else {
                ((NoteViewHolder) holder).note_type.setVisibility(View.GONE);
            }
        } else {
            LogUtils.i("liyy", "2222--event.date:" + event.date + " position:" + position + "event.type:" + event.type + "event.title:" + event.getTitle());
            ((TimeViewHolder) holder).fl_card_time.removeAllViews();

            //add by zhengjl at 2017-2-22 for GNSPR #68578 not end
            setTagDate(((TimeViewHolder) holder).tv_date_tag, event.getDate());
            //add by zhengjl at 2017-2-22 for GNSPR #68578 not end

            addCard((TimeViewHolder) holder, event);
        }
    }

    private void setTagDate(TextView tv_date, Date eventDate) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        LogUtils.e("zjl", "tagDate:" + format.format(tagDate));
        LogUtils.e("zjl", "eventDate:" + format.format(eventDate));
        String dateString;
        if (eventDate.getYear() != tagDate.getYear()) {
            tv_date.setVisibility(View.VISIBLE);
            dateString = new SimpleDateFormat("yyyy年M月d日").format(eventDate);
            tv_date.setText(dateString);
            tagDate = eventDate;
        } else if ((eventDate.getMonth() != tagDate.getMonth()) || (eventDate.getDay() != tagDate.getDay())) {
            tv_date.setVisibility(View.VISIBLE);
            dateString = new SimpleDateFormat("M月d日").format(eventDate);
            tv_date.setText(dateString);
            tagDate = eventDate;
        } else {
            tv_date.setVisibility(View.GONE);
        }
    }

    /*Gionee zhengyt 2016-12-20 add for search note begin*/
    private void addNoteCard(NoteViewHolder holder, BaseSchedule event) {
        setNoteCard(holder, (BaseNoteSchedule) event);

        holder.note_frame.setTag(event);

        holder.note_frame.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                /*modify by zhengjl for  GNSPR #65753 not end*/
                if (!TextUtils.isEmpty(mTitleHighLightCompile)) {
//                    LogUtils.e("zjl","save to DB ===" + mTitleHighLightCompile);
                    mSearchPresent.saveSearchTextToDB(mTitleHighLightCompile);
                }
                /*modify by zhengjl for  GNSPR #65753 end*/
                BaseNoteSchedule event = (BaseNoteSchedule) v.getTag();
                Intent noteIntent = new Intent();
                noteIntent.setClass(mContext, NoteDetailActivity.class);
                noteIntent.putExtra("noteid", event.id);
                noteIntent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                mContext.startActivity(noteIntent);
            }
        });

    }
    /*Gionee zhengyt 2016-12-20 add for search note End*/


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
        if (event == 3) { //签收
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


    private void setExpressCard(TimeViewHolder holder, ExpressSchedule event) {
        View view = View.inflate(mContext, R.layout.item_card_content_delivery, null);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, 1);

        /*add by zhengjl at 2017-2-7 for search
        搜索页面的卡片显示异常
        */
        holder.fl_card_time.setVisibility(View.VISIBLE);
        holder.divider_img.setVisibility(View.VISIBLE);
        holder.schdule_title.setText(Constants.EXPRESS_INFO);
        holder.type_image.setImageResource(R.drawable.express_img);

        holder.fl_card_time.addView(view, params);
        final DeliveryHolder deliveryHolder = new DeliveryHolder(view);

        deliveryHolder.card_view.setTag(event);
        view.setTag(event.getId());
        RequestExpressTrace runnable = new RequestExpressTrace(event, deliveryHolder);
        executorService.submit(runnable);
        if (null != event.getTrace_date() && !isEmpty(DateUtils.formatDate2String2(event.getTrace_date()))) {
            deliveryHolder.tv_delivery_datetime.setText(DateUtils.formatDate2String2(event.getTrace_date()));
        }
        if (!isEmpty(event.getExpressProgress())) {
            deliveryHolder.tv_delivery_info.setText(event.getExpressProgress());
        }
        if (!isEmpty(event.getExpressCompany())) {
            TextUtilTools.setHighLightText(event.getExpressCompany(), mTitleHighLightCompile, deliveryHolder.tv_delivery_company_name, mContext);
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

       /* if(mExpressList !=null && mExpressList.size() > 1){
            setIsOpen(false, deliveryHolder);
            deliveryHolder.card_view_more.setVisibility(View.VISIBLE);
            deliveryHolder.card_view_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isOpen) {
                        setIsOpen(true, deliveryHolder);
                    }
                }
            });
        } else {
            deliveryHolder.card_view_more.setVisibility(View.GONE);
            deliveryHolder.ll_more_order.setVisibility(View.GONE);
        }*/
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
                    holder1.tv_delivery_datetime.setText(DateUtils.formatDate2String2(expressSchedule.getTrace_date()));
                }
                if (!isEmpty(expressSchedule.getExpressProgress())) {
                    holder1.tv_delivery_info.setText(expressSchedule.getExpressProgress());
                }
                if (!isEmpty(expressSchedule.getExpressCompany())) {
                    holder1.tv_delivery_company_name.setText(expressSchedule.getExpressCompany());
                }
                if (!isEmpty(expressSchedule.getExpressNum())) {
                    holder1.tv_order_num.setText(expressSchedule.getExpressNum());
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
            this.isOpen = false;
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

        /*add by zhengjl at 2017-2-7 for search
        搜索页面的卡片显示异常
        */
        holder.fl_card_time.setVisibility(View.VISIBLE);
        holder.divider_img.setVisibility(View.VISIBLE);

        holder.tv_time.setText(Constants.ALL_DAY);
        if (!isEmpty(event.getHotelName())) {
//            SpannableStringBuilder title = getHighLightedText(event.getHotelName());
//            hotelHolder.tv_hotel_name.setText(event.getHotelName(),mTitleHighLightCompile);
            TextUtilTools.setHighLightText(event.getHotelName(), mTitleHighLightCompile, holder.schdule_title, mContext);
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
        if ((!isEmpty(event.getHotelName()) && !isEmpty(event.getHotelAddress()))) {
            hotelHolder.tv_hotel_address.setText(event.getHotelAddress());
            hotelHolder.iv_hotel_location.setVisibility(View.VISIBLE);
            hotelHolder.tv_hotel_address.setVisibility(View.VISIBLE);
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

        holder.tv_time.setText(DateUtils.time2String(event.getDate()));
        /*add by zhengjl at 2017-2-7 for search
        搜索页面的卡片显示异常
        */
        holder.fl_card_time.setVisibility(View.VISIBLE);
        holder.divider_img.setVisibility(View.VISIBLE);
        holder.type_image.setImageResource(R.drawable.movie_img);
        if (!isEmpty(event.getCinemaName())) {
            movieHolder.iv_cinema_location.setVisibility(View.VISIBLE);
            movieHolder.tv_cinema_name.setVisibility(View.VISIBLE);
//            SpannableStringBuilder cInemaName = getHighLightedText(event.getCinemaName());
            movieHolder.tv_cinema_name.setText(event.getCinemaName());
        } else {
            movieHolder.tv_cinema_name.setVisibility(View.GONE);
            movieHolder.tv_cinema_name.setVisibility(View.GONE);
        }

        if (!isEmpty(event.getMovieName())) {
//            movieHolder.tv_movie_name.setText(event.getMovieName(),mTitleHighLightCompile);

            TextUtilTools.setHighLightText(event.getMovieName(), mTitleHighLightCompile, holder.schdule_title, mContext);
        }

        if (!isEmpty(DateUtils.date2String(event.getDate()))) {
            movieHolder.tv_date.setVisibility(View.VISIBLE);
            movieHolder.tv_time.setVisibility(View.VISIBLE);
            movieHolder.tv_week_date.setVisibility(View.VISIBLE);
            movieHolder.tv_date.setText(DateUtils.getDate(event.getDate()));
            movieHolder.tv_time.setText(DateUtils.time2String(event.getDate()));
            movieHolder.tv_week_date.setText(DateUtils.getWeekOfDate(event.getDate()));
        } else {
            movieHolder.tv_date.setVisibility(View.GONE);
            movieHolder.tv_time.setVisibility(View.GONE);
            movieHolder.tv_week_date.setVisibility(View.GONE);
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

        /*modify by zhengjl at 2017-2-7 for search
        搜索界面的卡片显示不正常
         */
        holder.schdule_title.setText(Constants.TRIP_INFO);
        holder.fl_card_time.setVisibility(View.VISIBLE);
        holder.divider_img.setVisibility(View.VISIBLE);
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
//            flightHolder.tv_start_station.setText(event.getStartAddress(),mTitleHighLightCompile);
            TextUtilTools.setHighLightText(event.getStartAddress(), mTitleHighLightCompile, flightHolder.tv_start_station, mContext);
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
//            flightHolder.tv_end_station.setText(event.getDestination(),mTitleHighLightCompile);
            TextUtilTools.setHighLightText(event.getDestination(), mTitleHighLightCompile, flightHolder.tv_end_station, mContext);
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

        /*modify by zhengjl at 2017-2-7 for search
        搜索界面的卡片显示不正常
         */
        holder.schdule_title.setText(Constants.TRIP_INFO);
        holder.fl_card_time.setVisibility(View.VISIBLE);
        holder.divider_img.setVisibility(View.VISIBLE);
        holder.type_image.setImageResource(R.drawable.train_img);
        if (!isEmpty(event.getOrdernumber())) {
            trainHolder.tv_order_num.setVisibility(View.VISIBLE);
            CardDetailsUtils.setTextView(trainHolder.tv_order_num, trainHolder.tv_order_num.getText().toString(), event.getOrdernumber());
        } else {
            trainHolder.tv_order_num.setVisibility(View.GONE);
        }
        if (!isEmpty(event.getDeparture())) {
//            SpannableStringBuilder title = getHighLightedText(event.getDeparture());
//            trainHolder.tv_start_station.setText(event.getDeparture(),mTitleHighLightCompile);
            TextUtilTools.setHighLightText(event.getDeparture(), mTitleHighLightCompile, trainHolder.tv_start_station, mContext);
        }
        if (!isEmpty(event.getDestination())) {
//            SpannableStringBuilder title = getHighLightedText(event.getDestination());
//            trainHolder.tv_end_station.setText(event.getDestination(),mTitleHighLightCompile);
            TextUtilTools.setHighLightText(event.getDestination(), mTitleHighLightCompile, trainHolder.tv_end_station, mContext);
        }
        if (!isEmpty(event.getTrainnumber())) {
            trainHolder.tv_train_num.setText(event.getTrainnumber());
        }
        if (!isEmpty(DateUtils.date2String(event.getDate()))) {
            trainHolder.tv_start_date.setVisibility(View.VISIBLE);
            trainHolder.tv_start_date.setText(DateUtils.date2String2(event.getDate()) + " " + DateUtils.getWeekOfDate(event.getDate()) + " " + DateUtils.time2String(event.getDate()));
//            trainHolder.tv_start_time.setText(DateUtils.time2String(event.getDate()));
        } else {
            trainHolder.tv_start_date.setVisibility(View.GONE);
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

        /*add by zhengjl at 2017-2-7 for search
        搜索页面的卡片显示异常
        */
        holder.fl_card_time.setVisibility(View.VISIBLE);
        holder.divider_img.setVisibility(View.VISIBLE);
        String money = null;
        //modify by zhengjl at 2017-2-21 修改搜索界面显示效果  begin
        /*
        if (!isEmpty(event.getRepaymentAmount())) {
            if(event.getRepaymentAmount().contains("美元")){
                int index = event.getRepaymentAmount().indexOf("美");
                String rmb = event.getRepaymentAmount().substring(0, index);
                String doll = event.getRepaymentAmount().substring(index,event.getRepaymentAmount().length());
                money = rmb + "\n" + doll;
            }else
                money = event.getRepaymentAmount();
        }
        */
        if (!isEmpty(event.getRepaymentAmount())) {
            //LogUtils.e("zjl", "event.getRepaymentAmount()" + event.getRepaymentAmount());
            String regEx = "[0-9]+(.)?";
//            String regEx= "\\d";
            Pattern p = Pattern.compile(regEx);
            Matcher m = p.matcher(event.getRepaymentAmount());
            StringBuilder strBuilder = new StringBuilder();
            while (m.find()) {
                String temp = m.group();
                strBuilder.append(temp);
            }
            if (strBuilder.length() != 0)
                money = strBuilder.toString();
            if (event.getRepaymentAmount().contains("美元") || event.getRepaymentAmount().contains("$")) {
                money = "$" + money;
                CardDetailsUtils.setTextViewWithTimeTable(bankHolder.tv_dollar_bill, bankHolder.tv_dollar_bill.getText().toString(), money);
            } else {
                //美元账单
                CardDetailsUtils.setTextViewWithTimeTable(bankHolder.tv_dollar_bill, bankHolder.tv_dollar_bill.getText().toString(), "$0.00");
                money = "￥" + money;
            }
        }

        if (!isEmpty(event.title)) {
            TextUtilTools.setHighLightText(event.title, mTitleHighLightCompile, holder.schdule_title, mContext);
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
        //modify by zhengjl at 2017-2-21 修改搜索界面显示效果  begin
    }

    /*Gionee zhengyt 2016-12-20 add for search not begin*/
    private void setNoteCard(NoteViewHolder holder, BaseNoteSchedule event) {
        final BaseNoteSchedule noteEvent = event;

        View view = LayoutInflater.from(mContext).inflate(R.layout.item_card_note_2, holder.note_frame);
        NoteItemViewHolder noteHolder = new NoteItemViewHolder(view); //NoteHolder
        holder.note_frame.setTag(holder);

        TextUtilTools.setHighLightText(noteEvent.getNoteTitle(), mTitleHighLightCompile, noteHolder.note_title, mContext);
        Date creatDate = new Date(noteEvent.getNoteCreateTime());
        String creatTime = new SimpleDateFormat("yyyy.MM.dd HH:mm").format(creatDate);
        noteHolder.note_time.setText(creatTime);

        //modify by zhengjl at 2017-1-20 备忘内容显示bug begin
        String content = noteEvent.getNoteContext();

        if (content != null) {
            //定义正则表达式，用于匹配路径
            Pattern p = Pattern.compile(Constants.MATCH_PATTERN, Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(content);
            int startIndex = 0;
            StringBuffer buf = new StringBuffer();
            //modified by luorw for GNSPR #72993 2017-03-15 begin
            boolean hasImage = false;
            boolean hasRecord = false;
            boolean imageIsAvailable = false;
            //取出内容中的除了路径的所有内容
            while (m.find()) {
                //取出路径前后的文字
                int start = m.start();
                buf.append(content.substring(startIndex, start));
                String matchStr = m.group();
                String endStr = matchStr.substring(matchStr.length() - 3, matchStr.length());
                //清单图标的路径
                if (endStr.equalsIgnoreCase("png") || endStr.equalsIgnoreCase("jpg")
                        || endStr.equalsIgnoreCase("jpeg") || endStr.equalsIgnoreCase("bmp")
                        || endStr.equalsIgnoreCase("gif")) {
                    if (!matchStr.contains(Constants.PATH_BILL_UNCHECKED_IMG) && !matchStr.contains(Constants.PATH_BILL_CHECKED_IMG)) {
                        hasImage = true;
                        Uri uri = Uri.parse(matchStr);
                        //判断当前图片附件只要本地能获取到，说明图片没有丢失，并且只需要执行一次，只要有图片就必须显示图片附件图表
                        if (!imageIsAvailable && isImageExists(uri)) {
                            imageIsAvailable = true;
                        }
                    }
                } else if (matchStr.contains(".amr")) {
                    hasRecord = true;
                }
                startIndex = start + matchStr.length();
            }
            if (m.find(0)) {
                //添加最后一个路径之后的文字
                if (startIndex < content.length() - 1) {
                    buf.append(content.substring(startIndex, content.length()));
                }
                //显示附件类型
                if (hasImage && hasRecord && imageIsAvailable) {
                    noteHolder.attach_img.setVisibility(View.VISIBLE);
                    noteHolder.attach_img.setImageDrawable(mContext.getResources().getDrawable(R.drawable.attachment_icon));
                } else if (hasImage && !hasRecord && imageIsAvailable) {
                    noteHolder.attach_img.setVisibility(View.VISIBLE);
                    noteHolder.attach_img.setImageDrawable(mContext.getResources().getDrawable(R.drawable.picture_icon));
                } else if (!hasImage && hasRecord && !imageIsAvailable) {
                    noteHolder.attach_img.setVisibility(View.VISIBLE);
                    noteHolder.attach_img.setImageDrawable(mContext.getResources().getDrawable(R.drawable.note_record_icon));
                } else {
                    noteHolder.attach_img.setVisibility(View.GONE);
                }
                //modified by luorw for GNSPR #72993 2017-03-15 end
            } else {
                noteHolder.attach_img.setVisibility(View.GONE);
            }
        } else {
            //没有内容，只有提醒
            noteHolder.note_content.setVisibility(View.GONE);
            noteHolder.attach_img.setVisibility(View.GONE);
        }

        if (noteEvent.getRemindDate() > 0) {
            Drawable right_draw = mContext.getResources().getDrawable(R.drawable.remind_icon_gray);
            right_draw.setBounds(0, 0, right_draw.getMinimumWidth(), right_draw.getMinimumHeight());
            noteHolder.note_title.setCompoundDrawables(null, null, right_draw, null);
//                ((ItemViewHolder) viewHolder).imgRemind.setVisibility(View.VISIBLE);
        } else {
            noteHolder.note_title.setCompoundDrawables(null, null, null, null);
//                ((ItemViewHolder) viewHolder).imgRemind.setVisibility(View.GONE);
        }


//    	TextUtilTools.setHighLightText(noteEvent.getNoteTitle()+noteEvent.getNoteContext(), mTitleHighLightCompile, noteHolder.note_content, mContext);

        //modify by zhengjl at 2017-1-20 备忘内容显示bug end


    }

    private boolean isImageExists(Uri uri) {
        File file = new File(uri.toString().replace("file://", ""));
        return file.exists();
    }

    /**
     * add by zhengjl at 2017-1-18
     * 提取内容
     * 有换行，取换行后面的为内容
     * 没有换行，20个字后面的为内容
     *
     * @param content
     * @return
     */
    private String getContentFromContent(String content) {
        if (!TextUtils.isEmpty(content)) {

            int index = 0;
            int end = content.length() - 1;
            while (index < content.length() && content.charAt(index) == '\n') {
                index++;
            }
            //去除文本后的换行符
            while (end > 0 && end <= content.length() - 1 && content.charAt(end) == '\n') {
                end--;
            }
            if (index >= end) return null;
            content = content.substring(index, end + 1);
//            LogUtils.e("zjl","去掉换行符--content:" + content);
            index = content.indexOf("\n");

            if (index > 0) {
                if (index < content.length() - 1) {
//                    LogUtils.e("zjl","...after..." + content.substring(index + 1, content.length()));
                    return content.substring(index + 1, content.length());
                } else {
                    return content.substring(0, index + 1).length() > 20 ? content.substring(21, content.length()) : null;
                }
            } else {
                return content.length() > 20 ? content.substring(21, content.length()) : null;
            }
        } else
            return null;
    }

    /*Gionee zhengyt 2016-12-20 add for search not End*/

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
            TextUtilTools.setHighLightText(selfCreateEvent.getTitle(), mTitleHighLightCompile, holder.schdule_title, mContext);
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
        /*add by zhengjl at 2017-2-7 for search
        搜索页面的卡片显示异常
        */
        holder.fl_card_time.setVisibility(View.VISIBLE);
        holder.divider_img.setVisibility(View.VISIBLE);
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
        switch (viewType) {
            case DATE_CARD:
                holder = new DateViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_card_date_search, parent, false));
                break;
            case TIME_CARD:
                holder = new TimeViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_card_time_search, parent, false));
                break;
            case NOTE_CARD:
                holder = new NoteViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_card_note, parent, false));
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
        LogUtils.i("liyy", "mneedHight:adapter....." + mNeedHighLight + " ,title:" + title + "  ,hightText:" + mTitleHighLightCompile);
        if (mNeedHighLight) {
            return TextUtilTools.hightLightText(title, mTitleHighLightCompile, R.color.search_highlight_color);
        } else {
            return new SpannableStringBuilder(title);
        }
    }

    class NoteViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout note_layout;
        private FrameLayout note_frame;
        private TextView note_type;

        //add by zhengjli at 2017-1-20 for GNSPR #65660 begin
        private ImageView note_divider_img;
//    	private LinearLayout item_card;

        public NoteViewHolder(View itemView) {
            super(itemView);
            note_layout = (RelativeLayout) itemView.findViewById(R.id.voice_note_layout);
            note_frame = (FrameLayout) itemView.findViewById(R.id.note_frame);
            note_type = (TextView) itemView.findViewById(R.id.note_type);
//			item_card = (LinearLayout)itemView.findViewById(R.id.item_card);
            note_divider_img = (ImageView) itemView.findViewById(R.id.search_divider_line);
        }

    }

    class NoteItemViewHolder extends NoteViewHolder {
        private TextView note_title;
        private TextView note_content;
        private ImageView attach_img;
        private ImageView remind_img;
        private TextView note_time;

        public NoteItemViewHolder(View itemView) {
            super(itemView);
            note_time = (TextView) itemView.findViewById(R.id.note_date);
            note_title = (TextView) itemView.findViewById(R.id.note_title);
            note_content = (TextView) itemView.findViewById(R.id.note_content);
            attach_img = (ImageView) itemView.findViewById(R.id.attach_img);
            remind_img = (ImageView) itemView.findViewById(R.id.remind_img);
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
        protected ImageView divider_img;
        protected TextView tv_date_tag;


        public TimeViewHolder(View itemView) {
            super(itemView);
            item_layout = (LinearLayout) itemView.findViewById(R.id.item_card);
            schdule_title = (TextView) itemView.findViewById(R.id.schdule_title);
            tv_time = (TextView) itemView.findViewById(R.id.tv_time);
            fl_card_time = (FrameLayout) itemView.findViewById(R.id.fl_card_time);
            location_img = (ImageView) itemView.findViewById(R.id.iv_manual_create_location_in_title);
            time_layout = (RelativeLayout) itemView.findViewById(R.id.time_layout);
            divider_img = (ImageView) itemView.findViewById(R.id.divider_img);
            tv_date_tag = (TextView) itemView.findViewById(R.id.tv_date_tag);
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
        private TextView tv_seat_num;

        public MovieHolder(View itemView) {
            super(itemView);
            tv_cinema_name = (TextView) itemView.findViewById(R.id.tv_cinema_name);
            tv_date = (TextView) itemView.findViewById(R.id.tv_date);
            tv_week_date = (TextView) itemView.findViewById(R.id.tv_week_date);
            tv_time = (TextView) itemView.findViewById(R.id.tv_time);
            tv_cell_num = (TextView) itemView.findViewById(R.id.tv_cell_num);
            tv_seat_num = (TextView) itemView.findViewById(R.id.tv_seat_num);
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
        private TextViewSnippet tv_delivery_company_name;
        private TextView tv_order_num;
        private TextView tv_delivery_datetime;
        private TextView tv_delivery_info;
        private TextView tv_delivery_reason;
        private TextView tv_hotel_tel_num;
        private FrameLayout card_view;
        private View view_gap;

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
        }
    }


    /*Gionee zhengyt 2016-12-20 add for search not Begin*/
    class NoteHolder extends RecyclerView.ViewHolder {
        private TextViewSnippet tv_content;

        public NoteHolder(View itemView) {
            super(itemView);

            tv_content = (TextViewSnippet) itemView.findViewById(R.id.tv_content);
        }
    }
    /*Gionee zhengyt 2016-12-20 add for search not End*/


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


    private static int noteCardItemCount = 0;

    public void initNoteCard() {
        noteCardItemCount = 0;
    }

    //add by zhengjl at 2017-1-20 for GNSPR #65660 no end
    private static int schduleCardItemCount = 0;

    public void initSchduleCard() {
        schduleCardItemCount = 0;
    }


}