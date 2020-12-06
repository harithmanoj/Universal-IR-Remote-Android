package com.remote.universalirremote.network;

import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.HandlerThread;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

public class RawGet {
    private HttpClient _httpClient;

    private Handler _responseHandler;
    private HandlerThread _responseHandlerThread;
    private Handler _outerHandler;

    public static final String TAG = "RawGet";

    public static final String PROTOCOL_KEY = "response.protocol";
    public static final String RAW_KEY = "response.raw";
    public static final String BUTTON_ID_KEY = "request.btn.id";

    public RawGet(NsdServiceInfo info, Handler handler) {
        _responseHandlerThread = new HandlerThread("GetHandlerThread");
        _responseHandlerThread.start();
        _responseHandler = new Handler(_responseHandlerThread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                String response = (String) msg.getData().getString(HttpClient.RESPONSE_KEY);

                int protocol = Integer.parseInt(response.substring(0, response.indexOf(';')));

                Bundle msgBundle = new Bundle();
                msgBundle.putInt(PROTOCOL_KEY, protocol);

                Log.i(TAG, String.format("Protocol: %d", protocol));

                if(protocol != -1) {
                    msgBundle.putString(RAW_KEY, response.substring(response.indexOf(';')+1));
                    Log.i(TAG, String.format("Raw: %s", response.substring(response.indexOf(';')+1)));
                }
                else {
                    msgBundle.putString(RAW_KEY, "0:");
                }

                msgBundle.putInt(BUTTON_ID_KEY, Integer.parseInt(
                        ((HttpClient.Request)msg.getData()
                                .getParcelable(HttpClient.TRANSACTION_KEY))
                                ._requestProperties.get(0).getValue()
                ));

                Message msgr = new Message();
                msgr.setData(msgBundle);

                _outerHandler.sendMessage(msgr);
            }
        };

        _httpClient = new HttpClient(info, _responseHandler);

        _outerHandler = handler;
    }

    public void getData(int btnId)
    {
        _httpClient.transaction(new HttpClient.Request(
                null, "GET",
                new HttpClient.Request.Property[]{
                        new HttpClient.Request.Property("Content-Type", "application/xml"),
                        new HttpClient.Request.Property("charset", "utf-8"),
                        new HttpClient.Request.Property("Connection", "close")
                },
                new HttpClient.Request.Property[]{
                        new HttpClient.Request.Property("buttonId", ((Integer)btnId).toString())
                }));

    }
}
