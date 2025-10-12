# 使用一个基础的Java镜像，Java 开发工具包镜像版本需与项目Java版本一致
FROM eclipse-temurin:21-jre-alpine

# 设置工作目录
WORKDIR /app

# 将项目的JAR包复制到容器内
COPY target/Community_Shop_Backend-0.0.1-SNAPSHOT.jar app.jar

# 暴露应用程序运行的端口，根据你的Spring Boot项目实际端口修改，默认为8080
EXPOSE 8090

# 定义容器启动时执行的命令
CMD ["java", "-jar", "app.jar"]