//
//
//        Copyright (C) 2020  Contributors (in contributors file)
//
//        This program is free software: you can redistribute it and/or modify
//        it under the terms of the GNU General Public License as published by
//        the Free Software Foundation, either version 3 of the License, or
//        (at your option) any later version.
//
//        This program is distributed in the hope that it will be useful,
//        but WITHOUT ANY WARRANTY; without even the implied warranty of
//        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//        GNU General Public License for more details.
//
//        You should have received a copy of the GNU General Public License
//        along with this program.  If not, see <https://www.gnu.org/licenses/>.
//
package com.remote.universalirremote.network;

import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.HandlerThread;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

public class RawGet {
    private final HttpClient _httpClient;

    private final HandlerThread _responseHandlerThread;
    private final Handler _outerHandler;

    public static final String TAG = "RawGet";

    public static final String PROTOCOL_KEY = "response.protocol";
    public static final String RAW_KEY = "response.raw";
    public static final String BUTTON_ID_KEY = "request.btn.id";


    public void terminate() {
        _responseHandlerThread.quitSafely();
    }

    public RawGet(NsdServiceInfo info, Handler handler, NetworkErrorCallback errorCallback) {
        _responseHandlerThread = new HandlerThread("GetHandlerThread");
        _responseHandlerThread.start();
        _outerHandler = handler;
        Handler _responseHandler = new Handler(_responseHandlerThread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if(msg.getData().getInt(HttpClient.EXCEPTION_KEY, 0) == HttpClient.NO_ROUTE_TO_HOST) {
                    errorCallback.errorResponse("Host unreachable");
                    return;
                } else if (msg.getData().getInt(HttpClient.EXCEPTION_KEY, 0) == HttpClient.IO_EXCEPTION) {
                    errorCallback.errorResponse("IO exception "
                            + msg.getData().getString(HttpClient.EXCEPTION_DATA_KEY));
                    return;
                } else if (msg.getData().getInt(
                        HttpClient.EXCEPTION_KEY, 0) == HttpClient.CONNECT_EXCEPTION) {
                    errorCallback.errorResponse("Connection error, check if blaster is active");
                    return;
                }

                String response = msg.getData().getString(HttpClient.RESPONSE_KEY);

                int protocol = Integer.parseInt(response.substring(0, response.indexOf(';')));

                Bundle msgBundle = new Bundle();
                msgBundle.putInt(PROTOCOL_KEY, protocol);

                Log.i(TAG, String.format("Protocol: %d", protocol));

                msgBundle.putString(RAW_KEY, response.substring(response.indexOf(';') + 1));
                Log.i(TAG, String.format("Raw: %s", response.substring(response.indexOf(';') + 1)));

                msgBundle.putInt(BUTTON_ID_KEY, Integer.parseInt(
                        ((HttpClient.Request) msg.getData()
                                .getParcelable(HttpClient.TRANSACTION_KEY))
                                ._requestMeta.get(0).getValue()
                ));

                Message msgr = new Message();
                msgr.setData(msgBundle);

                _outerHandler.sendMessage(msgr);
            }
        };

        _httpClient = new HttpClient(info, _responseHandler);

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
