# ============================================
# Stage 1: Build (maven + jdk17, 支持 ARM64 & AMD64)
# ============================================
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# 先复制 POM 文件，利用 Docker 缓存加速依赖下载
COPY pom.xml .
COPY label-common/pom.xml label-common/pom.xml
COPY label-system/pom.xml label-system/pom.xml
COPY label-admin/pom.xml  label-admin/pom.xml

# 下载依赖（利用缓存层）
RUN mvn dependency:go-offline -B || true

# 复制源码
COPY label-common/src label-common/src
COPY label-system/src label-system/src
COPY label-admin/src  label-admin/src

# 编译打包
RUN mvn clean package -DskipTests -B

# ============================================
# Stage 2: Runtime (仅 JDK，体积更小)
# ============================================
FROM eclipse-temurin:17-jdk

WORKDIR /app

# 从构建阶段复制 jar
COPY --from=builder /build/label-admin/target/*.jar app.jar

EXPOSE 8080

ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8"

ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]
