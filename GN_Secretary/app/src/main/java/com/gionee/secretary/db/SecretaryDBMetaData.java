package com.gionee.secretary.db;

import android.provider.BaseColumns;

/**
 * Created by luorw on 5/11/16.
 */
public class SecretaryDBMetaData {
    public static final String DB_NAME = "secretary_db";

    public static final int DB_VERSION = 3;

    public static final class T_BASE_SCHEDULE_MeteData implements BaseColumns {
        public static final String TABLE_NAME = "t_base_schedule_info";
        public static final String TYPE = "type";//银行类,火车票,飞机票,自建,电影,快递
        public static final String TITLE = "title";//日程用于搜索的标题
        public static final String DATE = "date";//日程发生时间
        public static final String IS_ALL_DAY = "is_all_day";//全天 0否,1是
        public static final String REMIND_TYPE = "remind_type";//提醒类型
        public static final String REMIND_PERIOD = "remind_period";//提醒周期
        public static final String REMIND_DATE = "remind_date";//提醒时间
        public static final String IS_SMART_REMIND = "is_smart_remind";//-1不提醒;0常规提醒；1智能提醒
        public static final String IS_REMIND_ACTIVE = "is_remind_active"; // -1 已提醒 ；0未激活；1激活
        public static final String PERIOD_ID = "period_id";//重复周期日程的id
        public static final String SENDER = "sender";//发件人
        public static final String Content = "content";//短信内容
        public static final String Source = "source";//短信来源

        public static final String SEARCH_CONTENT = "search_content";//备忘搜索字段，是去掉图片和录音路径的其他字符
        public static final String BROADCAST_SORT_DATE = "broadcast_sort_date";//日程播报时用于排序的时间
    }

    public static final class T_SELF_CREATE_SCHEDULE_MeteData implements BaseColumns {
        public static final String TABLE_NAME = "t_self_create_schedule_info";
        public static final String ADDRESS = "address";//地点
        public static final String DESCRIPTION = "description";//描述
        public static final String TRIP_MODE = "trip_mode";//出行方式
        public static final String END_TIME = "end_time";//设置日程结束时间
        public static final String SUB_PERIOD_DATE = "sub_period_date";//与base表中的date相同，用于更新或删除周期重复的日程
        public static final String SUB_PERIOD_ID = "sub_period_id";//重复周期日程的id
        public static final String PERIOD = "period";//结束时间和开始时间的差值
        public static final String ADDRESS_REMARK = "address_remark";//地点备注
    }

    public static final class T_TRAIN_SCHEDULE_MeteData implements BaseColumns {
        public static final String TABLE_NAME = "t_train_schedule_info";
        public static final String START_TIME = "start_time";//出发时间
        public static final String ARRIVAL_TIME = "arrival_time";//到达时间
        public static final String DEPARTURE = "departure";//出发地
        public static final String DESTINATION = "destination";//目的地
        public static final String TRAINNUMBER = "trainnumber";//车次
        public static final String SEATNUMBER = "seatnumber";//座位号
        public static final String ORDERNUMBER = "ordernumber";//订单号
        public static final String ORDERPERSON = "orderperson";//订票人
    }

    public static final class T_BANK_SCHEDULE_MeteData implements BaseColumns {
        public static final String TABLE_NAME = "t_bank_schedule_info";
        public static final String BANK_NAME = "bank_name";//银行名称
        public static final String REPAYMENT_MONTH = "repayment_month";//还款月份
        public static final String BILL_MONTH = "bill_month";//账单月份
        public static final String REPAYMENT_AMOUNT = "repayment_amount";//还款金额
        public static final String CARD_NUM = "card_num";//卡帐号
        public static final String ALERT_DESC = "alert_desc";//提醒描述
    }

    public static final class T_EXPRESS_SCHEDULE_MeteData implements BaseColumns {
        public static final String TABLE_NAME = "t_express_schedule_info";
        public static final String TRACE_DATE = "trace_date";//物流跟踪更新时间
        public static final String EXPRESS_NUM = "express_num";//快递单号
        public static final String EXPRESS_COMPANY = "express_company";//快递公司
        public static final String EXPRESS_STATE = "express_state";//派送状态
        public static final String EXPRESS_PROGRESS = "express_progress";//派送进度描述
        public static final String EXPRESS_CODE = "express_code";//快递简写
        public static final String STATE = "state";//订单状态；0发件、1在途中、2签收;
    }

    public static final class T_FLIGHT_SCHEDULE_MeteData implements BaseColumns {
        public static final String TABLE_NAME = "t_flight_schedule_info";
        public static final String ARRIVAL_TIME = "arrival_time";//到达时间
        public static final String START_ADDRESS = "start_address";//出发地
        public static final String DESTINATION = "destination";//到达地
        public static final String FLIGHT_NUM = "flight_num";//航班
        public static final String PASSENGER = "passenger";//乘客
        public static final String TICKET_NUM = "ticket_num";//票号
        public static final String AIRLINE_SOURCE = "airline_source";//机票来源
        public static final String SERVICE_NUM = "service_num";//客服电话
        public static final String ALERT_DESC = "alert_desc";//提示
    }

    public static final class T_HOTEL_SCHEDULE_MeteData implements BaseColumns {
        public static final String TABLE_NAME = "t_hotel_schedule_info";
        public static final String HOTEL_NAME = "hotel_name";//酒店名称
        public static final String CHECKOUT_DATE = "check_out_date";//退房日期
        public static final String ROOM_STYLE = "room_style";//房型
        public static final String CHECKIN_PEOPLE = "check_in_people";//入住人
        public static final String ROOM_COUNTS = "room_counts";//房间数
        public static final String SERVICE_NUM = "service_num";//客服电话
        public static final String HOTEL_ADDRESS = "hotel_address";//酒店地址
    }

    public static final class T_MOVIE_SCHEDULE_MeteData implements BaseColumns {
        public static final String TABLE_NAME = "t_movie_schedule_info";
        public static final String MOVIE_NAME = "movie_name";//电影名称
        public static final String CINEMA_NAME = "cinema_name";//电影院名称
        public static final String SEAT_DESC = "seat_desc";//座位描述
        public static final String TICKET_CERTIFICATE = "ticket_certificate";//取票凭证
        public static final String PLAY_TIME = "play_time";//观影时间
    }

    public static final class T_SEARCH_HISTORY_MeteData implements BaseColumns {
        public static final String TABLE_NAME = "t_search_history";
        public static final String CONTENT = "content"; //搜索关键字
        public static final String SEARCH_CONTENT = "search_content"; //搜索内容
    }

    public static final class T_VOICE_NOTE_MeteData implements BaseColumns {
        public static final String TABLE_NAME = "t_voice_note";
        public static final String CREATE_TIME = "create_time"; //便签创建时间
        public static final String CONTENT = "content"; //便签内容
        public static final String TITLE = "title"; //便签标题
        public static final String REMIND_TIME = "remind_time"; //便签提醒时间
        public static final String IS_REMIND_ACTIVE = "is_remind_active"; // -1 已提醒 ；0未激活；1激活
        public static final String SEARCH_CONTENT = "search_content";//搜索内容，与content的区别是不包含图片和录音路径
        public static final String ATTACHMENT_TYPE = "attachment_type";
    }

    public static final class T_VOICE_NOTE_RECORD_MeteData implements BaseColumns {
        public static final String TABLE_NAME = "t_voice_note_record";
        public static final String RECORD_TIME = "record_time";//录音时长
        public static final String RECORD_URI = "record_uri";//录音路径
        public static final String NOTE_ID = "note_id";//note ID外键
    }
}
