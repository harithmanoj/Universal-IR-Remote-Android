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

package com.remote.universalirremote;


// Class to encapsulate application wide constants used eg: activity launcher key
//
public final class Constant {
    // Spinner Content when no services are available.
    public static final String NO_SELECT = "None";

    private Constant() {}

    // Key for the service info passed to DeviceSelect activity
    public static final String INT_SERVICE_KEY = "com.remote.universalirremote.MainActivity.SERVICE";

    // Key to identify which activity launched intent
    public static final String INT_LAUNCHER_KEY = "com.remote.universalirremote.LAUNCHER";

    // Value for INT_LAUNCHER_KEY when MainActivity launches an activity.
    public static final int INT_LAUNCHER_MAIN = 0;

    // Value for INT_LAUNCHER_KEY when DeviceSelect launches an activity.
    public static final int INT_LAUNCHER_DEVICE_SELECT = 1;
}
