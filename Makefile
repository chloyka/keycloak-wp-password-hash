build:
	mvn clean package
	mkdir test/providers
	cp target/wordpress-password-hasher-1.0.0.jar test/providers
build-docker:
	docker buildx build \
	--platform linux/amd64 \
    -t kc:latest \
    test
