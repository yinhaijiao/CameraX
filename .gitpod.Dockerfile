FROM gitpod/workspace-full:latest

USER gitpod

# 安装OpenJDK 11
RUN brew install openjdk@11

# 设置Java环境
ENV JAVA_HOME=/home/linuxbrew/.linuxbrew/opt/openjdk@11
ENV PATH="$JAVA_HOME/bin:$PATH"

# 安装必要的工具
RUN sudo apt-get update && \
    sudo apt-get install -y unzip wget && \
    sudo rm -rf /var/lib/apt/lists/*