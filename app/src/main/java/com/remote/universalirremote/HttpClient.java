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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpClient {
    private NsdServiceInfo _serviceInfo;
    private HttpURLConnection _httpConnection;
    private String _serviceUrl;

    public static final String TAG = "HttpClient";

    public HttpClient(NsdServiceInfo info) {
        _serviceInfo = info;
        _serviceUrl = new String("http://") + _serviceInfo.getHost().getHostAddress();
    }

    public void connect() {
        try {
            URL url = new URL(_serviceUrl);
            _httpConnection = url.openConnection();
        } catch (MalformedURLException ex) {
            Log.e(TAG, "malformed url exception ", ex);
            ex.printStackTrace();
        } catch (IOException ex) {
            Log.e(TAG, "IO exception ", ex);
            ex.printStackTrace();
        }
    }

    
}
