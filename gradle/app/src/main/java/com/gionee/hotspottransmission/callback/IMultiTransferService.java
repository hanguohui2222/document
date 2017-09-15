package com.gionee.hotspottransmission.callback;

import com.gionee.hotspottransmission.bean.MultiCommandInfo;

import java.net.Socket;

/**
 * Created by luorw on 5/26/17.
 */

public interface IMultiTransferService {

    void createReadCommand(String key);

    void closeAllCommandSocket();

    void createWriteCommand(String key, MultiCommandInfo info);

    void notifyReceiveWork(String key);

    Socket getReceiveFileSocket(String ip);

    void notifySendWork(String key);

    void notifyOffline();

    boolean isGroupOwner();
}
