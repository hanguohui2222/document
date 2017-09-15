package com.gionee.hotspottransmission.constants;

/**
 * Created by luorw on 17/5/9.
 */
public class Constants {
    //接收文件的5个分类目录
    public static final String DIR_MUSIC = "audios";
    public static final String DIR_VIDEO = "videos";
    public static final String DIR_IMAGE = "pictures";
    public static final String DIR_APP = "apps";
    public static final String DIR_FILE = "files";
    //应用权限提示框sp key
    public static final String PERMISSION_TIP = "permission_tip";
    public static final String SHOW_NET_TIP = "show_net_tip";
    //关于设备重命名
    public static final String DEVICE_NAME = "device_name";
    public static final String SP_DEVICE_NAME = "sp_device_name";
    //热点状态改变的action
    public static final String ACTION_HOTSPOT_STATE_CHANGED = "android.net.wifi.WIFI_AP_STATE_CHANGED";
    //热点名称前缀
    public static final String WIFI_HOT_SPOT_SSID_PREFIX = "GN_KuaiChuan";
    public static final String WIFI_HOT_SPOT_GROUP_TRANSFER_PREFIX = "GN_GroupTransfer";
    // UDP通信服务端默认端口号
    public static final int CONNECT_SUCCESS_PORT = 9204;//通知连接成功的端口
    public static final int SEND_FILES_PORT = 8989;//发送文件端口
    public static final int RECEIVE_FILES_PORT = 8989;//接收文件端口
    public static final int SEND_COMMAND_PORT = 9090;//发送命令端口
    public static final int RECEIVE_COMMAND_PORT = 9090;//接收命令端口
    public static final int BROADCAST_PORT = 30000;//多播端口
    public static final String BROADCAST_IP = "230.0.1.1";//多播ip
    public static final int DATA_LEN = 4096; //定义每个数据报的最大大小为4K

    public static final String HOST_IP = "host_ip";
    public static final String CLIENT_IP = "client_ip";

    public static final String MSG_RESPONSE = "response";
    public static final String MSG_ONLINE = "online";
    public static final String MSG_OFFLINE = "offline";
    public static final String MSG_WORK = "work";
    // 最大尝试次数
    public static final int DEFAULT_TRY_COUNT = 10;
    //WiFi连接成功时未分配的默认IP地址
    public static final String DEFAULT_UNKNOW_IP = "0.0.0.0";
    //收到文件的五大类 begin
    public static final int TYPE_MUSIC = 0;
    public static final int TYPE_VIDEO = 1;
    public static final int TYPE_IMAGE = 2;
    public static final int TYPE_APPS = 3;//tab中应用页，无apk只是
    public static final int TYPE_FILE = 4;

    public static final int TYPE_DOCUMENT = 5;
    public static final int TYPE_COMPRESS = 6;
    public static final int TYPE_EBOOK = 7;
    public static final int TYPE_APK = 8;
    //收到文件的五大类 end
    public static final String VOLUME_NAME = "external";
    public static final int REFRESH_IMAGE_DRI_GV = 4;
    public static final int SET_IMAGE_BITMAP = 7;

    public static final int IMAGE_SECOND_DIR = 1;
    public static final int IMAGE_FIRST_DIR = 0;

    public static final String PREFERENCE_DEVICE_INFO = "DEVICE_INFO";
    public static final String DEVICE_NAME_KEY = "deviceName";
    public static final String DEVICE_ADDRESS_KEY = "deviceAddress";
    public static final String DEVICE_IP_KEY = "deviceIp";
    public static final String HOST_IP_KEY = "hostIp";
    public static final String CONNECTED_DEVICE_NAME_KEY = "connectedDeviceName";
    public static final String CONNECTED_DEVICE_ADDRESS_KEY = "connectedDeviceAddress";
    public static final String ACTION_GROUP_TRANSFER = "com.gionee.hotspottransmission.action.GROUP_TRANSFER";
    public static final String ACTION_SEND_FROM_SCAN = "com.gionee.hotspottransmission.action.SEND_FROM_SCAN";
    public static final String ACTION_SELECT_FILE_FROM_TRANSFER = "com.gionee.hotspottransmission.action.SELECT_FILE_FROM_TRANSFER";
    public static final String ACTION_RESEND_FILES = "com.gionee.hotspottransmission.action.RESEND";
    public static final String ACTION_GC_SEND_FILES = "com.gionee.hotspottransmission.action.GC_SEND_FILES";
    public static final String ACTION_MULTI_SEND_FILES = "com.gionee.hotspottransmission.action.MULTI_SEND";
    public static final int DEFAULT_FILE_TRANSFER_STATE = -1;
    public static final int FILE_TRANSFER_START = 0;
    public static final int FILE_TRANSFERING = 1;
    public static final int FILE_TRANSFER_SUCCESS = 2;
    public static final int FILE_TRANSFER_FAILURE = 3;
    public static final int FILE_TRANSFER_CANCEL = 4;
    public static final int FILE_TRANSFER_ALL_COMPLETE = 5;
    public static final int FILE_TRANSFER_BREAK = 6;
    public static final int SEND_DESCRIBE = -1;
    public static final int SEND_FILE = 3;
    public static final int CANCEL_ALL_TAG = -3;
    public static final int SEND_ALL_COMPLETE = -6;
    public static final int RESEND_TAG = -2;
    public static final int CANCEL_BY_INDEX = 8;
    public static final int SEND_FAIL = -4;
    public static final String ACTION_SCAN_FROM_SELECT_FILES = "com.gionee.hotspottransmission.action.SCAN_FROM_SELECT_FILES";
    public static final String ACTION_ENTER_FROM_NOTIFICATION = "com.gionee.wlandirect.enter.transfer.notification";
    public static final String DATE_FORMAT = "yyyy/MM/dd";
    public static final String TIME_FORMAT = "HH:mm";
    public static final String IS_RESEND_ACTION = "isReSend";
    //added by luorw for GNSPR #41659 begin
    public static final String IS_CONNECTED = "is_connected";
    public static final String CONNECT_STATUS = "connect_status";
    //added by luorw for GNSPR #41659 end


