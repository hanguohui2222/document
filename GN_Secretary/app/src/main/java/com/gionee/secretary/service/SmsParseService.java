package com.gionee.secretary.service;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;

import com.gionee.secretary.bean.BankSchedule;
import com.gionee.secretary.bean.ExpressSchedule;
import com.gionee.secretary.bean.FlightSchedule;
import com.gionee.secretary.bean.HotelSchedule;
import com.gionee.secretary.bean.MovieSchedule;
import com.gionee.secretary.bean.TrainSchedule;
import com.gionee.secretary.constants.CardBaseType;
import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.dao.ScheduleInfoDao;
import com.gionee.secretary.utils.CardDetailsUtils;
import com.gionee.secretary.utils.DateUtils;
import com.gionee.secretary.utils.LogUtils;
import com.gionee.secretary.utils.RemindUtils;
import com.gionee.secretary.utils.TextUtilTools;
import com.gionee.secretary.utils.WidgetUtils;
import com.ted.android.core.SmsEntityLoader;
import com.ted.android.core.SmsParserEngine;
import com.ted.android.data.SmsEntity;
import com.ted.android.data.bubbleAction.ActionBase;
import com.ted.android.data.bubbleAction.CarrierAction;
import com.ted.android.smscard.CardBase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Created by hangh on 5/14/16.
 */
public class SmsParseService extends IntentService {
    private SmsEntityLoader mEntityLoader;
    private static final String TAG = "SmsParseService";

