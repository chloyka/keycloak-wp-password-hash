build:
	mvn clean package
build-docker:
	docker buildx build \
	--platform linux/amd64 \
    -t kc:latest \
    .
