# 第一阶段：Maven 编译环境（使用与你本地版本非常接近的 JDK17 和 Maven 镜像）
FROM maven:3.8.5-openjdk-17-slim AS build
WORKDIR /app

# 为了利用 Docker 缓存加速构建，我们先仅复制 pom.xml
COPY pom.xml .
# 预先下载依赖。这步能省掉后续很多重复加载时间
RUN mvn dependency:go-offline -B || true

# 复制整个项目源码并进行一次真正地无测试的无交互打包
COPY src ./src
RUN mvn clean package -DskipTests

# 第二阶段：运行轻量级环境
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 从第一阶段提取我们编译出的纯洁的 jar 包
COPY --from=build /app/target/bandwidth-sharing-1.0.0.jar app.jar

# 暴露 8080 端口（云平台会去监听这玩意）
EXPOSE 8080

# 容器正式启动后，相当于你在电脑上双击了 .bat 文件
ENTRYPOINT ["java", "-jar", "app.jar"]
