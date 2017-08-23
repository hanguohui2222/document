package com.gionee.secretary.constants;

/**
 * Created by rongdd on 16-5-11.
 */
public class Constants {
    public static final boolean SHOWTAG = true;

    public static final String CITYCODE = "citycode";
    public static final String HISTORY_ADDRESS = "history_address";
    public static final String ADDRESS = "address";
    public static final String LONGITUDE = "longitude";
    public static final String LATITUDE = "latitude";
    public static final String DESC = "desc";
    public static final String PACKAGE_WEATHER = "com.coolwind.weather";
    public static final int ROUTE_TYPE_BUS = 1;
    public static final int ROUTE_TYPE_DRIVE = 2;
    public static final int ROUTE_TYPE_WALK = 3;

    public static final int NOT_REMIND = -1;
    public static final int GENERAL_REMIND = 0;
    public static final int SMART_REMIND = 1;

    public static final int TYPE_BUS = 11;
    public static final int TYPE_DRIVE = 22;
    public static final int TYPE_WALK = 33;
    public static final int SELF_CREATE_TYPE = 100;
    public static final int BANK_TYPE = 101;
    public static final int TRAIN_TYPE = 102;
    public static final int FLIGHT_TYPE = 103;
    public static final int MOVIE_TYPE = 104;
    public static final int HOTEL_TYPE = 105;
    public static final int EXPRESS_TYPE = 106;
    public static final int WEATHER_TYPE = 107;

    public static final int NOTE_TYPE = 108;

    public static final int EMPTY_TYPE = 109;

    public static final int FOOTER_LOADING = 200;
    public static final int FOOTER_END = 201;
    public static final int FOOTER_FAILED = 202;
    public static final int FOOTER_NORMAL = 203;

    public static final int PAGE_CONTENT_COUNT = 15;

    public static final int IS_REMIND_ACTIVE = 1;
    public static final int REMIND_NOT_ACTIVE = 0;
    public static final int REMIND_DONE = -1;
    public static String NORMAL_REMIND_ACTION = "com.gionee.secretary.remind.NORMAL_ALERT";
    public static String NOTE_REMIND_ACTION = "com.gionee.secretary.remind.NOTE_ALERT";
    public static String INTELLIGENT_REMIND_ACTION = "com.gionee.secretary.remind.INTELLIGENT_ALERT";
    public static String FREEZING_PASSWORD_ACTION = "com.gionee.secretary.password.FREEZE";
    public static String UNDO_FREEZING_PASSWORD_ACTION = "com.gionee.secretary.password.UNDO_FREEZE";
    public static final int FREEZING_PASSWORD_REQUEST_CODE = 1;
    public static String REMIND_CLOCK_RECEIVER_ACTION = "com.gionee.secretary.remind";
    public static String REMIND_BOOTCOMPLETE_RECEIVER_ACTION = "com.gionee.remind.bootcomplete";
    public static String REFRESH_TASK_ACTION = "com.gionee.remind.refresh";
    public static String REFRESH_BROADCAST_TASK_ACTION = "com.gionee.broadcast.refresh";
    public static String START_BROADCAST_TASK_ACTION = "com.gionee.broadcast.start";

    public static String RECYCLE_TYPE = "recycleType";
    public static final int RECYCLETYPE_ONCE = 1000;//1次
    public static final int RECYCLETYPE_DAY = 1001;//每天
    public static final int RECYCLETYPE_WEEK = 1002;//每周
    public static final int RECYCLETYPE_MONTH = 1003;//每月
    public static final int RECYCLETYPE_YEAR = 1004;//每年

