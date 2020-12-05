package com.remote.universalirremote.network;

import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

public class RawSend {
    private HttpClient _httpClient;

    private Handler _responseHandler;
    private HandlerThread _responseHandlerThread;
    private Handler _outerHandler;

    public static final String TAG = "RawSend";

    public static final String RESPONSE_KEY = "response.data";
    public static final String CODE_KEY = "response.code";

    public RawSend(NsdServiceInfo info) {
        _httpClient = new HttpClient(info);
        _responseHandlerThread = new HandlerThread("SendHandlerThread");
        _responseHandlerThread.start();
        _responseHandler = new Handler(_responseHandlerThread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                String response = (String) msg.getData().getString(_httpClient.RESPONSE_KEY);

                Log.i(TAG, String.format("Message was :%s", response));

                Bundle msgBundle = new Bundle();
                msgBundle.putString(RESPONSE_KEY, response);
                msgBundle.putInt(CODE_KEY, msg.getData().getInt(_httpClient.RESPONSE_CODE_KEY));
                Message msgr = new Message();
                msgr.setData(msgBundle);

                _outerHandler.sendMessage(msgr);
            }
        };

        _httpClient.connect(_responseHandler);
    }

    public void connect(Handler handler) {
        _outerHandler = handler;
    }

    public void sendData(String msg) {
        _httpClient.transaction(new HttpClient.Request(
                msg.getBytes(), "POST",
                new HttpClient.Request.Property("Content-Type", "application/xml"),
                new HttpClient.Request.Property("charset", "utf-8"),
                new HttpClient.Request.Property("Connection", "close")));

    }
}
