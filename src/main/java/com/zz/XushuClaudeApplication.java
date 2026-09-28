package com.zz;

import org.springaicommunity.agent.tools.FileSystemTools;
import org.springaicommunity.agent.tools.ShellTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.session.advisor.SessionMemoryAdvisor;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;

import java.util.Scanner;

@SpringBootApplication
public class XushuClaudeApplication {

    public static void main(String[] args) {
        SpringApplication.run(XushuClaudeApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(
            // 内部选择模型（唯一的）只配置一个模型的依赖
            ChatClient.Builder chatClientBuilder,
            DeepSeekChatModel deepSeekChatModel,
            ChatMemory chatMemory,
//            SessionMemoryAdvisor sessionMemoryAdvisor,
            SessionMemoryAdvisor sessionMemorySummarizationAdvisor,
            ToolService toolService,
            ToolCallbackProvider toolCallbackProvider
    ) {

        return args -> {
            // 对话代理（记忆、advisor大模型对话拦截器、结构化输入...）
            ChatClient chatClient = ChatClient.builder(deepSeekChatModel)
                    .defaultAdvisors(
//                            MessageChatMemoryAdvisor.builder(chatMemory).build()
//                            sessionMemoryAdvisor,
                            sessionMemorySummarizationAdvisor,
                            // 日志记录
                            SimpleLoggerAdvisor.builder().build()
                    )
                    .defaultTools(
                            // 自定义工具
//                            toolService
                            FileSystemTools.builder().build(),
                            ShellTools.builder().build(),

                            toolCallbackProvider
                    )
                    .build();

            // Start the chat loop
            System.out.println("\n我是xsCode:.\n");
            try (Scanner scanner = new Scanner(System.in)) {
                while (true) {
                    System.out.print("\n> 你:");
                    String userMessage = scanner.nextLine();

//                    String content = chatClient.prompt()
//                            .user(userMessage)
//                            .call()
//                            .content();
//                    System.out.println("\n> AI: " + content);

                    // 流式响应
                    System.out.println("\n> AI: ");
                    Flux<String> content = chatClient.prompt()
                            .user(userMessage)
                            .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "xushu"))
                            .stream()
                            .content();
                    content.doOnNext(System.out::print)
                            // 阻塞线程
                            .blockLast();
                }
            }
        };
    }

}
