package com.ciel.ai

import io.modelcontextprotocol.client.McpSyncClient
import mu.KotlinLogging
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

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

    @GetMapping("/", produces = [MediaType.TEXT_HTML_VALUE])
    fun index(): Mono<String> {
        return Mono.just("""
            <!DOCTYPE html>
            <html>
            <head>
                <title>AI Chat Interface</title>
                <style>
                    body { font-family: Arial, sans-serif; max-width: 800px; margin: 0 auto; padding: 20px; }
                    .chat-container { border: 1px solid #ccc; border-radius: 8px; padding: 20px; margin: 20px 0; }
                    .message-input { width: 100%; min-height: 100px; padding: 10px; border: 1px solid #ddd; border-radius: 4px; }
                    .send-button { background: #007bff; color: white; padding: 10px 20px; border: none; border-radius: 4px; cursor: pointer; margin-top: 10px; }
                    .send-button:hover { background: #0056b3; }
                    .response-area { margin-top: 20px; padding: 15px; background: #f8f9fa; border-radius: 4px; min-height: 50px; }
                    .loading { color: #666; font-style: italic; }
                    .error { color: #dc3545; }
                    .api-info { background: #e9ecef; padding: 15px; border-radius: 4px; margin-bottom: 20px; }
                    .code { background: #f8f9fa; padding: 2px 4px; border-radius: 3px; font-family: monospace; }
                </style>
            </head>
            <body>
                <h1>AI Chat Interface</h1>
                
                <div class="api-info">
                    <h3>API Information</h3>
                    <p><strong>Endpoint:</strong> <span class="code">POST /api/chat</span></p>
                    <p><strong>Request Format:</strong> <span class="code">{"message": "your message here"}</span></p>
                    <p><strong>Response Format:</strong> <span class="code">{"response": "AI response here"}</span></p>
                </div>
                
                <div class="chat-container">
                    <h3>Test the Chat API</h3>
                    <textarea id="messageInput" class="message-input" placeholder="Enter your message here..."></textarea>
                    <br>
                    <button onclick="sendMessage()" class="send-button">Send Message</button>
                    
                    <div id="responseArea" class="response-area">
                        Response will appear here...
                    </div>
                </div>

                <script>
                    async function sendMessage() {
                        const messageInput = document.getElementById('messageInput');
                        const responseArea = document.getElementById('responseArea');
                        const message = messageInput.value.trim();
                        
                        if (!message) {
                            alert('Please enter a message');
                            return;
                        }
                        
                        try {
                            responseArea.innerHTML = '<div class="loading">Sending request...</div>';
                            
                            const response = await fetch('/api/chat', {
                                method: 'POST',
                                headers: {
                                    'Content-Type': 'application/json',
                                },
                                body: JSON.stringify({ message: message })
                            });
                            
                            if (!response.ok) {
                                throw new Error(`HTTP error! status: ${'$'}{response.status}`);
                            }
                            
                            const data = await response.json();
                            responseArea.innerHTML = `<strong>Response:</strong><br>${'$'}{data.response}`;
                            
                        } catch (error) {
                            responseArea.innerHTML = `<div class="error"><strong>Error:</strong> ${'$'}{error.message}</div>`;
                        }
                    }
                    
                    // Allow Enter key to send message (Ctrl+Enter for new line)
                    document.getElementById('messageInput').addEventListener('keydown', function(e) {
                        if (e.key === 'Enter' && !e.ctrlKey) {
                            e.preventDefault();
                            sendMessage();
                        }
                    });
                </script>
            </body>
            </html>
        """.trimIndent())
    }

    @PostMapping("/chat")
    fun chat(@RequestBody request: ChatRequest): Mono<ChatResponse> {
        return Mono.fromCallable {
            logger.info { "Received chat request: ${request.message}" }
            val response = chatClient.prompt(request.message).call().content() ?: "No response generated"
            logger.info { "Generated response: $response" }
            ChatResponse(response)
        }.subscribeOn(Schedulers.boundedElastic())
    }
}
