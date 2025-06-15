package com.ciel.ai

import io.modelcontextprotocol.client.McpSyncClient
import mu.KotlinLogging
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

data class ChatRequest(val message: String)
data class ChatResponse(val response: String)

@RestController
@RequestMapping("/api")
class ChatController(
    private val chatClientBuilder: ChatClient.Builder,
    private val mcpSyncClients: List<McpSyncClient>
) {
    private val logger = KotlinLogging.logger {}
    
    private val chatClient by lazy {
        chatClientBuilder
            .defaultToolCallbacks(SyncMcpToolCallbackProvider(mcpSyncClients))
            .build()
    }

    @PostMapping("/chat")
    fun chat(@RequestBody request: ChatRequest): Mono<ChatResponse> {
        return Mono.fromCallable {
            logger.info { "Received chat request: ${request.message}" }
            val response = chatClient.prompt(request.message).call().content()
            logger.info { "Generated response: $response" }
            ChatResponse(response)
        }
    }
}
