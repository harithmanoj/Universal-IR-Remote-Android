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


import java.util.ArrayList;

// Class to encapsulate application wide constants used eg: activity launcher key
//
public final class Constant {
    // Spinner Content when no services are available.
    public static final String NO_SELECT = "None";

    private Constant() {}

    // Key for the service info passed to DeviceSelect activity
    public static final String INT_SERVICE_KEY = "com.remote.universalirremote.ui.MainActivity.SERVICE";

    // Key to identify which activity launched intent
    public static final String INT_LAUNCHER_KEY = "com.remote.universalirremote.LAUNCHER";

    // Value for INT_LAUNCHER_KEY when MainActivity launches an activity.
    public static final int INT_LAUNCHER_MAIN = 0;

    // Value for INT_LAUNCHER_KEY when DeviceSelect launches an activity.
    public static final int INT_LAUNCHER_DEVICE_SELECT = 1;

    public static final int INT_LAUNCHER_NEW_DEVICE = 2;

    public static final int INT_LAUNCHER_AC_REMOTE = 3;

    public static final int INT_LAUNCHER_TV_CONFIGURE = 4;

    public static final int INT_LAUNCHER_TV_TRANSMIT = 5;

    public static final int INT_LAUNCHER_GEN_CONFIGURE = 6;

    public static final int INT_LAUNCHER_GEN_TRANSMIT = 7;

    public static final String INT_SELECTED_DEVICE = "com.remote.universalirremote.DEVICE.SELECTED";

    public static int getProtocol(String prt) {
        for( int i = 0; i< Protocols._protocolList.length; ++ i) {
            if (prt.equals(Protocols._protocolList[i]))
                return i;
        }
        return -1;
    }

    public static String getProtocol(int prt) {
        if ((prt >= 0) && (prt < Protocols._protocolList.length) ) {
            return Protocols._protocolList[prt];
        }
        return null;
    }

    public static String getLayout(int lay) {
        if(lay == Layout.LAY_AC) {
            return Layout.AC_SPINNER;
        } else if (lay == Layout.LAY_GEN) {
            return Layout.GEN_SPINNER;
        } else if (lay == Layout.LAY_TV) {
            return Layout.TV_SPINNER;
        } else
            return null;
    }

    public static int getLayout(String lay) {
        if(lay.equals(Layout.AC_SPINNER)) {
            return Layout.LAY_AC;
        } else if(lay.equals(Layout.GEN_SPINNER)) {
            return Layout.LAY_GEN;
        } else if(lay.equals(Layout.TV_SPINNER)) {
            return Layout.LAY_TV;
        } else
            return -1;
    }

    public static final class Layout {

        // Value to identify TV remote layout
        public static final int LAY_TV = 0;

        // Value to identify AC remote layout
        public static final int LAY_AC = 1;

        // Value to identify general remote layout
        public static final int LAY_GEN = 2;

        public static final String AC_SPINNER = "AC layout";
        public static final String TV_SPINNER = "TV layout";
        public static final String GEN_SPINNER = "Generic layout";
    }
    
    
    // Values for protocols (compatible with decode_type_t enumeration in ESP32 code)
    public static final class Protocols {

        public static final String[] _protocolList = {
                NO_SELECT,
                "Unused",
                "RC5",
                "RC6",
                "NEC",
                "SONY",
                "PANASONIC",
                "JVC",
                "SAMSUNG",
                "WHYNTER",
                "AIWA_RC_T501",
                "LG",
                "SANYO",
                "MITSUBISHI",
                "DISH",
                "SHARP",
                "COOLIX",
                "DAIKIN",
                "DENON ",
                "KELVINATOR ",
                "SHERWOOD",
                "MITSUBISHI_AC",
                "RCMM ",
                "SANYO_LC7461 ",
                "RC5X ",
                "GREE",
                "PRONTO ",
                "NEC_LIKE ",
                "ARGO ",
                "TROTEC ",
                "NIKAI ",
                "RAW ",
                "GLOBALCACHE ",
                "TOSHIBA_AC ",
                "FUJITSU_AC",
                "MIDEA ",
                "MAGIQUEST ",
                "LASERTAG ",
                "CARRIER_AC ",
                "HAIER_AC ",
                "MITSUBISHI2 ",
                "HITACHI_AC ",
                "HITACHI_AC1 ",
                "HITACHI_AC2 ",
                "GICABLE",
                "HAIER_AC_YRW02 ",
                "WHIRLPOOL_AC ",
                "SAMSUNG_AC",
                "LUTRON ",
                "ELECTRA_AC ",
                "PANASONIC_AC ",
                "PIONEER ",
                "LG2 ",
                "MWM ",
                "DAIKIN2",
                "VESTEL_AC",
                "TECO",
                "SAMSUNG36 ",
                "TCL112AC",
                "LEGOPF",
                "MITSUBISHI_HEAVY_88",
                "MITSUBISHI_HEAVY_152",
                "DAIKIN216",
                "SHARP_AC",
                "GOODWEATHER",
                "INAX",
                "DAIKIN160",
                "NEOCLIMA",
                "DAIKIN176",
                "DAIKIN128",
                "AMCOR",
                "DAIKIN152",
                "MITSUBISHI136",
                "MITSUBISHI112",
                "HITACHI_AC424",
                "SONY_38K",
                "EPSON",
                "SYMPHONY",
                "HITACHI_AC3",
                "DAIKIN64",
                "AIRWELL",
                "DELONGHI_AC",
                "DOSHISHA",
                "MULTIBRACKETS",
                "CARRIER_AC40",
                "CARRIER_AC64",
                "HITACHI_AC344",
                "CORONA_AC",
                "MIDEA24",
                "ZEPEAL",
                "SANYO_AC",
                "VOLTAS",
                "METZ",
                "TRANSCOLD",
                "TECHNIBEL_AC",
                "MIRAGE",
                "ELITESCREENS",
                "PANASONIC_AC32"
        };