    public static String REFRESH_FOR_MAIN_UI = "com.gionee.secretary.refresh.mainui";
    public static String REFRESH_FOR_NOTE_DETAIL_UI = "com.gionee.secretary.refresh.note_detail_ui";
    public static final String[] expressCode = {"7TLSWL", "AJ", "ANE", "AXD", "BALUNZHI", "BFDF", "BKWL", "BQXHM", "BSWL", "BTWL", "CCES", "CITY100", "COE", "CSCY", "CXWL", "DBL", "DCWL", "DHWL",
            "DSWL", "DTKD", "DTWL", "DYWL", "EMS", "FAST", "FBKD", "FEDEX", "FHKD", "FKD", "FYPS", "FYSD", "GDEMS", "GDKD", "GHX", "GKSD", "GSD", "GTKD", "GTO", "GTSD",
            "HBJH", "HFWL", "HHKD", "HHTT", "HLKD", "HLWL", "HMJKD", "HMSD", "HOAU", "hq568", "HQKY", "HSWL", "HTKY", "HTWL", "HXLWL", "HYLSD", "JD", "JGSD", "JIUYE", "JJKY", "JLDT",
            "JTKD", "JXD", "JYKD", "JYM", "JYSD", "JYWL", "KLWL", "KTKD", "KYDSD", "KYWL", "LB", "LBKD", "LHKD", "LHT", "LJD", "LJS", "MB", "MDM", "MHKD", "MLWL", "MSKD", "NEDA",
            "NJSBWL", "PADTF", "PXWL", "QCKD", "QFKD", "QRT", "RFD", "RLWL", "SAD", "SAWL", "SBWL", "SDHH", "SDWL", "SF", "SFWL", "SHLDHY", "SHWL", "SJWL", "ST", "STO", "STSD", "SURE",
            "SXHMJ", "SYJHE", "SYKD", "THTX", "TSSTO", "UAPEX", "UC", "WJWL", "WTP", "WXWL", "XBWL", "XFEX", "XGYZ", "XLYT", "XYT", "YADEX", "YBJ", "YCWL", "YD", "YDH", "YFEX", "YFHEX",
            "YFSD", "YJSD", "YLSY", "YMWL", "YSH", "YSKY", "YTD", "YTFH", "YTKD", "YTO", "YXWL", "YZPY", "ZENY", "ZHQKD", "ZJS", "ZMKM", "ZRSD", "ZTE", "ZTKY", "ZTO", "ZTWL", "ZTWY", "ZWYSD",
            "ZYWL", "ZZJH"};
    public static final String[] expressFullName = {"7天连锁物流", "安捷快递", "安能物流", "安信达快递", "巴伦支快递", "百福东方", "宝凯物流", "北青小红帽", "邦送物流", "百世物流", "CCES快递", "城市100",
            "COE东方快递", "长沙创一", "传喜物流", "德邦快递", "德创物流", "东红物流", "D速物流", "店通快递", "大田物流", "大洋物流快递", "EMS", "快捷速递", "飞豹快递", "FedEx联邦快递", "飞狐快递", "飞康达",
            "飞远配送", "凡宇速递", "广东邮政", "冠达快递", "挂号信", "港快速递", "共速达", "广通速递", "国通快递", "高铁速递", "河北建华", "汇丰物流", "华航快递", "天天快递", "韩润物流", "恒路物流",
            "黄马甲快递", "海盟速递", "天地华宇", "华强物流", "华企快运", "昊盛物流", "百世汇通", "户通物流", "华夏龙物流", "好来运快递", "京东快递", "京广速递", "九曳供应链", "佳吉快运", "嘉里大通",
            "捷特快递", "急先达", "晋越快递", "加运美", "久易快递", "佳怡物流", "康力物流", "快淘快递", "快优达速递", "跨越速递", "龙邦快递", "联邦快递", "蓝弧快递", "联昊通速递", "乐捷递", "立即送",
            "民邦速递", "门对门", "民航快递", "明亮物流", "闽盛快递", "能达速递", "南京晟邦物流", "平安达腾飞快递", "陪行物流", "全晨快递", "全峰快递", "全日通快递", "如风达", "日昱物流", "赛澳递", "圣安物流",
            "盛邦物流", "山东海红", "上大物流", "顺丰速运", "盛丰物流", "上海林道货运", "盛辉物流", "穗佳物流", "速通物流", "申通快递", "三态速递", "速尔快递", "山西红马甲", "沈阳佳惠尔", "世运快递",
            "通和天下", "唐山申通", "全一快递", "优速快递", "万家物流", "微特派", "万象物流", "新邦物流", "信丰快递", "香港邮政", "祥龙运通", "希优特", "源安达快递", "邮必佳", "远成物流", "韵达快递",
            "义达国际物流", "越丰物流", "原飞航物流", "亚风快递", "银捷速递", "亿领速运", "英脉物流", "亿顺航", "音素快运", "易通达", "一统飞鸿", "运通快递", "圆通速递", "宇鑫物流", "邮政平邮/小包",
            "增益快递", "汇强快递", "宅急送", "芝麻开门", "中睿速递", "众通快递", "中铁快运", "中通快递", "中铁物流", "中天万运", "中外运速递", "中邮物流", "郑州建华"};


