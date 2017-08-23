package com.gionee.secretary.utils;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.gionee.secretary.bean.AddressJason;
import com.gionee.secretary.bean.WeatherSchedule;
import com.gionee.secretary.ui.viewInterface.ISetWeatherListener;
import com.gionee.secretary.service.VoiceBroadcastService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Created by luorw on 1/11/17.
 */
public class WeatherQueryUtil {
    private static String mUrl = "http://weather-api.gionee.com/weather-api/api/weather.json";

    /**
     * @param context
     * @param listener
     * @param cities      如：[{"cityName":"深圳","provinceName":"广东"}]
     * @param isDeparture
     */
    public static void getWeatherLive(Context context, ISetWeatherListener listener, AddressJason cities, boolean isDeparture) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("cityName", cities.getCityName());  //城市省份名称列表,最大长度128
        params.put("provinceName", cities.getProvinceName());
        params.put("phoneModel", Build.MODEL);//手机型号,最大长度32
        params.put("app", "com.gionee.secretary");//app包名,最大长度32
        params.put("imeiMd5", getImeiMd5(context));//imei的md5值,最大长度64
        params.put("networkType", "WF");//网络类型,最大长度16
        if (isDeparture) {
            getDepartureWeather(listener, params);
        } else {
            getDestinationWeather(listener, params);
        }
    }

    //added by luorw for voice broadcast 2017-03-25 begin
    public static void getWeatherForBroadCast(Context context, VoiceBroadcastService.IWeatherBroadcastListener listener, AddressJason cities) {
        Map<String, String> requestParams = new HashMap<String, String>();
        requestParams.put("cityName", cities.getCityName());  //城市省份名称列表,最大长度128
        requestParams.put("provinceName", cities.getProvinceName());
        requestParams.put("phoneModel", Build.MODEL);//手机型号,最大长度32
        requestParams.put("app", "com.gionee.secretary");//app包名,最大长度32
        requestParams.put("imeiMd5", getImeiMd5(context));//imei的md5值,最大长度64
        requestParams.put("networkType", "WF");//网络类型,最大长度16
        getWeather(listener, requestParams);
    }

    private static void getWeather(final VoiceBroadcastService.IWeatherBroadcastListener listener, final Map<String, String> requestParams) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<WeatherSchedule> list = responseToWeather(sendGet(requestParams));
                listener.getWeather(list);
            }
        }).start();
    }
    //added by luorw for voice broadcast 2017-03-25 end

    private static void getDepartureWeather(final ISetWeatherListener listener, final Map<String, String> params) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<WeatherSchedule> list = responseToWeather(sendGet(params));
                //added by luorw for GNSPR #69301 20170227 begin
                if (list == null || list.size() == 0) {
                    return;
                }
                //added by luorw for GNSPR #69301 20170227 end
                listener.showDepartureWeather(list.get(0));
            }
        }).start();
    }

    private static void getDestinationWeather(final ISetWeatherListener listener, final Map<String, String> params) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<WeatherSchedule> list = responseToWeather(sendGet(params));
                //added by luorw for GNSPR #69301 20170227 begin
                if (list == null || list.size() == 0) {
                    return;
                }
                //added by luorw for GNSPR #69301 20170227 end
                listener.showDestinationWeather(list.get(0));
            }
        }).start();
    }

    private static List<WeatherSchedule> responseToWeather(String result) {
        if (result == null) {
            return null;
        }
        List<WeatherSchedule> list = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONObject jWeather = jsonObject.getJSONObject("data");
            WeatherSchedule weatherSchedule = new WeatherSchedule();
            weatherSchedule.setAddress(jWeather.getString("cityName"));
            JSONObject jWeatherLive = jWeather.getJSONObject("weatherLive");
            weatherSchedule.setWeather(jWeatherLive.getString("weatherTxt"));
            weatherSchedule.setTemp(jWeatherLive.getString("temperature"));
            list.add(weatherSchedule);
            return list;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getImeiMd5(Context context) {
        String imei = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        LogUtils.i("luorw", "getImeiMd5......imei = " + imei);
        return md5Encode(imei);
    }

    private static String md5Encode(String string) {
        byte[] hash = new byte[0];
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) {
                hex.append("0");
            }
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

    private static synchronized String sendGet(Map<String, String> params) {
        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;
        try {
            String urlWithParams = mUrl + "?cityName=" + URLEncoder.encode(params.get("cityName"), "utf-8")
                    + "&provinceName=" + URLEncoder.encode(params.get("provinceName"), "utf-8")
                    + "&phoneModel=" + URLEncoder.encode(params.get("phoneModel"), "utf-8")
                    + "&app=" + URLEncoder.encode(params.get("app"), "utf-8")
                    + "&imeiMd5=" + URLEncoder.encode(params.get("imeiMd5"), "utf-8")
                    + "&networkType=" + URLEncoder.encode(params.get("networkType"), "utf-8");
            LogUtils.i("luorw", "url = " + urlWithParams);
            URL url = new URL(urlWithParams);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Accept-Encoding", "gzip");
            urlConnection.connect();
            int statusCode = urlConnection.getResponseCode();
            /* 200 represents HTTP OK */
            StringBuilder response = new StringBuilder();
            if (statusCode == 200) {
                // 定义BufferedReader输入流来读取URL的响应
                String encoding = urlConnection.getContentEncoding();
                InputStream ism = urlConnection.getInputStream();
                if (encoding != null && encoding.contains("gzip")) {
                    System.out.println("支持GZIP");
                    ism = new GZIPInputStream(urlConnection.getInputStream());
                }
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(ism, "UTF-8"));
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                LogUtils.i("luorw", "response = " + response.toString());
                return response.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }

}
