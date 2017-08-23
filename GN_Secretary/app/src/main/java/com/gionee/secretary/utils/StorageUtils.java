package com.gionee.secretary.utils;

import android.os.StatFs;

/**
 * Created by hangh on 4/11/17.
 */
public class StorageUtils {
    public static long getAvailSpace(String path) {
        StatFs statfs = new StatFs(path);
        long size = statfs.getBlockSizeLong();//获取分区的大小
        long count = statfs.getAvailableBlocksLong();//获取可用分区块的个数
        return size * count;
    }
}
