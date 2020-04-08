package com.feser.ycappconnectivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class Connectivity {

    private static final String TAG = "Connectivity";

    public static String getNetworkType(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        WifiManager wifiManager = (WifiManager) context
                .getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            return (Connectivity.getNetworkType(telephonyManager, wifiManager, info.getType()));
        } else {
            return "none";
        }
    }

    public static String getNetworkType(TelephonyManager telephonyManager, WifiManager wifiManager, int type) {
        switch (type) {
            case ConnectivityManager.TYPE_ETHERNET:
            case ConnectivityManager.TYPE_WIFI:
            case ConnectivityManager.TYPE_WIMAX:
                Log.d(TAG, "getNetworkType.wifi");
                int level = getWifiStrength(wifiManager, 9);
                if (level >= 6) {           //5,7,8
                    return "wifiFast";
                } else if (level >= 3) {    //3,4,5,
                    return "wifiMedium";
                } else {                    //0,1,2,
                    return "wifiSlow";
                }
            case ConnectivityManager.TYPE_MOBILE:
            case ConnectivityManager.TYPE_MOBILE_DUN:
                Log.d(TAG, "getNetworkType.mobile");
                return getMobileNetworkType(telephonyManager);
            default:
                return "none";
        }
    }

    private static String getMobileNetworkType(TelephonyManager telephonyManager) {
        int networkType = telephonyManager.getNetworkType();
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                Log.d(TAG, "getMobileNetworkType.mobileSlow");
                return "mobileSlow";
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                Log.d(TAG, "getMobileNetworkType.mobileMedium");
                return "mobileMedium";
            case TelephonyManager.NETWORK_TYPE_LTE:
                Log.d(TAG, "getMobileNetworkType.mobileFast");
                return "mobileFast";
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
            default:
                Log.d(TAG, "getMobileNetworkType.unknown");
                return "unknown";
        }
    }

    public static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return getNetworkInfo(cm);
    }

    public static NetworkInfo getNetworkInfo(ConnectivityManager cm) {
        return cm.getActiveNetworkInfo();
    }

    public static boolean isConnected(Context context) {
        NetworkInfo info = getNetworkInfo(context);

        Log.d(TAG, "isConnected." + ((info != null && info.isConnected())));
        return (info != null && info.isConnected());
    }

    public static boolean isConnected(ConnectivityManager cm) {
        NetworkInfo info = getNetworkInfo(cm);
        Log.d(TAG, "isConnected." + ((info != null && info.isConnected())));
        return (info != null && info.isConnected());
    }

    public static boolean isConnectedWifi(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        Log.d(TAG, "isConnected." + (info != null && info.isConnected()
                && info.getType() == ConnectivityManager.TYPE_WIFI));
        return (info != null && info.isConnected()
                && info.getType() == ConnectivityManager.TYPE_WIFI);
    }

    public static boolean isConnectedWifi(ConnectivityManager cm) {
        NetworkInfo info = getNetworkInfo(cm);
        Log.d(TAG, "isConnected." + (info != null && info.isConnected()
                && info.getType() == ConnectivityManager.TYPE_WIFI));
        return (info != null && info.isConnected()
                && info.getType() == ConnectivityManager.TYPE_WIFI);
    }

    public static boolean isConnectedMobile(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        Log.d(TAG, "isConnected." + (info != null && info.isConnected()
                && info.getType() == ConnectivityManager.TYPE_MOBILE));
        return (info != null && info.isConnected()
                && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }

    public static boolean isConnectedMobile(ConnectivityManager cm) {
        NetworkInfo info = getNetworkInfo(cm);
        Log.d(TAG, "isConnected." + (info != null && info.isConnected()
                && info.getType() == ConnectivityManager.TYPE_MOBILE));
        return (info != null && info.isConnected()
                && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }

    public static boolean isConnectedFast(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        Log.d(TAG, "isConnected." + (info != null && info.isConnected()
                && isConnectionFast(info.getType(), info.getSubtype())));
        return (info != null && info.isConnected() && isConnectionFast(info.getType(), info.getSubtype()));
    }

    public static boolean isConnectedFast(ConnectivityManager cm) {
        NetworkInfo info = getNetworkInfo(cm);
        Log.d(TAG, "isConnected." + (info != null && info.isConnected()
                && isConnectionFast(info.getType(), info.getSubtype())));
        return (info != null && info.isConnected() && isConnectionFast(info.getType(), info.getSubtype()));
    }

    private static boolean isConnectionFast(int type, int subType) {
        if (type == ConnectivityManager.TYPE_WIFI) {
            Log.d(TAG, "isConnectionFast.wifi: " + true);
            return true;
        } else if (type == ConnectivityManager.TYPE_MOBILE) {
            switch (subType) {
                case TelephonyManager.NETWORK_TYPE_EVDO_0: // ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A: // ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_UMTS: // ~ 400-7000 kbps
                case TelephonyManager.NETWORK_TYPE_HSPA: // ~ 700-1700 kbps
                case TelephonyManager.NETWORK_TYPE_HSDPA: // ~ 2-14 Mbps
                case TelephonyManager.NETWORK_TYPE_HSUPA: // ~ 1-23 Mbps
                case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11 // ~ 1-2 Mbps
                case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9 // ~ 5 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13 // ~ 10-20 Mbps
                case TelephonyManager.NETWORK_TYPE_LTE: // API level 11 // ~ 10+ Mbps
                    Log.d(TAG, "isConnectionFast.mobile: " + true);
                    return true;
                case TelephonyManager.NETWORK_TYPE_1xRTT: // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_CDMA: // ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_EDGE: // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS: // ~ 100 kbps
                case TelephonyManager.NETWORK_TYPE_IDEN:  // ~25 kbps // API level 8
                case TelephonyManager.NETWORK_TYPE_UNKNOWN: // Unknown
                default:
                    Log.d(TAG, "isConnectionFast.mobile: " + false);
                    return false;
            }
        } else {
            Log.d(TAG, "isConnectionFast.unknown" + false);
            return false;
        }
    }

    public static int getWifiStrength(WifiManager wifiManager, int levels) {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), levels);
        Log.d(TAG, "getWifiStrength." + level);
        return level;
    }
}
