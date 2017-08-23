package com.gionee.secretary.constants;

/**
 * Created by yzzhao on 1/21/16.
 */
public class CardBaseType {
    /**
     * 打车
     */
    public static final int TYPE_TAXI = 0x01;
    /**
     * 点评
     */
    public static final int TYPE_DIANPING = 0x02;
    /**
     * 电影票
     */
    public static final int TYPE_MOVIE = 0x03;
    /**
     * 电子商务
     */
    public static final int TYPE_E_BUSINESS = 0x04;
    /**
     * 电子支付
     */
    public static final int TYPE_E_PAY = 0x05;
    /**
     * 火车票
     */
    public static final int TYPE_TRAIN = 0x06;
    /**
     * 机票
     */
    public static final int TYPE_FLIGHT = 0x07;
    /**
     * 景点
     */
    public static final int TYPE_VIEW_POINT = 0x08;
    /**
     * 酒店
     */
    public static final int TYPE_HOTEL = 0x09;
    /**
     * 快递
     */
    public static final int TYPE_CARRIER = 0x0A;
    /**
     * 美团
     */
    public static final int TYPE_MEITUAN = 0x0B;
    /**
     * 其它
     */
    public static final int TYPE_OTHERS = 0x0C;
    /**
     * 水电煤
     */
    public static final int TYPE_LIFE = 0x0D;
    /**
     * 验证码
     */
    public static final int TYPE_VERIFICATION = 0x0E;
    /**
     * 银行
     */
    public static final int TYPE_BANK = 0x0F;
    /**
     * 运营商
     */
    public static final int TYPE_OPERATOR = 0x10;

    /**
     * 代金券
     */
    public static final int SUB_TYPE_COUPON = 0x01;

    /**
     * 电影券
     */
    public static final int SUB_TYPE_MOVIE = 0x02;

    /**
     * 电子登机牌
     */
    public static final int SUB_TYPE_E_BOARD = 0x03;

    /**
     * 航班信息
     */
    public static final int SUB_TYPE_FLIGHT = 0x04;

    /**
     * 团购券
     */
    public static final int SUB_TYPE_GROUPON = 0x05;

    /**
     * 自取快递
     */
    public static final int SUB_TYPE_CARRIER = 0x06;


    public static class Dianping {

        /**
         * 点评//订单驳回通知
         */
        public static final int ORDER_REFUSED_NOTIFY = 0x17;

        /**
         * 点评//订单购买成功
         */
        public static final int ORDER_PURCHASED_SUCCESS = 0x1B;

        /**
         * 点评//订单退款成功
         */
        public static final int ORDER_REFUND_SUCCESS = 0x1E;

        /**
         * 点评//订单退款失败
         */
        public static final int ORDER_REFUND_FAILED = 0x1F;

        /**
         * 点评//订单消费通知
         */
        public static final int ORDER_CONSUMED_NOTIFY = 0x22;

        /**
         * 点评//订座提醒
         */
        public static final int ORDER_SEAT_REMINDER = 0x26;

        /**
         * 点评//过期提醒
         */
        public static final int ORDER_EXPIRED_REMINDER = 0x35;

        /**
         * 点评/代金券/发放代金券
         */
        public static final int ISSUE_COUPON = 0x27;

        /**
         * 点评/团购券/订单购买成功
         */
        public static final int ORDER_COUPON_PURCHASE_SUCCESS = 0x1B;
    }

    public static class Movie {

        /**
         * 电影票//电影票抵用券
         */
        public static final int TICKET_COUPON = 0x10;

        /**
         * 电影票//电影票订票成功
         */
        public static final int TICKET_BOOKED_SUCCESS = 0x11;

        /**
         * 电影票//电影票订票提醒
         */
        public static final int TICKET_BOOK_REMINDER = 0x12;

        /**
         * 电影票//电影票兑换券
         */
        public static final int TICKET_GIFT_CARD = 0x13;

