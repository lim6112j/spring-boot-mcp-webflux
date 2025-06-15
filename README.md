# how to run

set .env at ~
ANTHROPIC_API_KEY
BRAVE_API_KEY

./mvnw spring-boot:run

curl -X POST http://localhost:8080/api/chat \
-H "Content-Type: application/json" \
-d '{"message": "Does Spring AI supports the Model Context Protocol? Please provide some references"}'

# spring-boot-mcp-webflux
