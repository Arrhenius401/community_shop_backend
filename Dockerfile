# --------------- 第一阶段 ---------------
# 第一步：构建阶段（使用包含Maven和Java 21的镜像）
# 声明构建阶段，命名为builder
FROM maven:3.9-eclipse-temurin-21 AS builder

# 为builder阶段设置工作目录
WORKDIR /app

# 复制pom.xml和源代码，编译生成jar包（使用Maven镜像避免本地依赖问题）
COPY pom.xml .
# 提前下载所有依赖，加速后续构建
RUN mvn dependency:go-offline
# 复制源代码
COPY src ./src

# 编译打包（跳过测试，生成jar包到target目录）
RUN mvn clean package -DskipTests

# --------------- 第二阶段 ---------------
# 第二步：运行阶段（使用轻量的Java 21运行时镜像）
FROM eclipse-temurin:21-jre-alpine

# 设置工作目录
WORKDIR /app

# 从builder阶段复制编译好的jar包（确保路径与实际生成的jar一致）
# 格式：COPY --from=builder /app/target/[你的项目名]-[版本].jar /app/app.jar
COPY --from=builder /app/target/Community_Shop_Backend-1.0.0.jar app.jar

# 配置JVM参数（适配文档“高稳定性”要求：设置堆内存、GC策略）
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 暴露应用程序运行的端口（与Spring Boot项目配置一致）
EXPOSE 8090

# 启动命令（执行jar包）
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]