        /**
         * 电影票//电影票观影提醒
         */
        public static final int TIME_REMINDER = 0x14;

        /**
         * 电影票//电影票预售券
         */
        public static final int TICKET_PRESALE_COUPON = 0x15;

        /**
         * 电影票//观影提醒
         */
        public static final int MOVIE_REMINDER = 0x34;

        /**
         * 电影票/代金券/发放代金券
         */
        public static final int ISSUE_COUPON = 0x27;
    }

    public class EBusiness {
        /**
         * 电子商务//订单处理通知
         */
        public static final int ORDER_PROCESS_NOTIFY = 0x18;

        /**
         * 电子商务//订单到期提醒
         */
        public static final int ORDER_EXPIRE_REMINDER = 0x19;

        /**
         * 电子商务//订单购买成功
         */
        public static final int ORDER_PURCHASE_SUCCESS = 0x1B;

        /**
         * 电子商务//订单取消通知
         */
        public static final int ORDER_CANCELLED_NOTIFY = 0x1D;

        /**
         * 电子商务//订单退款提醒
         */
        public static final int ORDER_REFUND_REMINDER = 0x20;

        /**
         * 电子商务//订单消费提醒
         */
        public static final int ORDER_CONSUMED_REMINDER = 0x21;

        /**
         * 电子商务//订单支付提醒
         */
        public static final int ORDER_PAY_REMINDER = 0x24;

        /**
         * 电子商务//积分提醒
         */
        public static final int ORDER_POINT_REMINDER = 0x4A;

        /**
         * 电子商务//汽车票订票失败
         */
        public static final int TICKED_ORDER_FAILED = 0x56;

        /**
         * 电子商务/代金券/发放代金券
         */
        public static final int ISSUE_COUPON = 0x27;

        /**
         * 电子商务/代金券/即将到期提醒
         */
        public static final int EXPIRE_REMINDER = 0x4C;
    }

    public static class EPay {
        /**
         * 电子支付//交易提醒
         */
        public static final int TRANSACTION_REMINDER = 0x4E;
    }

    public static class Train {
        /**
         * 火车票//火车票订票成功
         */
        public static final int TICKET_BOOKED_SUCCESS = 0x39;
        /**
         * 火车票//火车票订票失败
         */
        public static final int TICKET_BOOKED_FAIL = 0x3A;

        /**
         * 火车票//火车票订票提醒
         */
        public static final int TICKET_BOOK_REMINDER = 0x3B;

        /**
         * 火车票//火车票订票通知
         */
        public static final int TICKET_BOOK_NOTIFY = 0x3C;
        /**
         * 火车票//火车票购票成功
         */
        public static final int TICKET_BUY_SUCCESS = 0x3D;
        /**
         * 火车票//火车票退票通知
         */
        public static final int TICKET_RETURN_NOTIFY = 0x3E;
        /**
         * 火车票//火车票预订成功
         */
        public static final int TICKET_PREORDER_SUCCESS = 0x3F;
        /**
         * 火车票//火车票支付成功
         */
        public static final int TICKET_PAID_SUCCESS = 0x40;
        /**
         * 火车票//火车票支付提醒
         */
        public static final int TICKET_PAY_REMINDER = 0x41;
    }

    public static class Flight {
        /**
         * 机票//登机提醒
         */
        public static final int ONBOARD_REMINDER = 0x0A;
        /**
         * 机票//航班取消通知
         */
        public static final int FLIGHT_CANCELLED_NOTIFY = 0x36;
        /**
         * 机票//航班调整通知
         */
        public static final int FLIGHT_CHANGED_NOTIFY = 0x37;

        /**
         * 机票/航班信息/机票出票提醒
         */
        public static final int TICKET_CONFIRM_REMINDER = 0x42;

