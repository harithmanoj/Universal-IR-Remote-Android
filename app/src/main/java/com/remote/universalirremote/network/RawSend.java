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
    }

    public void connect(Handler handler)
    {
        _outerHandler = handler;
    }

    public void sendData(String msg)
    {
        _httpClient.transaction(new HttpClient.Request(
                msg.getBytes(), "POST",
                new HttpClient.Request.Property("Content-Type", "application/xml"),
                new HttpClient.Request.Property("charset", "utf-8"),
                new HttpClient.Request.Property("Connection", "close")));

    }
}
