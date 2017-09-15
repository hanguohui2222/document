package com.gionee.hotspottransmission.history.biz;

import android.content.Context;

import com.gionee.hotspottransmission.bean.FileInfo;
import com.gionee.hotspottransmission.history.dao.ReceivedFileDao;

import java.util.List;
import java.util.Map;

/**
 * Created by zhuboqin on 5/05/16.
 */
public class ReceivedFileBiz {

    private ReceivedFileDao dao;

    public ReceivedFileBiz(Context context){
        dao = new ReceivedFileDao(context);
    }

    public Map<String,List<FileInfo>> findFileByType(int type){
        return dao.findFileByType(type);
    }

}
