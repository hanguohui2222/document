package com.gionee.hotspottransmission.callback;

import com.gionee.hotspottransmission.bean.FileInfo;
import java.util.List;

/**
 * Created by luorw on 4/27/16.
 */
public interface IClearFileSelectedCallBack {
    void clearSelectedFiles();
    void refreshUI(List<FileInfo> list);
    void refreshSelectAllUI();
}
