package com.example.cameraapp

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var viewFinder: PreviewView
    private lateinit var cameraCaptureButton: FloatingActionButton
    private lateinit var btnDimScreen: Button
    private lateinit var btnRestoreBrightness: Button
    
    // 亮度相关变量
    private var originalBrightness: Float = -1f
    private var isDimmed = false
    
    // 长按连拍相关变量
    private var isLongPressing = false
    private val continuousHandler = Handler(Looper.getMainLooper())
    private val continuousRunnable = object : Runnable {
        override fun run() {
            if (isLongPressing) {
                takePhoto()
                continuousHandler.postDelayed(this, 500) // 每500ms拍一张
            }
        }
    }

    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private const val REQUEST_CODE_WRITE_SETTINGS = 11
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化视图
        viewFinder = findViewById(R.id.viewFinder)
        cameraCaptureButton = findViewById(R.id.camera_capture_button)
        btnDimScreen = findViewById(R.id.btn_dim_screen)
        btnRestoreBrightness = findViewById(R.id.btn_restore_brightness)

        // 保存原始亮度
        saveOriginalBrightness()

        // 检查相机权限
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        // 设置按钮点击事件
        cameraCaptureButton.setOnClickListener { takePhoto() }
        btnDimScreen.setOnClickListener { dimScreen() }
        btnRestoreBrightness.setOnClickListener { restoreBrightness() }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    // 保存原始亮度
    private fun saveOriginalBrightness() {
        try {
            originalBrightness = Settings.System.getInt(
                contentResolver,
                Settings.System.SCREEN_BRIGHTNESS
            ).toFloat()
        } catch (e: Settings.SettingNotFoundException) {
            originalBrightness = 128f // 默认中等亮度
        }
    }

    // 调暗屏幕
    private fun dimScreen() {
        if (!Settings.System.canWrite(this)) {
            // 请求修改系统设置权限
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            startActivityForResult(intent, REQUEST_CODE_WRITE_SETTINGS)
            Toast.makeText(this, "请授予修改系统设置权限", Toast.LENGTH_LONG).show()
            return
        }

        try {
            // 设置最暗亮度（通常是1-10之间）
            Settings.System.putInt(
                contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                5
            )
            
            // 同时设置当前窗口亮度
            val layoutParams = window.attributes
            layoutParams.screenBrightness = 5f / 255f
            window.attributes = layoutParams

            isDimmed = true
            btnRestoreBrightness.visibility = android.view.View.VISIBLE
            Toast.makeText(this, "屏幕亮度已调至最暗", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Toast.makeText(this, "调节亮度失败: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "调节亮度失败", e)
        }
    }

    // 恢复亮度
    private fun restoreBrightness() {
        if (!Settings.System.canWrite(this)) {
            Toast.makeText(this, "没有修改系统设置权限", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // 恢复原始亮度
            Settings.System.putInt(
                contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                originalBrightness.toInt()
            )
            
            // 恢复当前窗口亮度
            val layoutParams = window.attributes
            layoutParams.screenBrightness = originalBrightness / 255f
            window.attributes = layoutParams

            isDimmed = false
            btnRestoreBrightness.visibility = android.view.View.GONE
            Toast.makeText(this, "屏幕亮度已恢复", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Toast.makeText(this, "恢复亮度失败: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "恢复亮度失败", e)
        }
    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (event?.repeatCount == 0) {
                    // 第一次按下，开始长按检测
                    isLongPressing = true
                    continuousHandler.postDelayed(continuousRunnable, 800) // 800ms后开始连拍
                    takePhoto() // 立即拍一张
                }
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    // 处理音量键释放事件
    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                // 停止长按连拍
                isLongPressing = false
                continuousHandler.removeCallbacks(continuousRunnable)
                return true
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        // 创建带时间戳的文件名
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        }

        // 创建输出选项对象，包含文件和元数据
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        // 设置图像捕获监听器，在拍照后触发
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "拍照失败: ${exception.message}", exception)
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "拍照失败", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val msg = "拍照成功: ${output.savedUri}"
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "拍照成功", Toast.LENGTH_SHORT).show()
                    }
                    Log.d(TAG, msg)
                }
            }
        )
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // 用于将相机的生命周期绑定到生命周期所有者
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // 预览
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            // 图像捕获用例
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY) // 优化连拍性能
                .build()

            // 选择后置摄像头作为默认摄像头
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // 解绑用例，然后重新绑定
                cameraProvider.unbindAll()

                // 将用例绑定到相机
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                Log.e(TAG, "相机绑定失败", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "用户拒绝了权限。",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_WRITE_SETTINGS) {
            if (Settings.System.canWrite(this)) {
                Toast.makeText(this, "权限已授予，请重新点击调暗按钮", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "需要系统设置权限才能调节亮度", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        continuousHandler.removeCallbacks(continuousRunnable)
        
        // 恢复原始亮度
        if (isDimmed) {
            restoreBrightness()
        }
    }
}