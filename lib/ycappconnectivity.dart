import 'dart:async';

import 'package:flutter/services.dart';

enum YConnectivityResult {
  wifiSlow,
  wifiMedium,
  wifiFast,
  mobileSlow,
  mobileMedium,
  mobileFast,
  none,
}

class YConnectivity {
  static const MethodChannel _methodChannel = MethodChannel(
    'ycappconnectivity/connectivity',
  );
  static const EventChannel _eventChannelState = EventChannel(
    'ycappconnectivity/connectivity_status',
  );
  static const EventChannel _eventChannelWifi = EventChannel(
    'ycappconnectivity/wifi_speed',
  );

  factory YConnectivity() {
    if (_singleton == null) {
      _singleton = YConnectivity._();
    }
    return _singleton;
  }

  YConnectivity._();

  static YConnectivity _singleton;

  Stream<YConnectivityResult> _onConnectivityChanged;
  Stream<int> _onWifiChanged;

  Stream<YConnectivityResult> get onConnectivityChanged {
    if (_onConnectivityChanged == null) {
      _onConnectivityChanged =
          _eventChannelState.receiveBroadcastStream().map((dynamic event) {
        print('eventChannelState');
        print(event);
        YConnectivityResult result = _parseConnectivityResult(event);
        print(result);
        return result;
      }).asBroadcastStream();
    }
    return _onConnectivityChanged.asBroadcastStream();
  }

  Stream<int> get onWifiChanged {
    if (_onWifiChanged == null) {
      _onWifiChanged =
          _eventChannelWifi.receiveBroadcastStream().map((dynamic event) {
        print('eventChannelWifi');
        print(event);
        return event;
      }).asBroadcastStream();
    }
    return _onWifiChanged.asBroadcastStream();
  }

  Future<YConnectivityResult> checkConnectivity() async {
    final String result = await checkRawConnectivity();
    return _parseConnectivityResult(result);
  }

  Future<String> checkRawConnectivity() async {
    final String result = await _methodChannel.invokeMethod('check');
    return result;
  }

  Future<bool> isConnectedFast() {
    return _methodChannel.invokeMethod('isConnectedFast');
  }

  Future<bool> isConnected() {
    return _methodChannel.invokeMethod('isConnected');
  }

  Future<bool> isConnectedWifi() {
    return _methodChannel.invokeMethod('isConnectedWifi');
  }

  Future<bool> isConnectedMobile() {
    return _methodChannel.invokeMethod('isConnectedMobile');
  }

  Future<int> getWifiStrength() {
    return _methodChannel.invokeMethod('getWifiStrength');
  }

  Future<bool> isWifiFast() {
    return _methodChannel.invokeMethod('isWifiFast');
  }

  YConnectivityResult _parseConnectivityResult(String state) {
    switch (state) {
      case 'wifiSlow':
        return YConnectivityResult.wifiSlow;
      case 'wifiMedium':
        return YConnectivityResult.wifiMedium;
      case 'wifiFast':
        return YConnectivityResult.wifiFast;
      case 'mobileSlow':
        return YConnectivityResult.mobileSlow;
      case 'mobileMedium':
        return YConnectivityResult.mobileMedium;
      case 'mobileFast':
        return YConnectivityResult.mobileFast;
      case 'none':
      case 'unknown':
      default:
        return YConnectivityResult.none;
    }
  }
}
