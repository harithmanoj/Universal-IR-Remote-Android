package com.remote.universalirremote.network;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;

public class WifiConfigure {
    public static final String TAG = "WifiConfigure";
    public static final String _serverIPAddress = "192.168.1.1";
    public static final String _serverGetURI = "/scan";
    public static final String _serverSendURI = "/wificonfig";

    public static final String SCAN_KEY = "response.ssids";

    private HttpClient _httpGetClient;
    private HttpClient _httpSendClient;

    private Handler _getResponseHandler;
    private HandlerThread _getResponseHandlerThread;

    private Handler _userGetHandler;
    private Handler _userSendHandler;

    public WifiConfigure(Handler handler)
    {
        _getResponseHandlerThread = new HandlerThread("ConfigurationResponseHandlerThread");
        _getResponseHandlerThread.start();
        _getResponseHandler = new Handler(_getResponseHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                String response = msg.getData().getString(_httpGetClient.RESPONSE_KEY);

                Log.i(TAG, " Received message: " + response);

                String[] ssids = response.split("\\n");

                Bundle bundle = new Bundle();
                bundle.putStringArray(SCAN_KEY, ssids);

                Message msgresp = new Message();
                msgresp.setData(bundle);
                _userGetHandler.sendMessage(msgresp);
            }
        };

        _httpGetClient = new HttpClient(_serverIPAddress+_serverGetURI, _getResponseHandler);
        _httpSendClient = new HttpClient(_serverIPAddress+_serverSendURI, _getResponseHandler);

        _userSendHandler = handler;
    }

    public void getAccessPoints()
    {
        _httpGetClient.transaction(new HttpClient.Request(
                null, "GET",
                new HttpClient.Request.Property("Content-Type", "application/xml"),
                new HttpClient.Request.Property("charset", "utf-8"),
                new HttpClient.Request.Property("Connection", "close")
        ));
    }

    public void sendAccessPointData(String ssid, String password)
    {
        _httpGetClient.transaction(new HttpClient.Request(
                (ssid + ":" + password).getBytes(), "POST",
                new HttpClient.Request.Property("Content-Type", "application/xml"),
                new HttpClient.Request.Property("charset", "utf-8"),
                new HttpClient.Request.Property("Connection", "close")
        ));
    }
}
