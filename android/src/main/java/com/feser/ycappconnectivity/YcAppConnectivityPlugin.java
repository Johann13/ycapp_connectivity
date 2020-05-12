package com.feser.ycappconnectivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.PluginRegistry;

//none, wifi,
public class YcAppConnectivityPlugin implements MethodCallHandler, FlutterPlugin {
    private final static String channelName = "ycappconnectivity/connectivity";
    private final static String eventName = "ycappconnectivity/connectivity_status";
    private final static String eventWifiName = "ycappconnectivity/wifi_speed";

    private MethodChannel channel;
    private EventChannel eventChannel;
    private EventChannel eventChannelWifi;

    private static BroadcastReceiver receiverConnectivity;
    private static BroadcastReceiver receiverWifi;

    private static final String TAG = "YConnPlugin";
    private static TelephonyManager telephonyManager;
    private static WifiManager wifiManager;
    private ConnectivityManager manager;
    private static int wifiLevel = 5;


    public static void registerWith(PluginRegistry.Registrar registrar) {
        YcAppConnectivityPlugin plugin = new YcAppConnectivityPlugin();
        plugin.setupChannels(registrar.messenger(), registrar.context());
    }

    private static BroadcastReceiver createReceiver(final EventChannel.EventSink events) {
        Log.d(TAG, "createReceiver");
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isLost = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
                if (isLost) {
                    events.success("none");
                    return;
                }

                int type = intent.getIntExtra(ConnectivityManager.EXTRA_NETWORK_TYPE, -1);
                String networkType = Connectivity.getNetworkType(telephonyManager, wifiManager, type);
                Log.d("YConPlugin", "ConnectivityState: " + networkType);
                events.success(networkType);
            }
        };
    }

    private static BroadcastReceiver createReceiverWifi(final EventChannel.EventSink eventSink) {
        Log.d(TAG, "createReceiverWifi");
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                Log.d("YConPlugin", "WifiState: " + state);
                switch (state) {
                    case WifiManager.WIFI_STATE_DISABLED:
                        eventSink.success(-1);
                        break;
                    case WifiManager.WIFI_STATE_DISABLING:
                        eventSink.success(-1);
                        break;
                    case WifiManager.WIFI_STATE_ENABLED:
                        int strength = Connectivity.getWifiStrength(wifiManager, wifiLevel);
                        eventSink.success(strength);
                        break;
                    case WifiManager.WIFI_STATE_ENABLING:
                        eventSink.success(-1);
                        break;
                    case WifiManager.WIFI_STATE_UNKNOWN:
                    default:
                        eventSink.success(-1);
                        break;
                }
            }
        };
    }

    @Override
    public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
        switch (methodCall.method) {
            case "check":
                handleCheck(methodCall, result);
                break;
            case "isConnectedFast":
                result.success(Connectivity.isConnectedFast(manager));
                break;
            case "isConnected":
                result.success(Connectivity.isConnected(manager));
                break;
            case "isConnectedWifi":
                result.success(Connectivity.isConnectedWifi(manager));
                break;
            case "isConnectedMobile":
                result.success(Connectivity.isConnectedMobile(manager));
                break;
            case "getWifiStrength":
                result.success(Connectivity.getWifiStrength(wifiManager, wifiLevel));
                break;
            case "isWifiFast":
                boolean wifi = Connectivity.isConnectedWifi(manager);
                if (wifi) {
                    int strength = Connectivity.getWifiStrength(wifiManager, wifiLevel);
                    result.success(strength >= 3);
                } else {
                    result.success(false);
                }
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    private void handleCheck(MethodCall call, final MethodChannel.Result result) {
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            result.success(Connectivity.getNetworkType(telephonyManager, wifiManager, info.getType()));
        } else {
            result.success("none");
        }
    }

    @Override
    public void onAttachedToEngine(FlutterPluginBinding flutterPluginBinding) {
        setupChannels(flutterPluginBinding.getBinaryMessenger(), flutterPluginBinding.getApplicationContext());
    }

    @Override
    public void onDetachedFromEngine(FlutterPluginBinding binding) {
        teardownChannels();
    }

    private void setupChannels(BinaryMessenger messenger, final Context context) {
        channel = new MethodChannel(messenger, channelName);
        eventChannel = new EventChannel(messenger, eventName);
        eventChannelWifi = new EventChannel(messenger, eventWifiName);

        manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        wifiManager = (WifiManager) context
                .getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);

        channel.setMethodCallHandler(this);
        eventChannel.setStreamHandler(new EventChannel.StreamHandler() {
            @Override
            public void onListen(Object o, EventChannel.EventSink events) {
                receiverConnectivity = createReceiver(events);
                Log.d(TAG, "eventChannel.onListen");
                context
                        .registerReceiver(receiverConnectivity,
                                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
            }

            @Override
            public void onCancel(Object o) {
                Log.d(TAG, "eventChannel.onCancel");
                context.unregisterReceiver(receiverConnectivity);
                receiverConnectivity = null;
            }
        });
        eventChannelWifi.setStreamHandler(new EventChannel.StreamHandler() {
            @Override
            public void onListen(Object o, EventChannel.EventSink events) {
                receiverWifi = createReceiverWifi(events);
                Log.d(TAG, "eventChannel.onListen");
                context
                        .registerReceiver(receiverWifi,
                                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
            }

            @Override
            public void onCancel(Object o) {
                Log.d(TAG, "eventChannel.onCancel");
                context.unregisterReceiver(receiverWifi);
                receiverWifi = null;
            }
        });
    }

    private void teardownChannels() {
        channel.setMethodCallHandler(null);
        eventChannel.setStreamHandler(null);
        eventChannelWifi.setStreamHandler(null);
        channel = null;
        eventChannel = null;
        eventChannelWifi = null;
    }
}
