#!/bin/bash

echo "Setting up Web-based Android Emulator with VNC..."

# 安装必要的软件包
sudo apt-get update
sudo apt-get install -y \
    xvfb \
    x11vnc \
    fluxbox \
    wget \
    wmctrl \
    novnc \
    websockify

# 设置Android环境
export ANDROID_HOME=~/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator
export DISPLAY=:99

echo "Starting X Virtual Framebuffer..."
# 启动虚拟显示
Xvfb :99 -screen 0 1920x1080x24 &
sleep 2

# 启动窗口管理器
fluxbox &
sleep 2

echo "Starting VNC server..."
# 启动VNC服务器
x11vnc -display :99 -nopw -listen localhost -xkb -ncache 10 -ncache_cr -rfbport 5900 &
sleep 2

echo "Starting noVNC web client..."
# 启动noVNC（Web VNC客户端）
websockify --web=/usr/share/novnc/ 6080 localhost:5900 &

# 安装Android系统镜像（轻量级）
echo "Installing Android system images..."
sdkmanager "system-images;android-28;default;x86_64"
sdkmanager "emulator"

# 创建AVD
echo "Creating Android Virtual Device..."
echo "no" | avdmanager create avd \
  -n web_device \
  -k "system-images;android-28;default;x86_64" \
  --device "pixel_2" \
  --force

echo "Starting Android Emulator..."
# 启动Android模拟器（在虚拟显示中）
DISPLAY=:99 emulator -avd web_device \
  -no-audio \
  -gpu swiftshader_indirect \
  -memory 2048 \
  -skin 720x1280 &

echo "✅ Setup complete!"
echo ""
echo "🌐 Access the Android emulator via web browser:"
echo "   Open port 6080 in Gitpod (it should auto-open)"
echo "   Or visit: https://6080-yourworkspace.gitpod.io"
echo ""
echo "📱 Once the emulator is running, you can:"
echo "   1. Build your app: ./gradlew assembleDebug"
echo "   2. Install APK: adb install app/build/outputs/apk/debug/app-debug.apk"
echo "   3. Start app: adb shell am start -n com.example.cameraapp/.MainActivity"
echo ""
echo "🔧 Useful commands:"
echo "   - Check devices: adb devices"
echo "   - View logs: adb logcat"
echo "   - Take screenshot: adb shell screencap -p /sdcard/screenshot.png"