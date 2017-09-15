package com.gionee.hotspottransmission.callback;

import com.gionee.hotspottransmission.bean.ResponseInfo;

import java.net.Socket;

/**
 * Created by luorw on 5/26/17.
 */

public interface ITransferService {
    void notifyConnectSuccess();
    void createReceiveCommand();
    void receiverWriteCommand(int command);
    Socket getReceiveCommandSocket();
    void notifyReceiveWork();
    Socket getReceiveFileSocket();
    void createSendCommand();
    void senderWriteCommand(int command);
    Socket getSendCommandSocket();
    void notifySendWork();
    boolean isGroupOwner();
}
