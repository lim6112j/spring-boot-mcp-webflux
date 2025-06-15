package com.ciel.ai

import io.modelcontextprotocol.client.McpSyncClient
import mu.KotlinLogging
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class AiApplication {
  private val logger = KotlinLogging.logger {}
  @Bean
  fun predefinedQuestions(
          chatClientBuilder: ChatClient.Builder,
          mcpSyncClients: List<McpSyncClient>
  ): CommandLineRunner {
    return CommandLineRunner { args ->
      try {
        val chatClient =
                chatClientBuilder
                        .defaultToolCallbacks(SyncMcpToolCallbackProvider(mcpSyncClients))
                        .build()

        val question =
                "Does Spring AI supports the Model Context Protocol? Please provide some references."
        logger.info { "QUESTION: $question" }
        logger.info { "ASSISTANT: ${chatClient.prompt(question).call().content()}" }
      } catch (e: Exception) {
        logger.error(e) { "Error in predefined questions: ${e.message}" }
      }
    }
  }
}

fun main(args: Array<String>) {
  runApplication<AiApplication>(*args)
}
