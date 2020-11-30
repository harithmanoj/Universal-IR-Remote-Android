/*

        Copyright (C) 2020  Contributors (in contributors file)

        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with this program.  If not, see <https://www.gnu.org/licenses/>.

       */

package com.remote.universalirremote;

import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

public class HttpClient {
    private final NsdServiceInfo _serviceInfo;
    private HttpURLConnection _httpConnection;
    private String _serviceUrl;

    public static final String TAG = "HttpClient";

    // executor limit
    private static final int NUMBER_OF_THREADS = 4;

    public static final String RESPONSE_KEY = "response.transaction";
    public static final String RESPONSE_CODE_KEY = "response.key.transaction";
    public static final String TRANSACTION_KEY = "transaction.method";

    // executor for asynchronous data
    static final ExecutorService _networkTransactionExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public HttpClient(NsdServiceInfo info) {
        _serviceInfo = info;
        _serviceUrl = "http://" + _serviceInfo.getHost().getHostAddress();
    }

    public void connect() {
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
    }

    public void transaction(Request request, Handler _handleResponse) {

        _networkTransactionExecutor.execute(
                () -> {
                    try {
                        _httpConnection.setInstanceFollowRedirects(false);
                        _httpConnection.setDoOutput(true);
                        _httpConnection.setRequestMethod(request._requestMethod);
                        for (Request.Property i : request._requestProperties) {
                            _httpConnection.setRequestProperty(i._propertyName, i._propertyValue);
                        }

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
                                response.append(inputLine);
                            }
                            in.close();
                            msgBundle.putString(RESPONSE_KEY, response.toString());
                        }

                        _httpConnection.disconnect();

                        msgBundle.putInt(RESPONSE_CODE_KEY, responseCode);
                        msgBundle.putString(TRANSACTION_KEY, request._requestMethod);
                        Message msg = new Message();
                        msg.setData(msgBundle);
                        _handleResponse.sendMessage(msg);

                    } catch (IOException ex) {
                        Log.e(TAG, " exception at transaction executor", ex);
                        ex.printStackTrace();
                    }

                }
        );


    }

    public static class Request {

        public static class Property {
            public final String _propertyName;
            public final String _propertyValue;

            public Property(String name, String value ) {
                _propertyName = name;
                _propertyValue = value;
            }
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
