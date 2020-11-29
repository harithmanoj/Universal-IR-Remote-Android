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
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import javax.net.ssl.HttpsURLConnection;

public class HttpClient {
    private final NsdServiceInfo _serviceInfo;
    private HttpsURLConnection _httpsConnection;
    private String _serviceUrl;

    public static final String TAG = "HttpClient";

    public HttpClient(NsdServiceInfo info) {
        _serviceInfo = info;
        _serviceUrl = "https://" + _serviceInfo.getHost().getHostAddress();
    }

    public void connect() {
        try {
            URL url = new URL(_serviceUrl);
            _httpsConnection = (HttpsURLConnection)url.openConnection();
        } catch (MalformedURLException ex) {
            Log.e(TAG, "malformed url exception ", ex);
            ex.printStackTrace();
        } catch (IOException ex) {
            Log.e(TAG, "IO exception ", ex);
            ex.printStackTrace();
        }
    }

    public String transaction(Request request) throws IOException {
        _httpsConnection.setDoOutput(true);
        _httpsConnection.setInstanceFollowRedirects(false);
        _httpsConnection.setRequestMethod(request._requestMethod);
        for(Request.Property i : request._requestProperties) {
            _httpsConnection.setRequestProperty(i._propertyName, i._propertyValue);
        }

        _httpsConnection.connect();
        if(request._requestMethod.equals("POST")) {
            OutputStream os = _httpsConnection.getOutputStream();
            os.write(request._postData);
            os.flush();
            os.close();
        }

        int responseCode = _httpsConnection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) { //success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    _httpsConnection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            _httpsConnection.disconnect();
            return response.toString();
        } else {
            throw new IOException(request._requestMethod +
                    " response " + ((Integer)responseCode).toString());
        }
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
