package com.remote.universalirremote.network;

import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.remote.universalirremote.network.HttpClient;

public class RawSend {
    private HttpClient _httpClient;

    private Handler _responseHandler;
    private HandlerThread _responseHandlerThread;
    private Handler _outerHandler;

    public static final String TAG = "RawSend";

    public static final String PROTOCOL_KEY = "response.protocol";
    public static final String PROTOCOL_RAW = "response.raw";

    public RawSend(NsdServiceInfo info) {
        _httpClient = new HttpClient(info);
        _responseHandlerThread = new HandlerThread("transactionHandler");
        _responseHandlerThread.start();
        _responseHandler = new Handler(_responseHandlerThread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                HttpClient.Request request = (HttpClient.Request) msg.getData().getParcelable(_httpClient.TRANSACTION_KEY);

                Bundle msgBundle = new Bundle();

                String response = (String) msg.getData().getString(_httpClient.RESPONSE_KEY);

                int protocol = Integer.parseInt(response.substring(0, response.indexOf(';')));
                msgBundle.putInt(PROTOCOL_KEY, protocol);

                Log.i(TAG, String.format("Protocol: %d", protocol));

                if(protocol != -1) {
                    msgBundle.putString(PROTOCOL_RAW, response.substring(response.indexOf(';')+1));
                    Log.i(TAG, String.format("Raw: %s", response.substring(response.indexOf(';')+1)));
                }
                else {
                    msgBundle.putString(PROTOCOL_RAW, "0:");
                }

                Message msgr = new Message();
                msgr.setData(msgBundle);

                _getHandler.sendMessage(msgr);
            }
        };

        _httpClient.connect(_responseHandler);
    }

    public void connect(Handler handler)
    {
        _getHandler = handler;
    }

    public void getData()
    {
        _httpClient.transaction(new HttpClient.Request(
                null, "GET",
                new HttpClient.Request.Property("Content-Type", "application/xml"),
                new HttpClient.Request.Property("charset", "utf-8"),
                new HttpClient.Request.Property("Connection", "close")));

    }
}