        /**
         * 机票//机票购买成功
         */
        public static final int TICKET_PURCHASED_SUCCESS = 0x43;
        /**
         * 机票//机票行程单提醒
         */
        public static final int TICKET_ITINERARY_REMINDER = 0x44;
        /**
         * 机票//机票申请退票通知
         */
        public static final int TICKET_APPLY_RETURN_NOTIFY = 0x45;
        /**
         * 机票//机票退票成功
         */
        public static final int TICKET_RETURN_SUCCESS = 0x46;

        /**
         * 机票/航班信息/机票预留提醒
         */
        public static final int TICKET_RESERVED_REMINDER = 0x47;

        /**
         * 机票//机票支付成功
         */
        public static final int TICKET_PAID_SUCCESS = 0x48;
        /**
         * 机票//机票支付提醒
         */
        public static final int TICKET_PAY_REMINDER = 0x49;
        /**
         * 机票//选座提醒
         */
        public static final int CHOOSE_SEAT_REMINDER = 0x74;
        /**
         * 机票//预留座位提醒
         */
        public static final int SEAT_RESERVED_REMINDER = 0x80;
        /**
         * 机票//值机成功通知
         */
        public static final int CHECK_IN_SUCCESS_NOTIFY = 0x87;
        /**
         * 机票/航班信息/值机提醒
         */
        public static final int CHECK_IN_SUCCESS = 0x88;
    }

    public static class Hotel {

        /**
         * 酒店//订单返现成功
         */
        public static final int ORDER_CASH_RETURN_SUCCESS = 0x1A;

        /**
         * 酒店//订单购买成功
         */
        public static final int ORDER_PURCHASE_SUCCESS = 0x1B;

        /**
         * 酒店//订单取消通知
         */
        public static final int ORDER_CANCELLED_NOTIFY = 0x1D;

        /**
         * 酒店//订单退款成功
         */
        public static final int ORDER_REFUND_SUCCESS = 0x1E;

        /**
         * 酒店//订单消费提醒
         */
        public static final int ORDER_CONSUME_REMINDER = 0x21;

        /**
         * 酒店//订单预约通知
         */
        public static final int ORDER_BOOK_NOTIFY = 0x23;
        /**
         * 酒店//订单支付提醒
         */
        public static final int ORDER_PAY_REMINDER = 0x24;
        /**
         * 酒店//酒店入住提醒
         */
        public static final int HOTEL_CHECK_IN_REMINDER = 0x50;
    }

    public final static class Carrier {

        /**
         * 快递//菜鸟驿站提货通知
         */
        public static final int PICK_UP_NOTIFY = 0x01;
        /**
         * 快递//订单追踪
         */
        public static final int CARRIER_TRACK = 0x25;
        /**
         * 快递//派件提醒
         */
        public static final int ASSIGN_REMINDER = 0x55;
        /**
         * 快递//签收提醒
         */
        public static final int DELIVERED_REMINDER = 0x57;
    }

    public final static class Meituan {

        /**
         * 美团//订单变更提醒
         */
        public static final int ORDER_CHANGED_REMINDER = 0x16;

        /**
         * 美团//订单购买成功
         */
        public static final int ORDER_PURCHASE_SUCCESS = 0x1B;

        /**
         * 美团//订单即将到期提醒
         */
        public static final int ORDER_EXPIRE_REMINDER = 0x1C;
        /**
         * 美团//订单退款成功
         */
        public static final int ORDER_REFUND_SUCCESS = 0x1E;

        /**
         * 美团//订单消费通知
         */
        public static final int ORDER_CONSUMED_NOTIFY = 0x22;

        /**
         * 美团//账户余额提醒
         */
        public static final int ACCOUNT_REMAINING_REMINDER = 0x86;

        /**
         * 美团/代金券/到期提醒
         */
        public static final int EXPIRE_REMINDER = 0x09;

        /**
         * 美团/代金券/发放代金券
         */
        public static final int ISSUE_COUPON = 0x27;

    }

    public final static class Life {