    public static final String SHOWTIPS = "first_visit_application";
    public static final String SHOWVOICETIPS = "first_visit_voice_note";
    public static final String SHOWPROTECT = "first_show_protect";
    public static final String FILE_SCHEME = "file://";


    public final static String EVENT_ID = "SECTETARY_EVENT";
    public final static String NOTIFACATION_FLAG = "nofication_flag";
    public final static String APPWIDGET_FLAG = "appwidget_flag";

    //    public final static String APPID = "D8B574DFE464406E96E8DCDFD2395ABD"; //测试
    public static final String APPID = "D39AABB0B551485D82E70329CCCECF0E"; //生产
    public static final String SUFFIX = "_photo";

    public static final String APP_MARKET_PACKAGENAME = "com.gionee.aora.market";
    public static final String APP_MARKET_CLASSNAME = "com.gionee.aora.market.GoApkLoginAndRegister";

    public static final String APP_PAY_PACKAGENAME = "com.eg.android.AlipayGphone";
    public static final String APP_PAY_CLASSNAME = "com.eg.android.AlipayGphone.AlipayLogin";

    public static final String APP_MAP_PACKAGENAME = "com.autonavi.minimap";
    public static final String APP_MAP_CLASSNAME = "com.autonavi.map.activity.SplashActivity";

    public static final String MARKET_SEARCH_ACTION = "com.gionee.aora.market.GoMarketSearchResult";
    public static final String MARKET_PAY_EXTRA_KEY1 = "search_key";
    public static final String MARKET_PAY_EXTRA_KEY2 = "key_value";
    public static final String MARKET_EXTRA_PAY_VALUE = "支付宝";
    public static final String MARKET_EXTRA_MAP_VALUE = "高德地图";

    public final static String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";
    public final static String SMS_PDUS = "pdus";
    public final static String SMS_SENDER = "sender";
    public final static String SMS_CONTENT = "smscontent";
    public final static String SCHEDULE_KEY = "schedule";
    public final static String SCHEDULE_ID_KEY = "scheduleid";
    public final static String EVENT_ID_KEY = "eventid";
    public final static String LAST_EVENT_ID_KEY = "last_eventid";
    public final static String EVENT_SETUP = "eventsetup";
    public final static String SELF_CREATE_CLASS_NAME = "com.gionee.secretary.view.SelfCreateScheduleActivity";
    public final static String IS_REPEAT_EVENT = "isRepeatEvent";

    //dataType
    public final static String DATATYPE_BANK = "sync_bank_schedule_info";
    public final static String DATATYPE_BASE = "sync_base_schedule_info";
    public final static String DATATYPE_EXPRESS = "sync_express_schedule_info";
    public final static String DATATYPE_FLIGHT = "sync_flight_schedule_info";
    public final static String DATATYPE_HOTEL = "sync_hotel_schedule_info";
    public final static String DATATYPE_MOVIE = "sync_movie_schedule_info";
    public final static String DATATYPE_SELF_CREATE = "sync_self_create_schedule_info";
    public final static String DATATYPE_TRAIN = "sync_train_schedule_info";
    public final static String DATATYPE_VOICE_NOTE = "sync_voice_note";
    public final static String DATATYPE_SEARCH_HISTORY = "sync_search_history";