    public SmsParseService() {
        super("SmsParseService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SmsParserEngine.getInstance(this);
        mEntityLoader = SmsEntityLoader.getInstance(getApplicationContext());
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        smsEntityParse(intent);
    }

    /**
     * 短信解析引擎开始解析
     *
     * @param intent
     */
    private synchronized void smsEntityParse(Intent intent) {
        final String sender = intent.getExtras().getString(Constants.SMS_SENDER);
        final String smsContent = intent.getExtras().getString(Constants.SMS_CONTENT);
        long key = System.currentTimeMillis();
        SmsEntity entity = mEntityLoader.loadSmsEntity(key, smsContent, sender,
                System.currentTimeMillis(),
                new SmsEntityLoader.SmsEntityLoaderCallback() {
                    @Override
                    public void onSmsEntityLoaded(Long aLong, SmsEntity smsEntity) {
                        LogUtils.i(TAG, "smsEntity = " + smsEntity);
                        LogUtils.i(TAG, "smsEntity.getCardBase() = " + smsEntity.getCardBase());
                        if (smsEntity.getCardBase() != null) {
                            parseAndSaveDB(smsEntity, sender, smsContent);
                        }
                    }
                });
        if (entity != null) {
            LogUtils.i(TAG, entity.toString());
        }
    }

    private void parseAndSaveDB(SmsEntity smsEntity, String smsSender, String smsContent) {
        //银行类title为“信用卡账单/贷款账单+银行名称”，快递类title为快递公司名称，火车票飞机票类title为“出发地到达地”，酒店类title为酒店名称,电影类title为电影名称
        CardBase cardBase = smsEntity.getCardBase();
        int cardBaseType = cardBase.getCardBaseType();
        LogUtils.e(TAG, "cardBaseType = " + cardBaseType);
        LogUtils.e(TAG, "card type = " + smsEntity.getCardBase().getCardType());
        switch (cardBaseType) {
            case CardBaseType.TYPE_TRAIN:
                saveTrainSchedule(cardBase, smsSender, smsContent);
                break;
            case CardBaseType.TYPE_FLIGHT:
                saveFlightSchedule(cardBase, smsSender, smsContent);
                break;
            case CardBaseType.TYPE_BANK:
                saveBankSchedule(cardBase, smsSender, smsContent);
                break;
            case CardBaseType.TYPE_MOVIE:
                saveMovieSchedule(cardBase, smsSender, smsContent);
                break;
            case CardBaseType.TYPE_HOTEL:
                saveHotelSchedule(cardBase, smsSender, smsContent);
                break;
            case CardBaseType.TYPE_CARRIER:// 快递
                saveExpressSchedule(cardBase, smsSender, smsContent, smsEntity);
                break;
        }
        sendBroadcast(new Intent(Constants.REFRESH_FOR_MAIN_UI));
        WidgetUtils.updateWidget(this);
    }

    private void saveExpressSchedule(CardBase cardBase, String smsSender, String smsContent, SmsEntity smsEntity) {
        LogUtils.e(TAG, "TYPE_CARRIER");
        ExpressSchedule expressSchedule = new ExpressSchedule();
        Iterator expressSubTitle = cardBase.getSubTitle().entrySet()
                .iterator();
        while (expressSubTitle.hasNext()) {
            Map.Entry entry = (Map.Entry) expressSubTitle.next();
            String key = (String) entry.getKey();
            String val = (String) entry.getValue();
            if ("快递单号".equals(key)) {
                expressSchedule.setExpressNum(val);
            } else if ("签收状态".equals(key)) {
                expressSchedule.setExpressState(val);
            }
        }

        String name = TextUtilTools.getMessageSign(smsContent);
        if (TextUtils.isEmpty(name)) {
            for (ActionBase actionBase : smsEntity.getAllActions()) {
                if (actionBase instanceof CarrierAction) {
                    String carrierCom = ((CarrierAction) actionBase).getCarrierName();
                    expressSchedule.setExpressCompany(carrierCom);
                    expressSchedule.setTitle(carrierCom);
                }
            }
        } else {
            expressSchedule.setExpressCompany(name);
            expressSchedule.setTitle(name);
            if("苏宁".equals(name) || "德邦物流".equals(name)) {
                for (ActionBase actionBase : smsEntity.getAllActions()) {
                    if (actionBase instanceof CarrierAction) {
                        String carrierCom = ((CarrierAction) actionBase).getCarrierName();
                        if ("debangwuliu".equals(carrierCom)) {
                            expressSchedule.setExpressCompany("德邦快递");
                            expressSchedule.setTitle("德邦快递");
                        }
                    }
                }
            }
        }

        Iterator expressData = cardBase.getData().entrySet().iterator();
        while (expressData.hasNext()) {
            Map.Entry entry = (Map.Entry) expressData.next();
            String key = (String) entry.getKey();
            String val = (String) entry.getValue();
            if ("快递单号".equals(key)) {
                expressSchedule.setExpressNum(val);
            }
        }
        if (expressSchedule.getExpressCompany() != null) {
            List<String> expresslist = Arrays.asList(Constants.expressFullName);
            int index = expresslist.indexOf(expressSchedule.getExpressCompany());
            if (index != -1) {
                expressSchedule.setExpressCode(Constants.expressCode[index]);
            }
        }
        Calendar current = Calendar.getInstance();
        current.set(Calendar.HOUR_OF_DAY, 0);
        current.set(Calendar.MINUTE, 0);
        current.set(Calendar.SECOND, 0);
        current.set(Calendar.MILLISECOND, 0);
        expressSchedule.setDate(current.getTime());
        expressSchedule.setAllDay(true);
        expressSchedule.setIsSmartRemind(Constants.NOT_REMIND);
        expressSchedule.setType(Constants.EXPRESS_TYPE);
        expressSchedule.setRemindDate(-1);//无需提醒
        expressSchedule.setSmsSender(smsSender);
        expressSchedule.setSmsContent(smsContent);
        expressSchedule.setPeriodID(-1);
        expressSchedule.setRemindPeriod("一次");
        ScheduleInfoDao.getInstance(this).saveScheduleToDB(expressSchedule);
    }

    private void saveHotelSchedule(CardBase cardBase, String smsSender, String smsContent) {
        LogUtils.e(TAG, "TYPE_HOTEL");
        Calendar hotelCalendar = Calendar.getInstance();
        HotelSchedule hotelSchedule = new HotelSchedule();
        Iterator hotelSubTitle = cardBase.getSubTitle().entrySet().iterator();
        while (hotelSubTitle.hasNext()) {
            Map.Entry entry = (Map.Entry) hotelSubTitle.next();
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if ("入住日期".equals(key) || "入住时间".equals(key)) {
                if (value.contains("/")) {
                    hotelCalendar = DateUtils.convertHotelDate(hotelCalendar, value, "MM/dd");
                }
                if (value.contains(".")) {
                    hotelCalendar = DateUtils.convertHotelDate(hotelCalendar, value, "MM.dd");
                }
                if (value.contains("-")) {
                    hotelCalendar = DateUtils.convertHotelDate(hotelCalendar, value, "MM-dd");
                }
            }
            if ("酒店名".equals(key)) {
                hotelSchedule.setHotelName(value);
                hotelSchedule.setTitle(value);
            }
        }
        Iterator hotelData = cardBase.getData().entrySet().iterator();
        while (hotelData.hasNext()) {
            Map.Entry entry = (Map.Entry) hotelData.next();
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if ("酒店名".equals(key)) {
                hotelSchedule.setHotelName(value);
                hotelSchedule.setTitle(value);
            } else if ("离店时间".equals(key) || "退房时间".equals(key)) {
                hotelSchedule.setCheckOutDate(value);
            } else if ("房型".equals(key) || ("类型").equals(key)) {
                hotelSchedule.setRoomStyle(value);
            } else if ("入住人".equals(key) || ("预定人").equals(key)) {
                hotelSchedule.setCheckInPeople(value);
            } else if ("房间数".equals(key)) {
                hotelSchedule.setRoomCounts(value);
            } else if ("酒店电话".equals(key)) {
                hotelSchedule.setServiceNum(value);
            } else if ("酒店地址".equals(key) || "地址".equals(key)) {
                hotelSchedule.setHotelAddress(value);
            }
        }
        Date hotelDate = null;
        long remindTime = 0;
        if (cardBase.getTimeStamp() != -1) {
            hotelDate = new Date(cardBase.getTimeStamp());
            hotelCalendar.setTime(hotelDate);
            hotelCalendar.set(Calendar.HOUR_OF_DAY, 10);
            hotelCalendar.set(Calendar.MINUTE, 0);
            hotelCalendar.set(Calendar.SECOND, 0);
            hotelCalendar.set(Calendar.MILLISECOND, 0);
            remindTime = hotelCalendar.getTimeInMillis();
        } else {
            Calendar remindCalendar = Calendar.getInstance();
            remindCalendar.setTimeInMillis(hotelCalendar.getTimeInMillis());
            remindCalendar.set(Calendar.HOUR_OF_DAY, 10);
            remindCalendar.set(Calendar.MINUTE, 0);
            remindCalendar.set(Calendar.SECOND, 0);
            remindCalendar.set(Calendar.MILLISECOND, 0);
            remindTime = remindCalendar.getTimeInMillis();
            hotelDate = new Date(hotelCalendar.getTimeInMillis());
        }
        hotelSchedule.setDate(hotelDate);
        hotelSchedule.setAllDay(true);
        hotelSchedule.setType(Constants.HOTEL_TYPE);
        hotelSchedule.setRemindDate(remindTime);
        hotelSchedule.setSmsSender(smsSender);
        hotelSchedule.setSmsContent(smsContent);
        hotelSchedule.setPeriodID(-1);
        hotelSchedule.setRemindPeriod("一次");
        ScheduleInfoDao.getInstance(this).saveScheduleToDB(hotelSchedule);
        RemindUtils.startScheduleRemind(this, hotelSchedule, Constants.GENERAL_REMIND);
    }

    private void saveMovieSchedule(CardBase cardBase, String smsSender, String smsContent) {
        LogUtils.e(TAG, "TYPE_MOVIE");
        MovieSchedule movieSchedule = new MovieSchedule();
        StringBuilder sb = new StringBuilder();
        Iterator movieData = cardBase.getData().entrySet().iterator();
        Iterator subTitle = cardBase.getSubTitle().entrySet().iterator();
        while (subTitle.hasNext()) {
            Map.Entry entry = (Map.Entry) subTitle.next();
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if ("取票凭证".equals(key)) {
                movieSchedule.setTicketCertificate(value);
            }
        }
        if (!cardBase.getData().containsKey("电影院") && !cardBase.getData().containsKey("影院地址")) {
            movieSchedule.setCinemaName("电影院");
        }
        if (!cardBase.getData().containsKey("电影名")) {
            movieSchedule.setMovieName("电影");
            movieSchedule.setTitle("电影");
        }
        while (movieData.hasNext()) {
            Map.Entry entry = (Map.Entry) movieData.next();
            String key = (String) entry.getKey();
            String val = (String) entry.getValue();
            if ("电影名".equals(key)) {
                movieSchedule.setMovieName(val);
                movieSchedule.setTitle(val);
            } else if ("电影院".equals(key) || "影院地址".equals(key)) {
                movieSchedule.setCinemaName(val);
            } else if ("影厅".equals(key)) {
                sb.append(val);
            } else if ("座位号".equals(key)) {
                sb.append(" " + val);
            } else if ("取票凭证".equals(key) || "取票号".equals(key) || "取票码".equals(key)) {
                movieSchedule.setTicketCertificate(val);
            } else if ("观影时间".equals(key)) {
                movieSchedule.setPlayTime(val);
            }
        }
        movieSchedule.setSource(TextUtilTools.getMessageSign(smsContent));
        movieSchedule.setSeatDesc(sb.toString());
        movieSchedule.setDate(new Date(cardBase.getTimeStamp()));
        movieSchedule.setAllDay(false);
        movieSchedule.setType(Constants.MOVIE_TYPE);
        movieSchedule.setRemindDate(cardBase.getTimeStamp());
        movieSchedule.setSmsSender(smsSender);
        movieSchedule.setSmsContent(smsContent);
        movieSchedule.setPeriodID(-1);
        movieSchedule.setRemindPeriod("一次");
        ScheduleInfoDao.getInstance(this).saveScheduleToDB(movieSchedule);
        RemindUtils.startScheduleRemind(this, movieSchedule, Constants.SMART_REMIND);
    }

    private void saveBankSchedule(CardBase cardBase, String smsSender, String smsContent) {
        LogUtils.e(TAG, "TYPE_BANK");
        BankSchedule bankSchedule = new BankSchedule();
        bankSchedule.setBankName(TextUtilTools.getMessageSign(smsContent));
        Iterator bankTitleKey = cardBase.getTitle().keySet().iterator();
        while (bankTitleKey.hasNext()) {
            bankSchedule.setTitle((String) bankTitleKey.next());
        }
        Iterator iterbankSubTitle = cardBase.getSubTitle().entrySet().iterator();
        while (iterbankSubTitle.hasNext()) {
            Map.Entry entry = (Map.Entry) iterbankSubTitle.next();
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if ("应还金额".equals(key) || "剩余还款".equals(key) || "应还本息".equals(key)
                    || "未还金额".equals(key) || "仍需还款".equals(key) || "账单金额".equals(key)
                    || "本期账单".equals(key)) {
                bankSchedule.setRepaymentAmount(value);
            }
        }
        Iterator bankData = cardBase.getData().entrySet().iterator();
        /*modify by zhengjl at 2017-2-4 for GNSPR #66025*/
        SimpleDateFormat format = new SimpleDateFormat("MM/dd");
        while (bankData.hasNext()) {
            Map.Entry entry = (Map.Entry) bankData.next();
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if ("账号".equals(key) || "还款账号".equals(key)) {
                bankSchedule.setCardNum(value);
            } else if ("账单月份".equals(key)) {
                bankSchedule.setBillMonth(value);
            } else if ("到期还款日".equals(key) || "贷款还款日".equals(key)) {
                String ri = "";
                if (value.contains("/")) {
                    String[] d = value.split("/");
                    if (d.length > 2) {
                        ri = d[0] + "年" + d[1] + "月" + d[2] + "日";
                    }
                    if (d.length > 0 && d.length < 3 && d[0].length() < 4) {
                        ri = d[0] + "月" + d[1] + "日";
                    }
                    /*modify by zhengjl at 2017-2-4 for GNSPR #66025*/
                    bankSchedule.setRepaymentMonth(format.format(cardBase.getTimeStamp(ri)));
                } else if (value.contains("月") && value.contains("日")) {
                    bankSchedule.setRepaymentMonth(format.format(cardBase.getTimeStamp(value)));
                } else {
                    LogUtils.i("luorw", "到期还款日,cardBase.getTimeStamp() = " + cardBase.getTimeStamp() + " , value = " + value);
                    bankSchedule.setRepaymentMonth(format.format(cardBase.getTimeStamp()));
                }
            } else if ("提示".equals(key)) {
                bankSchedule.setAlertDesc(value);
            } else if ("还款时间".equals(key) || "截止时间".equals(key)) {
                String repay = bankSchedule.getRepaymentMonth();
                if (TextUtils.isEmpty(repay) || repay.trim().contains("null")) {
                    String ri = "";
                    if (value.contains("/")) {
                        String[] d = value.split("/");
                        if (d.length > 2) {
                            ri = d[0] + "年" + d[1] + "月" + d[2] + "日";
                        }
                        if (d.length > 0 && d.length < 3 && d[0].length() < 4) {
                            ri = d[0] + "月" + d[1] + "日";
                        }
                        bankSchedule.setRepaymentMonth(format.format(cardBase.getTimeStamp(ri)));
                    } else if (value.contains("月") && value.contains("日")) {
                        bankSchedule.setRepaymentMonth(format.format(cardBase.getTimeStamp(value)));
                    } else {
                        LogUtils.i("luorw", "还款时间,cardBase.getTimeStamp() = " + cardBase.getTimeStamp() + " , value = " + value);
                        bankSchedule.setRepaymentMonth(format.format(cardBase.getTimeStamp()));
                        /*modify by zhengjl at 2017-2-4 for GNSPR #66025 not end*/
                    }
                }
            } else if ("每期本金".equals(key)) {
                bankSchedule.setRepaymentAmount(value);
            }
        }
        //modified by lurow for GNSPR #60238 20170210 begin
        //解析生成的卡片属于全天事件，事件创建时间是还款日当天0点，事件提醒时间是当天10点
        if (TextUtils.isEmpty(bankSchedule.getRepaymentMonth())) {
            LogUtils.i("luorw", "非信用卡或者贷款账单，不需要生成卡片");
            return;
        }
        String repayment = bankSchedule.getRepaymentMonth();
        Calendar calendar = Calendar.getInstance();
        Date date = formatRepaymentDate(repayment, calendar.get(Calendar.YEAR));
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 10);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        bankSchedule.setRemindDate(calendar.getTimeInMillis());
        bankSchedule.setDate(date);
        LogUtils.i("luorw", "银行,RemindDate = " + new Date(bankSchedule.getRemindDate()) + " , Date = " + bankSchedule.getDate() + " ,还款时间 = " + bankSchedule.getRepaymentMonth());
        //modified by lurow for GNSPR #60238 20170210 end
        bankSchedule.setAllDay(true);
        bankSchedule.setType(Constants.BANK_TYPE);
        bankSchedule.setSmsSender(smsSender);
        bankSchedule.setSmsContent(smsContent);
        bankSchedule.setSource(TextUtilTools.getMessageSign(smsContent));
        bankSchedule.setPeriodID(-1);
        bankSchedule.setRemindPeriod("一次");
        ScheduleInfoDao.getInstance(this).saveScheduleToDB(bankSchedule);
        RemindUtils.startScheduleRemind(this, bankSchedule, Constants.GENERAL_REMIND);
    }