    //added by luorw for GNNCR #51917 20161027 begin
    public static final String EVENT_ID = "WLAN_DIRECT_EVENT";
    public static final String SETTING_EVENT = "GN_CLICK_SETTING";
    public static final String HISTORY_EVENT = "GN_CLICK_HISTORY";
    public static final String INVITE_EVENT = "GN_CLICK_INVITE";
    public static final String RENAME_EVENT = "GN_CLICK_RENAME";
    public static final String RENAME_SUCCESS_EVENT = "GN_RENAME_SUCCESS";
    public static final String SEND_EVENT = "GN_CLICK_SEND";
    public static final String RECEIVE_EVENT = "GN_CLICK_RECEIVE";
    public static final String TRANSFER_LIST_EVENT = "GN_TRANSFER_LIST";
    public static final String TRANSFER_LIST_COUNT = "GN_TRANSFER_LIST_COUNT";
    public static final String TRANSFER_DOC_COUNT = "GN_TRANSFER_DOC_COUNT";
    public static final String TRANSFER_APPS_COUNT = "GN_TRANSFER_APPS_COUNT";
    public static final String TRANSFER_IMAGE_COUNT = "GN_TRANSFER_IMAGE_COUNT";
    public static final String TRANSFER_MUSIC_COUNT = "GN_TRANSFER_MUSIC_COUNT";
    public static final String TRANSFER_VIDEO_COUNT = "GN_TRANSFER_VIDEO_COUNT";
    //added by luorw for GNNCR #51917 20161027 end
    public static final int ON_SCAN_REPEAT = 0x91000;
    public static final int ON_RESCAN_ENABLE = 0x91001;
    public static final int ON_HOTSPOT_ENABLE = 0x91002;
    public static final int ON_CONNECTED = 0x91003;
    public static final int ON_DISCONNECTED = 0x91004;
    public static final int ON_SHOW_SCAN_RESULTS = 0x91005;
    public static final int ON_WIFI_DISABLE = 0x91006;
    public static final int FILE_CAN_NOT_SHARE = 0x91007;
    public static final int FILE_SEND_DATA_IS_READY = 0x91008;

    public static final int NOTIFY_RECEIVE_WORK = 0x3910;
    public static final int RECEIVER_READ_FILE_LIST_SUCCESS = 0x3911;
    public static final int RECEIVER_TRANSFER_COMPLETE_BY_INDEX = 0x3912;
    public static final int RECEIVER_UPDATE_TRANSFER_PROGRESS = 0x3913;
    public static final int RECEIVER_TRANSFER_ALL_COMPLETE = 0x3914;
    public static final int RECEIVER_TRANSFER_CANCEL_BY_INDEX = 0x3915;
    public static final int RECEIVER_TRANSFER_CANCEL_ALL = 0x3916;
    public static final int RECEIVER_TRANSFER_STORAGE_FULL = 0x3917;

    public static final int SENDER_SEND_FILE_LIST_SUCCESS = 0x3918;
    public static final int SENDER_TRANSFER_COMPLETE_BY_INDEX = 0x3919;
    public static final int SENDER_UPDATE_TRANSFER_PROGRESS = 0x3920;
    public static final int SENDER_TRANSFER_ALL_COMPLETE = 0x3921;
    public static final int SENDER_TRANSFER_CANCEL_BY_INDEX = 0x3922;
    public static final int SENDER_TRANSFER_CANCEL_ALL = 0x3923;
    public static final int READY_TO_EXIT = 0x3924;

    public static final int OP_SEND = 0x3925;
    public static final int OP_RECEIVE = 0x3926;

    public static final int SENDER_SEND_DESCRIBE = -1;
    public static final int SENDER_CANCEL_ALL = -2;
    public static final int SENDER_CANCEL_BY_INDEX = -3;
    public static final int RECEIVER_CANCEL_ALL = -4;
    public static final int RECEIVER_CANCEL_BY_INDEX = -5;
    public static final int RECEIVER_REQUEST_FILE = -6;
    public static final int RECEIVER_RECEIVE_OVER = -7;

    public static final String IS_GROUP_OWNER = "is_group_owner";
    public static final String CURRENT_TAB = "current_tab";
    public static final String SEND_GROUP_IMEI_LIST = "send_group_imei_list";
    public static final String RESEND_GROUP_IMEI_LIST = "resend_group_imei_list";

    public static final int MAX_PROCESS = 100;
}