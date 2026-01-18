.PHONY: run build clean test install help

# Chạy ứng dụng
run:
	.\mvnw.cmd spring-boot:run

# Build dự án
build:
	.\mvnw.cmd clean package

# Xóa các file build
clean:
	.\mvnw.cmd clean

# Chạy tests
test:
	.\mvnw.cmd test

# Cài đặt dependencies
install:
	.\mvnw.cmd clean install

# Build không chạy test
build-skip-test:
	.\mvnw.cmd clean package -DskipTests

# Chạy ở chế độ debug
debug:
	.\mvnw.cmd spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"

# Hiển thị thông tin dependencies
deps:
	.\mvnw.cmd dependency:tree

# Format code
format:
	.\mvnw.cmd spring-javaformat:apply

# Hiển thị hướng dẫn
help:
	@echo "UIT-Buddy Backend - Makefile Commands"
	@echo "======================================"
	@echo "make run              - Chạy ứng dụng"
	@echo "make build            - Build dự án"
	@echo "make clean            - Xóa các file build"
	@echo "make test             - Chạy tests"
	@echo "make install          - Cài đặt dependencies"
	@echo "make build-skip-test  - Build không chạy test"
	@echo "make debug            - Chạy ở chế độ debug (port 5005)"
	@echo "make deps             - Hiển thị dependency tree"
	@echo "make help             - Hiển thị hướng dẫn này"
