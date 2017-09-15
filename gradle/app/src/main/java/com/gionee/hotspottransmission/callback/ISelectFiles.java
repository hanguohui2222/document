package com.gionee.hotspottransmission.callback;

import com.gionee.hotspottransmission.bean.FileInfo;
import java.util.List;

/**
 * Created by luorw on 4/27/16.
 */
public interface ISelectFiles {
    void showSelectedList();
    void send();
    void clearSelectedList();
    void clearSelectedItem(FileInfo info);
    void refreshMenu(List<FileInfo> list);
    void refreshSelectAllText();
}
