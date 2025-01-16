package com.suhaili.camera_plugin

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var imageCapture: ImageCapture
    private lateinit var executor: ExecutorService
    private var isFlashOn = false
    private var cameraProvider: ProcessCameraProvider? = null

    private val CAMERA_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        previewView = findViewById(R.id.previewView)
        val flashButton: Button = findViewById(R.id.flashButton)
        val captureButton: Button = findViewById(R.id.captureButton)

        executor = Executors.newSingleThreadExecutor()

        // Check for camera permission
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestCameraPermission()
        }

        flashButton.setOnClickListener {
            isFlashOn = !isFlashOn
            imageCapture.flashMode =
                if (isFlashOn) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF

            // Update flash state
            runOnUiThread {
                flashButton.text = if (isFlashOn) "Flash: ON" else "Flash: OFF"
            }
        }

        captureButton.setOnClickListener {
            if (::imageCapture.isInitialized) {
                val file = File(externalMediaDirs.first(), "${System.currentTimeMillis()}.jpg")
                val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
                imageCapture.takePicture(outputOptions, executor,
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onError(exc: ImageCaptureException) {
                            Toast.makeText(this@CameraActivity, "Capture failed: ${exc.message}", Toast.LENGTH_SHORT).show()
                            setResult(Activity.RESULT_CANCELED)
                            finish()
                        }

                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            val intent = Intent()
                            intent.putExtra("image_path", file.absolutePath)
                            setResult(Activity.RESULT_OK, intent)
                            finish()
                        }
                    })
            } else {
                Toast.makeText(this, "Camera not ready", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun allPermissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val preview = Preview.Builder().build()
            imageCapture = ImageCapture.Builder().build()

            preview.setSurfaceProvider(previewView.surfaceProvider)

            cameraProvider?.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageCapture
            )
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(Activity.RESULT_CANCELED)
        finish()
    }


    override fun onDestroy() {
        super.onDestroy()
        executor.shutdown()
        cameraProvider?.unbindAll()
        cameraProvider = null
    }
}
