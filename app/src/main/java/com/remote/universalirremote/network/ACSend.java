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
import android.view.ViewDebug;

import androidx.annotation.NonNull;

public class ACSend {
    private final HttpClient _httpClient;

    private final HandlerThread _responseHandlerThread;
    private final Handler _outerHandler;

    public static final String TAG = "RawSend";

    public static final String DELIMITER = ",";

    public static final String RESPONSE_KEY = "response.data";
    public static final String CODE_KEY = "response.code";
    public static final String POST_MSG_KEY = "post.msg.key";
    public static final String POST_META_KEY = "post.meta.key";

    public static final String AC_URI = "/ac";

    public ACSend(NsdServiceInfo info, Handler handler, Runnable reAcquireService) {
        _responseHandlerThread = new HandlerThread("SendHandlerThread");
        _responseHandlerThread.start();
        Handler _responseHandler = new Handler(_responseHandlerThread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if(msg.getData().getInt(HttpClient.EXCEPTION_KEY, 0) == HttpClient.NO_ROUTE_TO_HOST) {
                    reAcquireService.run();
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

        _httpClient = new HttpClient(info.getHost().getHostAddress() + AC_URI, _responseHandler);

        _outerHandler = handler;
    }

    // Helper function for converting inputs to sendData to string to be sent : convert integer to string
    private String intEncode(int input) {
        return Integer.toString(input);
    }

    // Helper function for converting inputs to sendData to string to be sent : convert boolean to string
    private String boolEncode(boolean input) {
        if(input)
            return "1";
        else
            return "0";
    }

    // Helper function for converting inputs to sendData to string to be sent : convert float to string
    private String floatEncode(float input) {
        return Float.toString(input);
    }

    // Parses the passed string and sends AC message
    // Format : protocol, model, power, mode, degrees, celsius, fan, swingv, swingh, quiet, turbo, econo, light, filter, clean, beep, sleep, clock
    // Sample : 10,1,1,1,25,1,2,4,2,1,0,1,1,0,0,1,-1,-1
    // Format explained
    // - protocol   - int(decode_type_t)    - The vendor/protocol type.
    // - model      - int                   - The A/C model if applicable.
    // - power      - bool                  - The power setting.
    // - mode       - int(opmode_t)         - The operation mode setting.
    // - degrees    - float                 - The temperature setting in degrees.
    // - celsius    - bool                  - Temperature units. True is Celsius, False is Fahrenheit.
    // - fan        - int(fanspeed_t)       - The speed setting for the fan.
    // The following are all "if supported" by the underlying A/C classes.
    // - swingv     - int(swingv_t)         - The vertical swing setting.
    // - swingh     - int(swingh_t)         - The horizontal swing setting.
    // - quiet      - bool                  - Run the device in quiet/silent mode.
    // - turbo      - bool                  - Run the device in turbo/powerful mode.
    // - econo      - bool                  - Run the device in economical mode.
    // - light      - bool                  - Turn on the LED/Display mode.
    // - filter     - bool                  - Turn on the (ion/pollen/etc) filter mode.
    // - clean      - bool                  - Turn on the self-cleaning mode. e.g. Mould, dry filters etc
    // - beep       - bool                  - Enable/Disable beeps when receiving IR messages.
    // - sleep      - int                   - Nr. of minutes for sleep mode.
    // - clock      - int                   - The time in Nr. of mins since midnight. < 0 is ignore.
    // Integers and floats are converted from string, and boolean is represented by integers (true for > 0, false otherwise)
    public void sendData(int protocol, int model, boolean power,
                         int mode, float degrees, boolean celsius,
                         int fan, int swingv, int swingh,
                         boolean quiet, boolean turbo, boolean econo,
                         boolean light, boolean filter, boolean clean,
                         boolean beep, int sleep, int clock, String btnname) {
        String msg = intEncode(protocol) + DELIMITER +
                intEncode(model) + DELIMITER +
                boolEncode(power) + DELIMITER +
                intEncode(mode) + DELIMITER +
                floatEncode(degrees) + DELIMITER +
                boolEncode(celsius) + DELIMITER +
                intEncode(fan) + DELIMITER +
                intEncode(swingv) + DELIMITER +
                intEncode(swingh) + DELIMITER +
                boolEncode(quiet) + DELIMITER +
                boolEncode(turbo) + DELIMITER +
                boolEncode(econo) + DELIMITER +
                boolEncode(light) + DELIMITER +
                boolEncode(filter) + DELIMITER +
                boolEncode(clean) + DELIMITER +
                boolEncode(beep) + DELIMITER +
                intEncode(sleep) + DELIMITER +
                intEncode(clock) + DELIMITER;

        _httpClient.transaction(new HttpClient.Request(
                msg.getBytes(), "POST",
                new HttpClient.Request.Property[]{
                        new HttpClient.Request.Property("Content-Type", "application/xml"),
                        new HttpClient.Request.Property("charset", "utf-8"),
                        new HttpClient.Request.Property("Connection", "close")
                },
                new HttpClient.Request.Property[]{
                        new HttpClient.Request.Property("buttonName", btnname)
                }));

    }
}