    public final static String YOUJU_PASSWORD = "GN_OPEN_PASSWORD_PROTECTED";
    public static final String SHARED_PREFS_NAME = "com_gionee_secretary_preferences";
    public static final String EXTRA_PASSWORD = "password"; // 密码
    public static final String EXTRA_CLOSE_PASSWORD_SWITCH = "close_password_switch"; // 密码开关
    public static final int PASSWORD_LENGTH = 4; // 密码长度
    public static final String LOCK_SWITCH = "lock_switch"; //是否启用安全锁
    public static final String LOCK_STATE = "lock_state"; // 安全锁运行状态
    public static final String FREEZING_PASSWORD_STATE = "freezing_password_state"; // 密码冷冻状态
    //public static final String LOCK_PASSWORD = "password"; // 密码
    public final static String ACCOUNT_TRAVEL_MODE_YOUJU = "trasportation";
    public final static String ACCOUNT_BROADCAST_TIME_YOUJU = "broadcast_time";

    public static final String TRAVEL_MODE_PREFERENCE_KEY = "preference_default_travel_method";
    public static final String REMIND_SWITCH_PREFERENCE_KEY = "preference_events_remind_enable";
    public static final String BROADCAST_SWITCH_PREFERENCE_KEY = "preference_events_broadcast_enable";
    public static final String BROADCAST_TIME_PREFERENCE_KEY = "preferences_modify_broadcast_time";
    public static final String NOTIFICATION_RING_PREFERENCE_KEY = "preferences_modify_ring";
    public static final String NOTIFICATION_RING_URI_KEY = "notification_ring_uri";
    public static final String EXPRESS_SWITCH_PREFERENCE_KEY = "preference_show_express_status";
    public static final String PASSWORD_SWITCH_PREFERENCE_KEY = "preference_private_protect";
    public static final String WIDGET_SWITCH_PREFERENCE_KEY = "preference_show_widget";
    public static final String ROOT_PREFERENCE_KEY = "parent";
    public static final String USER_PREFERENCE_KEY = "preferences_login";

    public class RemindConstans {
        public static final String NOTIFICATION_ID_KEY = "notificationid";
        public static final String SCHEDULE_KEY = "schedule";
        public static final String ACTION_KEY = "action";
        public static final String SCHEDULEID_KEY = "scheduleid";
        public static final String NOTE_KEY = "noteid";
        public static final String MSG_KEY = "msg";
        public static final String REMIND_REFRESH = "enter_application_first_time";

        public static final int INVALID_NOTIFICATION_ID = -1;
        public static final long SNOOZE_DELAY = 5 * 60 * 1000L;
        public static final long TWO_YEAR_BEFORE = 2 * 365 * 24 * 60 * 60 * 1000L;
        public static final long INVALID_REMIND_TIME = -1;
        public static final long DISMISS_OVERDUE_TIME = 60 * 1000;
        public static final String SCHEDULE_NOTIFICATION = "schedule_notification";
        public static final String NOTE_NOTIFICATION = "note_notification";
    }

