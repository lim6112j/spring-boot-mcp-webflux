package com.ciel.ai

import io.modelcontextprotocol.client.McpSyncClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class AiApplication {
  private val logger: Logger = LoggerFactory.getLogger(AiApplication::class.java)
  @Bean
  fun predefinedQuestions(
          chatClientBuilder: ChatClient.Builder,
          mcpSyncClients: List<McpSyncClient>
  ): CommandLineRunner {
    return CommandLineRunner { args ->
      val chatClient =
              chatClientBuilder
                      .defaultToolCallbacks(SyncMcpToolCallbackProvider(mcpSyncClients))
                      .build()

      val question =
              "Does Spring AI supports the Model Context Protocol? Please provide some references."
      logger.info("QUESTION: {}\n", question)
      logger.info("ASSISTANT: {}\n", chatClient.prompt(question).call().content())
    }
  }
}

fun main(args: Array<String>) {
  runApplication<AiApplication>(*args)
}
