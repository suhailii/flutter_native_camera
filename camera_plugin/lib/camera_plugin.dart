import 'package:flutter/services.dart';

class CameraPlugin {
  static const MethodChannel _channel = MethodChannel('camera_plugin');

  static Future<String?> startCameraActivity() async {
    try {
      final String? imagePath =
          await _channel.invokeMethod('startCameraActivity');
      return imagePath;
    } catch (e) {
      throw 'Failed to open camera: $e';
    }
  }
}
