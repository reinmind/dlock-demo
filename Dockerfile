# 使用一个基础的 Java 镜像
FROM openjdk:8

# 设置工作目录
WORKDIR /app

# 复制应用程序的 JAR 文件到镜像中
COPY target/dlock-demo-1.0.jar app.jar

# 暴露应用程序运行的端口
EXPOSE 8080

# 定义启动命令
CMD ["java", "-jar", "app.jar"]