    public static final String LAUNCH_FROM_ADDRESS_ACTION = "com.gionee.secretary.address.action";
    public static final String LAUNCH_FROM_ADDRESS_REMARK_ACTION = "com.gionee.secretary.address.remark.action";
    public static final String TRAVEL_MODE = "travel_mode";
    public static final String TIPS_POINT_SP = "tips_point_sp";
    public static final String SHOW_HOME_PAGE_TIPS = "show_home_page_tips";
    public static final String SHOW_NEW_SCHEDULE_TIPS = "show_new_schedule_tips";
    public static final String SHOW_NEW_NOTE_TIPS = "show_new_note_tips";
    public static final String SHOW_HOME_PAGE_TIPS_POINT = "show_home_page_tips_point";
    public static final String SHOW_NEW_SCHEDULE_TIPS_POINT = "show_new_schedule_tips_point";
    public static final String SHOW_NEW_NOTE_TIPS_POINT = "show_new_note_tips_point";
    public static final String SHOW_HOME_PAGE_GESTURE_TIPS_LANDSCAPE = "show_home_gesture_page_tips_landscape";
    public static final String SHOW_HOME_PAGE_GESTURE_TIPS_VERTICALLY = "show_home_gesture_page_tips_vertically";
    public static final int TIPS_FOR_HOME_PAGE = 1;
    public static final int TIPS_FOR_NEW_SCHEDULE = 2;
    public static final int TIPS_FOR_NEW_NOTE = 3;
    public static final String DELETE_ADDRESS = "delete_address";
    public static final String MODIFY_ADDRESS = "modify_address";
    public static final String ADDRESS_REMARK = "address_remark";

    public static final String TRIP_INFO = "出行信息";
    public static final String EXPRESS_INFO = "快递信息";
    public static final String CREDIT_BILL = "信用卡账单";
    public static final String NEW_SCHDULE = "新日程";
    public static final String ALL_DAY = "全天";
    //modified by luorw for 修改正则表达式适配更多路径类型 20170313 begin
//    public static final String MATCH_PATTERN = "file:///([\\w\\W]*?)\\.(amr|png|jpeg|jpg|bmp|gif|wbmp)[\\w\\W]*?";
    //added by luorw for GNSPR #101691 20170822 begin
    public static final String MATCH_PATTERN = "file:///([\\w\\W]*?)\\<gns>";
    //added by luorw for GNSPR #101691 20170822 end
    //modified by luorw for 修改正则表达式适配更多路径类型 20170313 end
    public static final String NOTE_PATH = "/sdcard/Pictures/SecretaryMedia";
    public static final String PATH_BILL_CHECKED_IMG = "file:///data/user/0/secretary/files/billIsChecked.png";
    public static final String PATH_BILL_UNCHECKED_IMG = "file:///data/user/0/secretary/files/billUnChecked.png";//这里保证两个长度一样，在onTextChanged时方便调用
    public static final String SYSTEM_PERMISSION_ALERT_SUPPORT = "ro.gn.sys_perm_alert.support";
    public static final String NETWORK_CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";
    public static final String AIRPLANE_MODE_CHANGE = "android.intent.action.AIRPLANE_MODE";
    public final static String DESK_CLOCK_FULL_SCREEN_DISMISS_ACTION = "com.android.deskclock.action.ALARM_SNOOZE_OR_CLOSE";
    public final static String DESK_CLOCK_HEAD_UP_DISMISS_ACTION = "com.android.deskclock.inten.HEAD_UP_STOP";
    public final static String DESK_CLOCK_OVER_TURN_DISMISS_ACTION = "com.android.deskclock.action.OVERTURN";
    public final static String LONG_SCREEN_SHOT_START = "com.gionee.longscreenshot.action.start_longscreenshot";
    public final static String LONG_SCREEN_SHOT_SHARE_RESULT = "com.gionee.secretary.action.longscreenshot_share";

    public static final int NONE_TYPE = 0;
    public static final int ATTACHMENT_TYPE = 1;
    public static final int PICTURE_TYPE = 2;
    public static final int RECORD_TYPE = 3;

    public static final String CAMERA_IMG_SP = "camera_img_sp";
    public static final String CURRENT_IMG_PATH = "current_img_path";
    //added by luorw for GNSPR #101691 20170822 begin
    public static final String URI_END_TAG = "<gns>";//副文本中uri后缀增加标签，表示gionee secretary
    public static final int URI_END_TAG_LENGTH = 5;
    //added by luorw for GNSPR #101691 20170822 end
}