        /**
         * 水电煤//电费缴费成功
         */
        public static final int ELECTRIC_PAY_SUCCESS = 0x0B;

        /**
         * 水电煤//电费扣费失败
         */
        public static final int ELECTRIC_PAY_FAIL = 0x0C;

        /**
         * 水电煤//电费账单
         */
        public static final int ELECTRIC_BILL = 0x0D;

        /**
         * 水电煤//电量提醒
         */
        public static final int ELECTRIC_REMAINING_REMINDER = 0x0E;

        /**
         * 水电煤//电路故障通知
         */
        public static final int ELECTRIC_MALFUNCTION_NOTIFY = 0x0F;

        /**
         * 水电煤//燃气缴费成功
         */
        public static final int GAS_PAY_SUCCESS = 0x58;

        /**
         * 水电煤//燃气账单
         */
        public static final int GAS_BILL = 0x59;

        /**
         * 水电煤//水费缴费成功
         */
        public static final int WATER_PAY_SUCCESS = 0x5F;

        /**
         * 水电煤//水费账单
         */
        public static final int WATER_BILL = 0x60;

        /**
         * 水电煤//停电通知
         */
        public static final int ELECTRIC_POWER_CUT_NOTIFY = 0x64;

        /**
         * 水电煤//停水通知
         */
        public static final int WATER_CUT_OFF_NOTIFY = 0x65;

        /**
         * 水电煤//账单提醒
         */
        public static final int BILL_REMINDER = 0x84;
    }

    public static final class VerificationCode {
        /**
         * 验证码//服务开通提醒
         */
        public static final int VERIFICATION_SERVICE_START_REMINDER = 0x29;

        /**
         * 验证码//服务开通验证
         */
        public static final int SERVICE_START_VERIFY = 0x2A;

        /**
         * 验证码//积分消费提醒
         */
        public static final int POINT_CONSUME_REMINDER = 0x4B;

        /**
         * 验证码//交易提醒
         */
        public static final int TRANSACTION_REMINDER = 0x4E;

        /**
         * 验证码//交易验证提醒
         */
        public static final int TRANSACTION_VERIFY_REMINDER = 0x4F;

        /**
         * 验证码//验证码
         */
        public static final int VERIFICATION_CODE = 0x75;

        /**
         * 验证码//验证码提醒
         */
        public static final int VERIFICATION_CODE_REMINDER = 0x76;

        /**
         * 验证码//业务办理验证
         */
        public static final int BUSINESS_START_VERIFY = 0x77;
    }

    /**
     * 银行//
     */
    public static final class Bank {

        /**
         * 银行//贷款发放成功
         */
        public static final int LOAN_DELIVER_SUCCESS = 0x04;

        /**
         * 银行//贷款还款成功
         */
        public static final int LOAN_REPAY_SUCCESS = 0x05;

        /**
         * 银行//贷款余额提醒
         */
        public static final int LOAN_REMAINING_REMINDER = 0x06;

        /**
         * 银行//贷款逾期提醒
         */
        public static final int LOAN_OVERDUE_REMINDER = 0x07;

        /**
         * 银行//贷款账单提醒
         */
        public static final int LOAN_BILL_REMINDER = 0x08;

        /**
         * 银行//服务到期提醒
         */
        public static final int SERVICE_EXPIRE_REMINDER = 0x28;

        /**
         * 银行//服务开通提醒
         */
        public static final int BANK_SERVICE_START_REMINDER = 0x29;

        /**
         * 银行//服务开通验证
         */
        public static final int SERVICE_START_VERIFY = 0x2A;

        /**
         * 银行//服务提醒
         */
        public static final int SERVICE_REMINDER = 0x2B;

        /**
         * 银行//服务停用通知
         */
        public static final int SERVICE_SUSPEND_NOTIFY = 0x2C;

        /**
         * 银行//付款成功提醒
         */
        public static final int PAY_SUCCESS_NOTIFY = 0x2D;

