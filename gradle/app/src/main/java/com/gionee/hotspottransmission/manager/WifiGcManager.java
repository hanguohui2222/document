package com.gionee.hotspottransmission.manager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.utils.FileUtil;
import com.gionee.hotspottransmission.utils.LogUtil;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class WifiGcManager {
    //过滤免密码连接的WiFi
    public static final String NO_PASSWORD = "[ESS]";
    public static final String NO_PASSWORD_WPS = "[WPS][ESS]";

	private Context mContext;
	private WifiManager mWifiManager;
	
	public WifiGcManager(Context context) {
		mContext = context.getApplicationContext();
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	}
	
	/**
     * 打开Wi-Fi
     */
    public void openWifi() {
        if (!isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
    }

    /**
     * 关闭Wi-Fi
     */
    public void closeWifi() {
        if (isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        }
    }
    
    /**
     * 当前WiFi是否开启
     */
    public boolean isWifiEnabled() {
    	return mWifiManager.isWifiEnabled();
    }
    
    /**
     * 清除指定网络
     * @param SSID
     */
    public void clearWifiInfo(String SSID) {
    	WifiConfiguration tempConfig = isExsits(SSID);
        if (tempConfig != null) {
            mWifiManager.removeNetwork(tempConfig.networkId);
        }
    }
    
    /**
     * 判断当前网络是否WiFi
     * @param context
     * @return
     */
    public boolean isWifi(final Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.getType() == 1;
    }
    
    /**
     * 扫描周围可用WiFi
     * @return
     */
    public boolean startScan() {
        if(isWifiEnabled()) {
            return mWifiManager.startScan();
        }
        return false;
    }
    
    /**
     * 获取周围可用WiFi扫描结果
     * @return
     */
    public List<ScanResult> getScanResults() {
        List<ScanResult> scanResults = mWifiManager.getScanResults();
        if(scanResults != null && scanResults.size() > 0) {
            return filterScanResult(scanResults);
        } else {
            return new ArrayList<>();
        }
    }
	
    /**
     * 获取周围信号强度大于-80的WiFi列表（Wifi强度为负数，值越大信号越好）
     * @return
     * @throws InterruptedException
     */
	public List<ScanResult> getWifiScanList() throws InterruptedException {
		List<ScanResult> resList = new ArrayList<ScanResult>();
		if(mWifiManager.startScan()) {
			List<ScanResult> tmpList = mWifiManager.getScanResults();
			Thread.sleep(2000);
			if(tmpList != null && tmpList.size() > 0) {
//				resList = sortByLevel(tmpList);
				for(ScanResult scanResult : tmpList) {
					if(scanResult.level > -80) {
						resList.add(scanResult);
					}
				}
			} else {
				System.err.println("扫描为空");
			}
		}
		return resList;
	}
	
	/**
	 * 判断当前WiFi是否正确连接指定WiFi
	 * @param SSID
	 * @return
	 */
	public boolean isWifiConnected(String SSID) {
		WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
		return wifiInfo != null && wifiInfo.getSSID().equals(SSID);
	}
	
	/**
	 * 获取当前连接WiFi的SSID
	 * @return
	 */
	public String getConnectedSSID() {
		WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
		return wifiInfo != null ? wifiInfo.getSSID().replaceAll("\"", "") : "";
	}

	public boolean connectWifi(String ssid,boolean isGroupTransfer){
        //WifiManager的addNetwork接口，传入WifiConfiguration后，得到对应的NetworkId
        int netId = mWifiManager.addNetwork(createWifiConfig(ssid, FileUtil.getPasswordForHotspot(isGroupTransfer), WIFICIPHER_WPA));
        //WifiManager的enableNetwork接口，就可以连接到netId对应的wifi了
        //其中boolean参数，主要用于指定是否需要断开其它Wifi网络
        boolean enable = mWifiManager.enableNetwork(netId, true);
        return enable;
	}
    
    /**
     * 断开指定ID的网络
     * @param SSID
     */
    public boolean disconnectWifi(String SSID) {
    	return mWifiManager.disableNetwork(getNetworkIdBySSID(SSID)) && mWifiManager.disconnect();
    }

    /**
     * 清除指定SSID的网络
     * @param SSID
     */
    public void clearWifiConfig(String SSID) {
        SSID = SSID.replace("\"", "");
        List<WifiConfiguration> wifiConfigurations = mWifiManager.getConfiguredNetworks();
        if(wifiConfigurations != null && wifiConfigurations.size() > 0) {
            for(WifiConfiguration wifiConfiguration : wifiConfigurations) {
                if(wifiConfiguration.SSID.replace("\"", "").contains(SSID)) {
                    mWifiManager.removeNetwork(wifiConfiguration.networkId);
                    mWifiManager.saveConfiguration();
                }
            }
        }
    }

    /**
     * 清除当前连接的WiFi网络
     */
    public void clearWifiConfig() {
        String SSID = mWifiManager.getConnectionInfo().getSSID().replace("\"", "");
        List<WifiConfiguration> wifiConfigurations = mWifiManager.getConfiguredNetworks();
        if(wifiConfigurations != null && wifiConfigurations.size() > 0) {
            for(WifiConfiguration wifiConfiguration : wifiConfigurations) {
                if(wifiConfiguration.SSID.replace("\"", "").contains(SSID)) {
                    mWifiManager.removeNetwork(wifiConfiguration.networkId);
                    mWifiManager.saveConfiguration();
                }
            }
        }
    }
    
    private boolean isAdHoc(final ScanResult scanResule) {
		return scanResule.capabilities.indexOf("IBSS") != -1;
	}
    
    /**
     * 根据SSID查networkID
     *
     * @param SSID
     * @return
     */
    public int getNetworkIdBySSID(String SSID) {
        if (TextUtils.isEmpty(SSID)) {
            return 0;
        }
        WifiConfiguration config = isExsits(SSID);
        if (config != null) {
            return config.networkId;
        }
        return 0;
    }

    /**
     * 获取连接WiFi后的IP地址
     * @return
     */
    public String getIpAddressFromHotspot() {
        DhcpInfo dhcpInfo = mWifiManager.getDhcpInfo();
        if(dhcpInfo != null) {
            int address = dhcpInfo.gateway;
            return ((address & 0xFF)
                    + "." + ((address >> 8) & 0xFF)
                    + "." + ((address >> 16) & 0xFF)
                    + "." + ((address >> 24) & 0xFF));
        }
        return null;
    }
    
    /**
     * 添加一个网络并连接 传入参数：WIFI发生配置类WifiConfiguration
     */
    public boolean addNetwork(WifiConfiguration wcg) {
        int wcgID = mWifiManager.addNetwork(wcg);
        return mWifiManager.enableNetwork(wcgID, true);
    }
	
    /**
     * 获取当前手机所连接的wifi信息
     */
    public WifiInfo getCurrentWifiInfo() {
        return mWifiManager.getConnectionInfo();
    }
	
    /**
     * 获取指定WiFi信息
     * @param SSID
     * @return
     */
    private WifiConfiguration isExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        if(existingConfigs != null && existingConfigs.size() > 0) {
            for (WifiConfiguration existingConfig : existingConfigs) {
                if (existingConfig.SSID.equals(SSID) || existingConfig.SSID.equals("\"" + SSID + "\"")) {
                    return existingConfig;
                }
            }
        }
        return null;
    }

    /**
     * 过滤WiFi扫描结果
     * @return
     */
    private List<ScanResult> filterScanResult(List<ScanResult> scanResults) {
        List<ScanResult> result = new ArrayList<>();
        if(scanResults == null) {
            return result;
        }
        for (ScanResult scanResult : scanResults) {
            if (!TextUtils.isEmpty(scanResult.SSID) && scanResult.SSID.contains(Constants.WIFI_HOT_SPOT_SSID_PREFIX) /*&& scanResult.level > -80*/) {
                result.add(scanResult);
            }
        }
        for (int i = 0; i < result.size(); i++) {
            for (int j = 0; j < result.size(); j++) {
                //将搜索到的wifi根据信号强度从强到弱进行排序
                if (result.get(i).level > result.get(j).level) {
                    ScanResult temp = result.get(i);
                    result.set(i, result.get(j));
                    result.set(j, temp);
                }
            }
        }
        for (ScanResult s : result){
            Log.e("hangh","ScanResult s = " + s.toString());
        }
        return result;
    }

    public static final int WIFICIPHER_NOPASS = 0;
    public static final int WIFICIPHER_WEP = 1;
    public static final int WIFICIPHER_WPA = 2;
    public WifiConfiguration createWifiConfig(String ssid, String password, int type) {
        LogUtil.i("connect , createWifiConfig , ssid = " + ssid + " , password = " + password + " , type = "+type);
        password = password + ssid.split("-")[1];
        //初始化WifiConfiguration
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        //指定对应的SSID
        config.SSID = "\"" + ssid + "\"";
        //如果之前有类似的配置
        WifiConfiguration tempConfig = isExist(ssid);
        if(tempConfig != null) {
            //则清除旧有配置
            mWifiManager.removeNetwork(tempConfig.networkId);
        }
        //不需要密码的场景
        if(type == WIFICIPHER_NOPASS) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            //以WEP加密的场景
        } else if(type == WIFICIPHER_WEP) {
            config.hiddenSSID = true;
            config.wepKeys[0]= "\""+password+"\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
            //以WPA加密的场景，自己测试时，发现热点以WPA2建立时，同样可以用这种配置连接
        } else if(type == WIFICIPHER_WPA) {
            config.preSharedKey = "\""+password+"\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }

    private WifiConfiguration isExist(String ssid) {
        List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration config : configs) {
            if (config.SSID.equals("\""+ssid+"\"")) {
                return config;
            }
        }
        return null;
    }

    /**
     * 是否可以ping通指定IP地址
     * @param ipAddress
     * @return
     */
    public boolean pingIpAddress(String ipAddress) {
        try {
            Process process = Runtime.getRuntime().exec("/system/bin/ping -c 1 -w 100 " + ipAddress);
            int status = process.waitFor();
            if (status == 0) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @return
     */
    public String getLocalIp() {
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String ip = intToIp(ipAddress);
        return ip;
    }

    private static String intToIp(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "."
                + (i >> 24 & 0xFF);
    }

    public InetAddress getBroadcastAddress() throws IOException {
        DhcpInfo dhcp = mWifiManager.getDhcpInfo();
        if (dhcp == null) {
            return InetAddress.getByName(getBroadcastIp());
        }
        int ipAddress = dhcp.gateway;
        if(ipAddress == 0){
            return InetAddress.getByName(getBroadcastIp());
        }
        int broadcast = (ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }

    private String getBroadcastIp(){
        String hostIp = "192.168.43.1";
        LogUtil.i("hostIp = " + hostIp);
        hostIp = hostIp.substring(0,hostIp.length()-1);
        LogUtil.i("Go,receive : online hostIp= " + hostIp);
        return hostIp + "255";
    }

}
