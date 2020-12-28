package com.remote.universalirremote.network;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

public class WifiConfigure {
    public static final String TAG = "WifiConfigure";
    public static final String SERVER_ADDRESS = "192.168.4.1";
    public static final String SCAN_URI = "/scan";
    public static final String CONFIG_URI = "/wificonfig";

    public static final String SCAN_KEY = "response.scan_key";
    public static final String RESP_KEY = "response.config_key";

    private String[] scanResults;

    private HttpClient _httpScanClient;
    private HttpClient _httpConfigClient;

    private Handler _scanResponseHandler;
    private HandlerThread _scanResponseHandlerThread;

    private Handler _configResponseHandler;
    private HandlerThread _configResponseHandlerThread;

    private Handler _userScanHandler;
    private Handler _userUpdateHandler;

    public WifiConfigure(Handler updatehandler, Handler scanHandler)
    {
        _userUpdateHandler = updatehandler;
        _userScanHandler   = scanHandler;

        _scanResponseHandlerThread = new HandlerThread("ScanResponseHandlerThread");
        _scanResponseHandlerThread.start();
        _scanResponseHandler = new Handler(_scanResponseHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                String response = msg.getData().getString(_httpScanClient.RESPONSE_KEY);

                Log.i(TAG, " Received message: " + response);

                scanResults = response.split("\\$", 0);

                Bundle bundle = new Bundle();
                bundle.putStringArray(SCAN_KEY, scanResults);

                for(String s : scanResults)
                    Log.i(TAG, "ssid detected : "+s);

                Message msgresp = new Message();
                msgresp.setData(bundle);
                _userScanHandler.sendMessage(msgresp);
            }
        };

        _configResponseHandlerThread = new HandlerThread("ConfigurationHandlerThread");
        _configResponseHandlerThread.start();
        _configResponseHandler = new Handler(_configResponseHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                String response = msg.getData().getString(_httpScanClient.RESPONSE_KEY);

                Log.i(TAG, "Received message : " + response);

                Message resp = new Message();
                Bundle respBundle = new Bundle();
                respBundle.putString(RESP_KEY, response);
                resp.setData(respBundle);

                _userUpdateHandler.sendMessage(resp);
            }
        };

        _httpScanClient   = new HttpClient(SERVER_ADDRESS+SCAN_URI  , _scanResponseHandler);
        _httpConfigClient = new HttpClient(SERVER_ADDRESS+CONFIG_URI, _configResponseHandler);
    }

    public void getAccessPoints()
    {
        _httpScanClient.transaction(new HttpClient.Request(
                null, "GET",
                new HttpClient.Request.Property("Content-Type", "application/xml"),
                new HttpClient.Request.Property("charset", "utf-8"),
                new HttpClient.Request.Property("Connection", "close")
        ));
    }

    public void sendAccessPointData(String ssid, String password, String hostname)
    {
        _httpConfigClient.transaction(new HttpClient.Request(
                (hostname + "$" + ssid + "$" + password + "$").getBytes(), "POST",
                new HttpClient.Request.Property("Content-Type", "application/xml"),
                new HttpClient.Request.Property("charset", "utf-8"),
                new HttpClient.Request.Property("Connection", "close")
        ));
    }
}