        /**
         * 银行//付款失败提醒
         */
        public static final int PAY_FAIL_NOTIFY = 0x2E;

        /**
         * 银行//公积金贷款还款成功
         */
        public static final int HOUSE_FUND_REPAY_SUCCESS = 0x2F;

        /**
         * 银行//公积金基数调整通知
         */
        public static final int HOUSE_FUND_RANK_CHANGED_NOTIFY = 0x30;

        /**
         * 银行//公积金缴存提醒
         */
        public static final int HOUSE_FUND_DEPOSIT_REMINDER = 0x31;

        /**
         * 银行//公积金提取提醒
         */
        public static final int HOUSE_FUND_WITHDRAW = 0x32;

        /**
         * 银行//公积金账户结息提醒
         */
        public static final int HOUSE_FUND_PROCESS_INTEREST_REMINDER = 0x33;

        /**
         * 银行//积分提醒
         */
        public static final int POINT_REMINDER = 0x4A;

        /**
         * 银行//积分消费提醒
         */
        public static final int POINT_CONSUME_REMINDER = 0x4B;
        /**
         * 银行//交易失败提醒
         */
        public static final int TRANSACTION_FAIL_REMINDER = 0x4D;

        /**
         * 银行//交易提醒
         */
        public static final int TRANSACTION_REMINDER = 0x4E;

        /**
         * 银行//交易验证提醒
         */
        public static final int TRANSACTION_VERIFY_REMINDER = 0x4F;

        /**
         * 银行//扣费提醒
         */
        public static final int CHARGE_REMINDER = 0x51;

        /**
         * 银行//手机银行绑定提醒
         */
        public static final int MOBILE_BIND_REMINDER = 0x5B;

        /**
         * 银行//手机银行签约提醒
         */
        public static final int MOBILE_SIGNED_REMINDER = 0x5C;

        /**
         * 银行//刷卡服务提醒
         */
        public static final int CREDIT_CARD_SERVICE_REMINDER = 0x5D;

        /**
         * 银行//刷卡奖励提醒
         */
        public static final int CREDIT_CARD_REWARD_REMINDER = 0x5E;

        /**
         * 银行//网银登录提醒
         */
        public static final int EBANK_LOGIN_REMINDER = 0x66;

        /**
         * 银行//信用卡额度申请失败
         */
        public static final int APPLY_CREDITS_FAILED = 0x67;

        /**
         * 银行//信用卡额度提醒
         */
        public static final int CREDIT_REMINDER = 0x68;

        /**
         * 银行//信用卡额度调整
         */
        public static final int CREDIT_CHANGED = 0x69;

        /**
         * 银行//信用卡还款成功
         */
        public static final int CREDIT_CARD_REPAY_SUCCESS = 0x6A;

        /**
         * 银行//信用卡还款失败
         */
        public static final int CREDIT_CARD_REPAY_FAIL = 0x6B;

        /**
         * 银行//信用卡激活提醒
         */
        public static final int CREDIT_CARD_OPENED_REMINDER = 0x6C;

        /**
         * 银行//信用卡即将到期提醒
         */
        public static final int CREDIT_CARD_EXPIRE_REMINDER = 0x6D;

        /**
         * 银行//信用卡启用提醒
         */
        public static final int CREDIT_CARD_START_REMINDER = 0x6E;

        /**
         * 银行//信用卡申请失败
         */
        public static final int CREDIT_CARD_APPLY_FAIL = 0x6F;

        /**
         * 银行//信用卡停用提醒
         */
        public static final int CREDIT_CARD_SUSPEND_REMINDER = 0x70;

        /**
         * 银行//信用卡退款成功
         */
        public static final int CREDIT_CARD_REFUND_SUCCESS = 0x71;

        /**
         * 银行//信用卡消费提醒
         */
        public static final int CREDIT_CARD_CONSUME_REMINDER = 0x72;

