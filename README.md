# 给脚本执行权限
chmod +x setup_web_android.sh

# 运行设置脚本
./setup_web_android.sh



# 构建APK
./gradlew assembleDebug

# 等待模拟器完全启动
adb wait-for-device

# 安装应用
adb install app/build/outputs/apk/debug/app-debug.apk

# 启动应用
adb shell am start -n com.example.cameraapp/.MainActivity




# 直接连接手机
# 确保ADB可用
export ANDROID_HOME=~/android-sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools

# 启动ADB服务
adb start-server


# 1. 用USB线连接手机和电脑
# 2. 手机上会弹出授权提示，点击"允许"
# 3. 检查连接状态
adb devices

# 应该显示类似这样：
# List of devices attached
# ABC123DEF456    device


# 1. 确保手机和电脑在同一WiFi网络
# 2. 在开发者选项中开启"无线调试"
# 3. 点击"无线调试"进入，记下IP地址和端口号
# 4. 在电脑上连接
adb connect 192.168.1.100:5555  # 替换为实际IP和端口

# 验证连接
adb devices