    private Date formatRepaymentDate(String str, int year) {
        str = year + "/" + str;
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date date = null;
        try {
            date = format.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }


    private void saveTrainSchedule(CardBase cardBase, String smsSender, String smsContent) {
        LogUtils.e(TAG, "TYPE_TRAIN");
        TrainSchedule trainSchedule = new TrainSchedule();
        Iterator iterSubTitle = cardBase.getSubTitle().entrySet()
                .iterator();
        while (iterSubTitle.hasNext()) {
            Map.Entry entry = (Map.Entry) iterSubTitle.next();
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if ("出发时间".equals(key) || "发车时间".equals(key)) {
                trainSchedule.setStarttime(value);
            } else if ("到达时间".equals(key)) {
                trainSchedule.setArrivaltime(value);
            } else if ("订单号".equals(key) || "取票号".equals(key)) {
                trainSchedule.setOrdernumber(value);
            }
        }
        Iterator iterData = cardBase.getData().entrySet().iterator();
        while (iterData.hasNext()) {
            Map.Entry entry = (Map.Entry) iterData.next();
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if ("出发地".equals(key)) {
                trainSchedule.setDeparture(value);
            } else if ("目的地".equals(key) || "到达地".equals(key)) {
                trainSchedule.setDestination(value);
                if (value == null || "".equals(value)) {
                    trainSchedule.setDestination("目的地");
                }
            } else if ("车次".equals(key)) {
                if (value != null && !TextUtils.isEmpty(value) && value.contains("次")) {
                    String val_sec = value.replace("次", "");
                    trainSchedule.setTrainnumber(val_sec);
                } else {
                    trainSchedule.setTrainnumber(value);
                }

            } else if ("座位号".equals(key) || "座位".equals(key)) {
                trainSchedule.setSeatnumber(value);
            } else if ("订单号".equals(key) || "取票号".equals(key)) {
                trainSchedule.setOrdernumber(value);
            } else if ("订票人".equals(key) || "购票人".equals(key) || "乘客".equals(key)) {
                trainSchedule.setOrderperson(value);
            } else if ("出发时间".equals(key) || "发车时间".equals(key)) {
                trainSchedule.setStarttime(value);
            } else if ("到达时间".equals(key)) {
                trainSchedule.setArrivaltime(value);
            }
        }
        boolean isDestinationNULL = CardDetailsUtils.isEmptyValue(trainSchedule.getDestination());
        boolean isDepartureNULL = CardDetailsUtils.isEmptyValue(trainSchedule.getDeparture());
        if (isDepartureNULL && isDestinationNULL) {
            trainSchedule.setTitle("出发地-目的地");
        } else if (isDepartureNULL && !isDestinationNULL) {
            trainSchedule.setTitle("出发地-" + trainSchedule.getDestination());
        } else if (!isDepartureNULL && isDestinationNULL) {
            trainSchedule.setTitle(trainSchedule.getDeparture() + "-目的地");
        } else {
            trainSchedule.setTitle(trainSchedule.getDeparture() + "-"
                    + trainSchedule.getDestination());
        }
        trainSchedule.setType(Constants.TRAIN_TYPE);
        trainSchedule.setAllDay(false);
        trainSchedule.setDate(new Date(cardBase.getTimeStamp()));
        trainSchedule.setSmsSender(smsSender);
        trainSchedule.setSmsContent(smsContent);
        trainSchedule.setRemindDate(cardBase.getTimeStamp());
        trainSchedule.setPeriodID(-1);
        trainSchedule.setRemindPeriod("一次");
        ScheduleInfoDao.getInstance(this).saveScheduleToDB(trainSchedule);
        RemindUtils.startScheduleRemind(this, trainSchedule, Constants.SMART_REMIND);
    }

    private void saveFlightSchedule(CardBase cardBase, String smsSender, String smsContent) {
        LogUtils.e(TAG, "TYPE_FLIGHT");
        FlightSchedule flightSchedule = new FlightSchedule();
        //#40281 start
        Iterator iterSubTitleHeight = cardBase.getSubTitle().entrySet()
                .iterator();
        while (iterSubTitleHeight.hasNext()) {
            Map.Entry entry = (Map.Entry) iterSubTitleHeight.next();
            String key = (String) entry.getKey();
            String val = (String) entry.getValue();
            if ("航班".equals(key)) {
                flightSchedule.setFlightNum(val);
            }
        }
        //#40281 end
        Iterator flightData = cardBase.getData().entrySet().iterator();
        if (!cardBase.getData().containsKey("航班") && !cardBase.getData().containsKey("第一段航班")) {
            flightSchedule.setFlightNum(" ");
        }
        while (flightData.hasNext()) {
            Map.Entry entry = (Map.Entry) flightData.next();
            String key = (String) entry.getKey();
            String val = (String) entry.getValue();
            if ("到达时间".equals(key)) {
                flightSchedule.setArrivalTime(val);
            } else if ("出发地".equals(key) || "第一段出发地".equals(key)) {
                flightSchedule.setStartAddress(val);
            } else if ("到达地".equals(key) || "第一段到达地".equals(key)) {
                flightSchedule.setDestination(val);
            } else if ("航班".equals(key) || "第一段航班".equals(key)) {
                flightSchedule.setFlightNum(val);
            } else if ("乘机人".equals(key) || "乘客".equals(key)) {
                flightSchedule.setPassenger(val);
            } else if ("票号".equals(key)) {
                flightSchedule.setTicketNum(val);
            } else if ("机票来源".equals(key)) {
                flightSchedule.setAirlineSource(val);
            } else if ("客服电话".equals(key)) {
                flightSchedule.setServiceNum(val);
            } else if ("提示".equals(key)) {
                flightSchedule.setAlertDesc(val);
            }
        }
        boolean isFlightDestinationNULL = CardDetailsUtils.isEmptyValue(flightSchedule.getDestination());
        boolean isFlightDepartureNULL = CardDetailsUtils.isEmptyValue(flightSchedule.getStartAddress());
        if (isFlightDepartureNULL && isFlightDestinationNULL) {
            flightSchedule.setTitle("出发地-目的地");
        } else if (isFlightDepartureNULL && !isFlightDestinationNULL) {
            flightSchedule.setTitle("出发地-" + flightSchedule.getDestination());
        } else if (!isFlightDepartureNULL && isFlightDestinationNULL) {
            flightSchedule.setTitle(flightSchedule.getStartAddress() + "-目的地");
        } else {
            flightSchedule.setTitle(flightSchedule.getStartAddress() + "-"
                    + flightSchedule.getDestination());
        }
        flightSchedule.setDate(new Date(cardBase.getTimeStamp()));
        flightSchedule.setAllDay(false);
        flightSchedule.setType(Constants.FLIGHT_TYPE);
        flightSchedule.setRemindDate(cardBase.getTimeStamp());
        flightSchedule.setSmsSender(smsSender);
        flightSchedule.setSmsContent(smsContent);
        flightSchedule.setPeriodID(-1);
        flightSchedule.setRemindPeriod("一次");
        ScheduleInfoDao.getInstance(this).saveScheduleToDB(flightSchedule);
        RemindUtils.startScheduleRemind(this, flightSchedule, Constants.SMART_REMIND);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
