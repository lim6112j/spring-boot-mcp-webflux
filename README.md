# how to run

./mvnw spring-boot:run

curl -X POST http://localhost:8080/api/chat \
-H "Content-Type: application/json" \
-d '{"message": "Hello, how are you?"}'
# spring-boot-mcp-webflux