        public static final int UNKNOWN = -1;
        public static final int UNUSED = 0;
        public static final int RC5 = 1;
        public static final int RC6 = 2;
        public static final int NEC = 3;
        public static final int SONY = 4;
        public static final int PANASONIC = 5;
        public static final int JVC = 6;
        public static final int SAMSUNG = 7;
        public static final int WHYNTER = 8;
        public static final int AIWA_RC_T501 = 9;
        public static final int LG = 10;
        public static final int SANYO = 11;
        public static final int MITSUBISHI = 12;
        public static final int DISH = 13;
        public static final int SHARP = 14;
        public static final int COOLIX = 15;
        public static final int DAIKIN = 16;
        public static final int DENON = 17;
        public static final int KELVINATOR = 18;
        public static final int SHERWOOD = 19;
        public static final int MITSUBISHI_AC = 20;
        public static final int RCMM = 21;
        public static final int SANYO_LC7461 = 22;
        public static final int RC5X = 23;
        public static final int GREE = 24;
        public static final int PRONTO = 25;
        public static final int NEC_LIKE = 26;
        public static final int ARGO = 27;
        public static final int TROTEC = 28;
        public static final int NIKAI = 29;
        public static final int RAW = 30;
        public static final int GLOBALCACHE = 31;
        public static final int TOSHIBA_AC = 32;
        public static final int FUJITSU_AC = 33;
        public static final int MIDEA = 34;
        public static final int MAGIQUEST = 35;
        public static final int LASERTAG = 36;
        public static final int CARRIER_AC = 37;
        public static final int HAIER_AC = 38;
        public static final int MITSUBISHI2 = 39;
        public static final int HITACHI_AC = 40;
        public static final int HITACHI_AC1 = 41;
        public static final int HITACHI_AC2 = 42;
        public static final int GICABLE = 43;
        public static final int HAIER_AC_YRW02 = 44;
        public static final int WHIRLPOOL_AC = 45;
        public static final int SAMSUNG_AC = 46;
        public static final int LUTRON = 47;
        public static final int ELECTRA_AC = 48;
        public static final int PANASONIC_AC = 49;
        public static final int PIONEER = 50;
        public static final int LG2 = 51;
        public static final int MWM = 52;
        public static final int DAIKIN2 = 53;
        public static final int VESTEL_AC = 54;
        public static final int TECO = 55;
        public static final int SAMSUNG36 = 56;
        public static final int TCL112AC = 57;
        public static final int LEGOPF = 58;
        public static final int MITSUBISHI_HEAVY_88 = 59;
        public static final int MITSUBISHI_HEAVY_152 = 60;
        public static final int DAIKIN216 = 61;
        public static final int SHARP_AC = 62;
        public static final int GOODWEATHER = 63;
        public static final int INAX = 64;
        public static final int DAIKIN160 = 65;
        public static final int NEOCLIMA = 66;
        public static final int DAIKIN176 = 67;
        public static final int DAIKIN128 = 68;
        public static final int AMCOR = 69;
        public static final int DAIKIN152 = 70;
        public static final int MITSUBISHI136 = 71;
        public static final int MITSUBISHI112 = 72;
        public static final int HITACHI_AC424 = 73;
        public static final int SONY_38K = 74;
        public static final int EPSON = 75;
        public static final int SYMPHONY = 76;
        public static final int HITACHI_AC3 = 77;
        public static final int DAIKIN64 = 78;
        public static final int AIRWELL = 79;
        public static final int DELONGHI_AC = 80;
        public static final int DOSHISHA = 81;
        public static final int MULTIBRACKETS = 82;
        public static final int CARRIER_AC40 = 83;
        public static final int CARRIER_AC64 = 84;
        public static final int HITACHI_AC344 = 85;
        public static final int CORONA_AC = 86;
        public static final int MIDEA24 = 87;
        public static final int ZEPEAL = 88;
        public static final int SANYO_AC = 89;
        public static final int VOLTAS = 90;
        public static final int METZ = 91;
        public static final int TRANSCOLD = 92;
        public static final int TECHNIBEL_AC = 93;
        public static final int MIRAGE = 94;
        public static final int ELITESCREENS = 95;
        public static final int PANASONIC_AC32 = 96;
    }

    public static class AcMode{
        public static int kOff  = -1,
            kAuto =  0,
            kCool =  1,
            kHeat =  2,
            kDry  =  3,
            kFan  =  4;
    };

    public static class AcFan{
        public static int  kAuto =   0,
            kMin =    1,
            kLow =    2,
            kMedium = 3,
            kHigh =   4,
            kMax =    5;
    }

    public static class AcSwingv{
        public static int kOff =    -1,
            kAuto =    0,
            kHighest = 1,
            kHigh =    2,
            kMiddle =  3,
            kLow =     4,
            kLowest =  5;
    }

    public static class AcSwingh{
        public static int  kOff =     -1,
            kAuto =     0,  // a.k.a. On.
            kLeftMax =  1,
            kLeft =     2,
            kMiddle =   3,
            kRight =    4,
            kRightMax = 5,
            kWide =     6;
    }
}
