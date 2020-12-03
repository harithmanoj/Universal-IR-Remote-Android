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
//

package com.remote.universalirremote.network;

import android.content.Intent;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public class HttpClient {
    private final NsdServiceInfo _serviceInfo;
    private HttpURLConnection _httpConnection;
    private String _serviceUrl;

    public static final String TAG = "HttpClient";


    public static final String RESPONSE_KEY = "response.transaction";
    public static final String RESPONSE_CODE_KEY = "response.key.transaction";
    public static final String TRANSACTION_KEY = "transaction.request";

    private HandlerThread _transactionHandlerThread;
    private Handler _transactionHandler;

    private Handler _responseHandler;

    public HttpClient(NsdServiceInfo info) {
        _serviceInfo = info;
        _serviceUrl = "http://" + _serviceInfo.getHost().getHostAddress();
        _transactionHandlerThread = new HandlerThread("transactionHandler");
        _transactionHandlerThread.start();
        _transactionHandler = new Handler(_transactionHandlerThread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                Request request = (Request) msg.getData().getParcelable(TRANSACTION_KEY);
                try {
                    try {
                        URL url = new URL(_serviceUrl);
                        _httpConnection = (HttpURLConnection)url.openConnection();
                    } catch (MalformedURLException ex) {
                        Log.e(TAG, "malformed url exception ", ex);
                        ex.printStackTrace();
                    } catch (IOException ex) {
                        Log.e(TAG, "IO exception ", ex);
                        ex.printStackTrace();
                    }
                    _httpConnection.setInstanceFollowRedirects(false);
                    if(request._requestMethod.equals("POST"))
                        _httpConnection.setDoOutput(true);
                    _httpConnection.setDoInput(true);
                    _httpConnection.setRequestMethod(request._requestMethod);
                    for (Request.Property i : request._requestProperties) {
                        _httpConnection.setRequestProperty(i._propertyName, i._propertyValue);
                    }
                    Log.i(TAG, "going to connect");
                    _httpConnection.connect();
                    if (request._requestMethod.equals("POST")) {
                        OutputStream os = _httpConnection.getOutputStream();
                        os.write(request._postData);
                        os.flush();
                        os.close();
                    }
                    Bundle msgBundle = new Bundle();
                    int responseCode = _httpConnection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) { //success
                        BufferedReader in = new BufferedReader(new InputStreamReader(
                                _httpConnection.getInputStream()));
                        String inputLine;
                        StringBuilder response = new StringBuilder();

                        while ((inputLine = in.readLine()) != null) {
                            Log.d(TAG, "read info " + inputLine);
                            response.append(inputLine);
                        }
                        in.close();
                        msgBundle.putString(RESPONSE_KEY, response.toString());
                    }
                    else
                    {
                        Log.e(TAG, "response not OK " + ((Integer)responseCode).toString());
                    }

                    _httpConnection.disconnect();
                    Log.i(TAG, "disconnected");
                    msgBundle.putInt(RESPONSE_CODE_KEY, responseCode);
                    msgBundle.putString(TRANSACTION_KEY, request._requestMethod);
                    Message msgr = new Message();
                    msgr.setData(msgBundle);
                    _responseHandler.sendMessage(msgr);

                } catch (IOException ex) {
                    Log.e(TAG, " exception at transaction executor", ex);
                    ex.printStackTrace();
                }

            }
        };

    }

    public void connect(Handler response) {
        _responseHandler = response;
    }

    public void transaction(Request request) {

        Message msg = new Message();
        Bundle msgBundle = new Bundle();
        msgBundle.putParcelable(TRANSACTION_KEY, request);
        msg.setData(msgBundle);
        _transactionHandler.sendMessage(msg);
    }

    public static class Request implements Parcelable {

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeStringArray( new String[] {_requestMethod, _postData.toString() });
            dest.writeInt(_requestProperties.size());
            Property[] prop = new Property[_requestProperties.size()];
            prop = _requestProperties.toArray(prop);
            dest.writeParcelableArray(prop,flags);
        }

        public static final Creator<Request> CREATOR = new Creator<Request>() {
            @Override
            public Request createFromParcel(Parcel source) {
                String[] data = new String[2];
                source.readStringArray(data);
                int size = source.readInt();
                Property[] prop = new Property[size];
                prop = (Property[])source.readParcelableArray(Property.class.getClassLoader());

                return new Request(
                        data[1].getBytes(),
                        data[0],
                        prop
                );
            }

            @Override
            public Request[] newArray(int size) {
                return new Request[size];
            }
        };

        public static class Property implements Parcelable {
            public final String _propertyName;
            public final String _propertyValue;

            public Property(String name, String value ) {
                _propertyName = name;
                _propertyValue = value;
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeStringArray(new String[] { _propertyName, _propertyValue });
            }

            public static final Creator<Property> CREATOR = new Creator<Property>() {
                @Override
                public Property createFromParcel(Parcel source) {
                    String[] data = new String[2];
                    source.readStringArray(data);
                    return new Property(
                            data[0],
                            data[1]
                    );
                }

                @Override
                public Property[] newArray(int size) {
                    return new Property[size];
                }
            };
        }

        public final byte[] _postData;
        public final String _requestMethod;
        public final ArrayList<Property> _requestProperties;

        public Request(byte[] data, String method, Property... properties) {
            _postData = data;
            _requestMethod = method;
            ArrayList<Property> tempProperties = new ArrayList<>(Arrays.asList(properties));
            if(method.equals("POST"))
                tempProperties.add(new Property("Content-Length", Integer.toString(data.length)));
            _requestProperties = tempProperties;
        }

    }

}
