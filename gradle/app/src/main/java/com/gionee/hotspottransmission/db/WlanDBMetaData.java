package com.gionee.hotspottransmission.db;

import android.provider.BaseColumns;

/**
 * Created by zhuboqin on 30/04/16.
 */
public class WlanDBMetaData {
    public static final String DATABASE_NAME = "wlandirect_db";
    public static int DATABASE_VERSION = 1;

    public static final String T_HISTORY_TITLE = "tb_history_title";
    public static final String T_HISTORY_CONTENT = "tb_history_content";

    public static final class T_DEVICE_INFO_MeteData implements BaseColumns {
        public static final String TABLE_NAME = "t_device";
        public static final String DEVICENAME = "device_name";
        public static final String CONNECTTIME = "connection_last_time";
        public static final String ADDRESS = "device_address";
        public static final String STATUS = "device_status";
        public static final String IP = "device_ip";
    }

    public static final class T_FILE_INFO_MeteData implements BaseColumns{
        public static final String TABLE_NAME = "t_file_info";
        public static final String FILENAME = "name";
        public static final String FILETYPE = "type";
        public static final String FILESIZE = "size";
        public static final String FILEDATETIME = "date_time";
        public static final String FILESTATE = "state";
        public static final String FILEURI = "uri";
        public static final String FILEPATH = "path";
        public static final String HISTORY_TITLE_ID = "history_title_id";
    }

    public static final class T_HISTORY_TITLE_MeteData implements BaseColumns{
        public static final String TABLE_NAME = "t_history_title";
        public static final String TOTALSIZE = "total_size";
        public static final String FILECOUNT = "file_count";
        public static final String DATE = "date";
        public static final String DEVICE = "device";
        public static final String DEVICE_ADDRESS = "device_address";
        public static final String ISSENDER = "is_sender";
    }

}