        /**
         * 银行//信用卡账单提醒
         */
        public static final int CREDIT_CARD_BILL_REMINDER = 0x73;


        /**
         * 银行//业务取消提醒
         */
        public static final int SERVICE_CANCELLED_REMINDER = 0x7A;

        /**
         * 银行//业务申购提醒
         */
        public static final int SERVICE_APPLY_REMINDER = 0x7B;

        /**
         * 银行//业务暂停通知
         */
        public static final int CREDIT_CARD_SUSPEND_NOTIFY = 0x7C;

        /**
         * 银行//银行卡挂失提醒
         */
        public static final int REPORT_LOSS_REMINDER = 0x7D;

        /**
         * 银行//银行卡开通提醒
         */
        public static final int DEPOSIT_CARD_OPEN_REMINDER = 0x7E;

        /**
         * 银行//余额提醒
         */
        public static final int REMAINING_REMINDER = 0x7F;

        /**
         * 银行//预约叫号提醒
         */
        public static final int RESERVE_REMINDER = 0x81;

        /**
         * 银行//账单分期提醒
         */
        public static final int BILL_STAGING_REMINDER = 0x83;

        /**
         * 银行//账单提醒
         */
        public static final int BILL_REMINDER = 0x84;

        /**
         * 银行//账单逾期提醒
         */
        public static final int BILL_OVERDUE_REMINDER = 0x85;

        /**
         * 银行//业务开通提醒
         */
        public static final int OPERATOR_SERVICE_START_REMINDER = 0x79;


    }

    /**
     * 运营商//
     */
    public static final class Operator {

        /**
         * 运营商//充值成功
         */
        public static final int RECHARGE_SUCCESS = 0x02;

        /**
         * 运营商//充值提醒
         */
        public static final int RECHARGE_REMINDER = 0x03;

        /**
         * 运营商//会员服务开通
         */
        public static final int MEMBERSHIP_START = 0x38;

        /**
         * 运营商//积分提醒
         */
        public static final int POINT_REMINDER = 0x4A;

        /**
         * 运营商//积分消费提醒
         */
        public static final int POINT_CONSUME_REMINDER = 0x4B;

        /**
         * 运营商//扣费提醒
         */
        public static final int CHARGE_REMINDER = 0x51;

        /**
         * 运营商//来去电提醒
         */
        public static final int IN_OUT_CALL_REMINDER = 0x52;

        /**
         * 运营商//流量提醒
         */
        public static final int DATA_TRAFFIC_REMINDER = 0x53;

        /**
         * 运营商//漫游提醒
         */
        public static final int ROAMING_REMINDER = 0x54;

        /**
         * 运营商//入网提醒
         */
        public static final int REGISTER_REMINDER = 0x5A;

        /**
         * 运营商//套餐办理提醒
         */
        public static final int PACKAGE_OPEN_REMINDER = 0x61;

        /**
         * 运营商//套餐取消提醒
         */
        public static final int PACKAGE_CANCEL_REMINDER = 0x62;

        /**
         * 运营商//套餐消费提醒
         */
        public static final int PACKAGE_CONSUME_REMINDER = 0x63;

        /**
         * 运营商//业务变更提醒
         */
        public static final int SERVICE_CHANGE_REMINDER = 0x78;

        /**
         * 运营商//业务开通提醒
         */
        public static final int OPERATOR_SERVICE_START_REMINDER = 0x79;

        /**
         * 运营商//业务取消提醒
         */
        public static final int SERVICE_CANCEL_REMINDER = 0x7A;

        /**
         * 运营商//余额提醒
         */
        public static final int REMAINING_REMINDER = 0x7F;

        /**
         * 运营商//赠费提醒
         */
        public static final int REWARD_REMINDER = 0x82;

        /**
         * 运营商//账单提醒
         */
        public static final int BILL_REMINDER = 0x84;
    }
}
