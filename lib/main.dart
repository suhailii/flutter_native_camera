import 'dart:io';

import 'package:flutter/material.dart';
import 'package:camera_plugin/camera_plugin.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: HomeScreen(),
    );
  }
}

class HomeScreen extends StatefulWidget {
  @override
  _HomeScreenState createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  String? _imagePath;

  Future<void> _captureImage() async {
    try {
      final imagePath = await CameraPlugin.startCameraActivity();
      if (imagePath != null) {
        setState(() {
          _imagePath = imagePath;
        });
      }
    } catch (e) {
      print('Error: $e');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Native Camera Plugin')),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            ElevatedButton(
              onPressed: _captureImage,
              child: Text('Capture Image'),
            ),
            if (_imagePath != null) ...[
              SizedBox(height: 20),
              Image.file(File(_imagePath!)),
            ],
          ],
        ),
      ),
    );
  }
}
