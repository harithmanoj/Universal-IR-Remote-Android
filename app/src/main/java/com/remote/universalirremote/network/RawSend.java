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
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

public class RawSend {
    private final HttpClient _httpClient;

    private final HandlerThread _responseHandlerThread;
    private final Handler _outerHandler;

    public static final String TAG = "RawSend";

    public static final String RESPONSE_KEY = "response.data";
    public static final String CODE_KEY = "response.code";
    public static final String POST_MSG_KEY = "post.msg.key";
    public static final String POST_META_KEY = "post.meta.key";

    public RawSend(NsdServiceInfo info, Handler handler, NetworkErrorCallback errorCallback) {
        _responseHandlerThread = new HandlerThread("SendHandlerThread");
        _responseHandlerThread.start();
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
                }

                String response = msg.getData().getString(HttpClient.RESPONSE_KEY);

                Log.i(TAG, String.format("Message was :%s", response));

                Bundle msgBundle = new Bundle();
                msgBundle.putString(RESPONSE_KEY, response);
                msgBundle.putInt(CODE_KEY, msg.getData().getInt(HttpClient.RESPONSE_CODE_KEY));
                msgBundle.putString(POST_MSG_KEY,
                        new String((
                                (HttpClient.Request) msg.getData()
                                        .getParcelable(HttpClient.TRANSACTION_KEY))
                                ._postData));
                msgBundle.putParcelable(POST_META_KEY, ((HttpClient.Request) msg.getData()
                        .getParcelable(HttpClient.TRANSACTION_KEY))._requestMeta.get(0)
                );
                Message msgr = new Message();
                msgr.setData(msgBundle);

                _outerHandler.sendMessage(msgr);
            }
        };

        _httpClient = new HttpClient(info, _responseHandler);

        _outerHandler = handler;
    }

    public void sendData(String msg, String name) {
        Log.i(TAG, "sendData: " + msg);
        _httpClient.transaction(new HttpClient.Request(
                msg.getBytes(), "POST",
                new HttpClient.Request.Property[]{
                        new HttpClient.Request.Property("Content-Type", "application/xml"),
                        new HttpClient.Request.Property("charset", "utf-8"),
                        new HttpClient.Request.Property("Connection", "close")
                },
                new HttpClient.Request.Property[]{
                        new HttpClient.Request.Property("buttonName", name)
                }));

    }
}
