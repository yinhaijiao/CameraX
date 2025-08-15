package com.example.cameraapp

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var viewFinder: PreviewView
    private lateinit var btnBrightnessDown: ImageButton
    private lateinit var btnBrightnessUp: ImageButton
    
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    
    private var originalBrightness: Float = 0.5f
    private var isVolumeKeyPressed = false
    private var isVolumeContinuousShoot = false
    private val continuousShootHandler = Handler(Looper.getMainLooper())
    private val startContinuousShootRunnable = Runnable {
        if (isVolumeKeyPressed) {
            isVolumeContinuousShoot = true
            continuousShootHandler.post(continuousShootRunnable)
        }
    }
    private val continuousShootRunnable = object : Runnable {
        override fun run() {
            if (isVolumeContinuousShoot && isVolumeKeyPressed) {
                takePhoto()
                continuousShootHandler.postDelayed(this, 100) // 每500ms拍一张
            }
        }
    }
    
    companion object {
        private const val TAG = "CameraApp"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        viewFinder = findViewById(R.id.viewFinder)
        btnBrightnessDown = findViewById(R.id.btnBrightnessDown)
        btnBrightnessUp = findViewById(R.id.btnBrightnessUp)
        
        // 保存原始亮度
        originalBrightness = getBrightness()
        
        // 设置亮度按钮点击事件
        btnBrightnessDown.setOnClickListener {
            setBrightnessToMinimum()
        }
        
        btnBrightnessUp.setOnClickListener {
            restoreOriginalBrightness()
        }
        
        // 检查权限
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
        
        cameraExecutor = Executors.newSingleThreadExecutor()
    }
    
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (event?.repeatCount == 0) {
                    // 首次按下
                    isVolumeKeyPressed = true
                    takePhoto() // 立即拍照
                    // 延迟启动连拍（1秒后开始）
                    continuousShootHandler.postDelayed(startContinuousShootRunnable, 100)
                }
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }
    
    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                // 按键释放，停止所有拍照相关操作
                isVolumeKeyPressed = false
                isVolumeContinuousShoot = false
                continuousShootHandler.removeCallbacks(startContinuousShootRunnable)
                continuousShootHandler.removeCallbacks(continuousShootRunnable)
                true
            }
            else -> super.onKeyUp(keyCode, event)
        }
    }
    
    private fun getBrightness(): Float {
        return try {
            Settings.System.getInt(
                contentResolver,
                Settings.System.SCREEN_BRIGHTNESS
            ) / 255f
        } catch (e: Settings.SettingNotFoundException) {
            0.5f
        }
    }
    
    private fun setBrightnessToMinimum() {
        val layoutParams = window.attributes
        layoutParams.screenBrightness = 0.0f // 设置为最低亮度
        window.attributes = layoutParams
        Toast.makeText(this, "屏幕亮度已调至最低", Toast.LENGTH_SHORT).show()
    }
    
    private fun restoreOriginalBrightness() {
        val layoutParams = window.attributes
        layoutParams.screenBrightness = originalBrightness
        window.attributes = layoutParams
        Toast.makeText(this, "屏幕亮度已恢复", Toast.LENGTH_SHORT).show()
    }
    
    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        
        val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraApp")
            }
        }
        
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()
        
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                }
                
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val msg = "照片保存成功"
                    runOnUiThread {
                        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    }
                    Log.d(TAG, msg)
                }
            }
        )
    }
    
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(viewFinder.surfaceProvider)
            }
            
            imageCapture = ImageCapture.Builder().build()
            
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
            
        }, ContextCompat.getMainExecutor(this))
    }
    
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "权限未授予", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        // 停止所有拍照相关操作
        isVolumeKeyPressed = false
        isVolumeContinuousShoot = false
        continuousShootHandler.removeCallbacks(startContinuousShootRunnable)
        continuousShootHandler.removeCallbacks(continuousShootRunnable)
    }
}