#!/bin/bash
set -e

echo "开始安装Android SDK..."

# 设置Android SDK路径
export ANDROID_HOME="$HOME/android-sdk"
echo "ANDROID_HOME设置为: $ANDROID_HOME"

# 创建Android SDK目录
mkdir -p $ANDROID_HOME
cd $ANDROID_HOME

# 检查是否已经下载
if [ ! -f "commandlinetools-linux-9477386_latest.zip" ] && [ ! -d "cmdline-tools" ]; then
    echo "下载Android命令行工具..."
    wget -q --show-progress https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip
    echo "下载完成"
fi

# 解压命令行工具
if [ ! -d "cmdline-tools/latest" ]; then
    echo "解压命令行工具..."
    unzip -q commandlinetools-linux-9477386_latest.zip
    
    # 创建正确的目录结构
    mkdir -p cmdline-tools/latest
    mv cmdline-tools/* cmdline-tools/latest/ 2>/dev/null || true
    
    # 清理
    rm -f commandlinetools-linux-9477386_latest.zip
    echo "命令行工具安装完成"
fi

# 设置PATH环境变量
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools

# 检查sdkmanager是否可用
if ! command -v sdkmanager &> /dev/null; then
    echo "错误: sdkmanager命令不可用"
    echo "PATH: $PATH"
    exit 1
fi

echo "接受许可证..."
yes | sdkmanager --licenses > /dev/null 2>&1 || echo "许可证已接受"

echo "安装SDK组件..."
sdkmanager --install "platform-tools" "platforms;android-33" "build-tools;33.0.2" --verbose

# 创建local.properties文件
echo "创建local.properties文件..."
echo "sdk.dir=$ANDROID_HOME" > /workspace/CameraX/local.properties

# 添加环境变量到.bashrc（如果还没有）
if ! grep -q "ANDROID_HOME" ~/.bashrc; then
    echo "添加环境变量到.bashrc..."
    echo "export ANDROID_HOME=\"$HOME/android-sdk\"" >> ~/.bashrc
    echo "export PATH=\$PATH:\$ANDROID_HOME/cmdline-tools/latest/bin:\$ANDROID_HOME/platform-tools" >> ~/.bashrc
fi

echo ""
echo "=========================="
echo "Android SDK安装完成！"
echo "ANDROID_HOME: $ANDROID_HOME"
echo "local.properties已创建于: /workspace/CameraX/local.properties"
echo ""
echo "请运行以下命令应用环境变量："
echo "source ~/.bashrc"
echo ""
echo "然后可以尝试构建项目："
echo "cd /workspace/CameraX && ./gradlew build"
echo "=========================="