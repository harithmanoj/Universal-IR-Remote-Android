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
import java.net.NoRouteToHostException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public class HttpClient {
    private HttpURLConnection _httpConnection;
    private final String _serviceUrl;

    public static final String TAG = "HttpClient";


    public static final String RESPONSE_KEY = "response.transaction";
    public static final String RESPONSE_CODE_KEY = "response.key.transaction";
    public static final String TRANSACTION_KEY = "transaction.request";
    public static final String EXCEPTION_KEY = "transaction.exception";
    public static final int NO_ROUTE_TO_HOST = 10;
    public static final int IO_EXCEPTION = 20;
    public static final String EXCEPTION_DATA_KEY = "exception.key";

    private final HandlerThread _transactionHandlerThread;
    private final Handler _transactionHandler;

    private final Handler _responseHandler;

    public HttpClient(NsdServiceInfo info, Handler handler) {
        this(info.getHost().getHostAddress(), handler);
    }

    public HttpClient(String serverAddress, Handler handler) {
        _serviceUrl = "http://" + serverAddress;
        _transactionHandlerThread = new HandlerThread("transactionHandler");
        _transactionHandlerThread.start();
        _transactionHandler = new Handler(_transactionHandlerThread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                Request request = msg.getData().getParcelable(TRANSACTION_KEY);
                try {
                    try {
                        URL url = new URL(_serviceUrl);
                        _httpConnection = (HttpURLConnection)url.openConnection();
                    } catch (MalformedURLException ex) {
                        Log.e(TAG, "malformed url exception ", ex);
                    } catch (IOException ex) {
                        Log.e(TAG, "IO exception ", ex);
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
                    msgBundle.putParcelable(TRANSACTION_KEY, request);
                    Message msgr = new Message();
                    msgr.setData(msgBundle);
                    _responseHandler.sendMessage(msgr);

                } catch (NoRouteToHostException ex) {
                    Log.i(TAG, " no route to host exception");
                    Bundle msgBundle = new Bundle();
                    msgBundle.putInt(EXCEPTION_KEY, NO_ROUTE_TO_HOST);
                    Message msgr = new Message();
                    msgr.setData(msgBundle);
                    _responseHandler.sendMessage(msgr);
                } catch (IOException ex) {
                    Log.e(TAG, " exception at transaction executor", ex);
                    Bundle msgBundle = new Bundle();
                    msgBundle.putInt(EXCEPTION_KEY, IO_EXCEPTION);
                    msgBundle.putString(EXCEPTION_DATA_KEY, ex.getMessage());
                    Message msgr = new Message();
                    msgr.setData(msgBundle);
                    _responseHandler.sendMessage(msgr);
                }

            }
        };
        _responseHandler = handler;
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
            dest.writeStringArray( new String[] {_requestMethod, Arrays.toString(_postData)});
            dest.writeInt(_requestProperties.size());
            Property[] prop = new Property[_requestProperties.size()];
            prop = _requestProperties.toArray(prop);
            dest.writeParcelableArray(prop,flags);
            if (_hasMeta) {
                dest.writeInt(1);
                dest.writeInt(_requestMeta.size());
                Property[] meta = new Property[_requestMeta.size()];
                meta = _requestMeta.toArray(meta);
                dest.writeParcelableArray(meta, flags);
            } else {
                dest.writeInt(0);
            }

        }

        public static final Creator<Request> CREATOR = new Creator<Request>() {
            @Override
            public Request createFromParcel(Parcel source) {
                String[] data = new String[2];
                source.readStringArray(data);
                Property[] prop = (Property[])source.readParcelableArray(Property.class.getClassLoader());
                int hasMeta = source.readInt();
                if(hasMeta == 0) {
                    return new Request(
                            data[1].getBytes(),
                            data[0],
                            prop
                    );
                } else {
                    Property[] meta = (Property[])source.readParcelableArray(Property.class.getClassLoader());
                    return new Request(
                            data[1].getBytes(),
                            data[0],
                            prop,
                            meta
                    );
                }
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

            public String getName() { return _propertyName; }
            public String getValue() { return _propertyValue; }
        }

        public final byte[] _postData;
        public final String _requestMethod;
        public final ArrayList<Property> _requestProperties;
        public final ArrayList<Property> _requestMeta;
        public final boolean _hasMeta;

        public byte[] getPostData() { return _postData; }
        public String getMethod() { return _requestMethod; }
        public ArrayList<Property> getProperties() { return _requestProperties; }
        public ArrayList<Property> getMeta() { return _requestMeta; }
        public boolean hasMeta() { return _hasMeta; }

        public Request(byte[] data, String method, Property... properties) {
            _postData = data;
            _requestMethod = method;
            ArrayList<Property> tempProperties = new ArrayList<>(Arrays.asList(properties));
            if(method.equals("POST"))
                tempProperties.add(new Property("Content-Length", Integer.toString(data.length)));
            _requestProperties = tempProperties;
            _hasMeta = false;
            _requestMeta = null;
        }

        public Request(byte[] data, String method, Property[] properties, Property[] meta) {
            _postData = data;
            _requestMethod = method;
            ArrayList<Property> tempProperties = new ArrayList<>(Arrays.asList(properties));
            if(method.equals("POST"))
                tempProperties.add(new Property("Content-Length", Integer.toString(data.length)));
            _requestProperties = tempProperties;
            if(meta != null) {
                _hasMeta = true;
                _requestMeta = new ArrayList<>(Arrays.asList(meta));
            } else {
                _hasMeta = false;
                _requestMeta = null;
            }
        }

    }

}
