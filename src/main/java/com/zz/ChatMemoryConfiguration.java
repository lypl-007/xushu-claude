package com.zz;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.session.DefaultSessionService;
import org.springframework.ai.session.InMemorySessionRepository;
import org.springframework.ai.session.SessionService;
import org.springframework.ai.session.advisor.SessionMemoryAdvisor;
import org.springframework.ai.session.compaction.RecursiveSummarizationCompactionStrategy;
import org.springframework.ai.session.compaction.SlidingWindowCompactionStrategy;
import org.springframework.ai.session.compaction.TurnCountTrigger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatMemoryConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                // 记录最大条数 10 条
                .maxMessages(10)
                .chatMemoryRepository(chatMemoryRepository)
                .build();
    }

//    /**
//     * 按条数压缩
//     */
//    @Bean
//    SessionMemoryAdvisor sessionMemoryAdvisor() {
//        SessionService service = DefaultSessionService.builder()
//                .sessionRepository(InMemorySessionRepository.builder().build())
//                .build();
//
//        return SessionMemoryAdvisor.builder(service)
//                .defaultUserId("xushu")
//                .compactionTrigger(new TurnCountTrigger(1))
//                .compactionStrategy(SlidingWindowCompactionStrategy.builder().maxEvents(1).build())
//                .build();
//    }

    /**
     * LLM提取摘要
     */
    @Bean
    SessionMemoryAdvisor sessionMemorySummarizationAdvisor(DeepSeekChatModel deepSeekchatModel) {
        SessionService service = DefaultSessionService.builder()
                .sessionRepository(InMemorySessionRepository.builder().build())
                .build();

        return SessionMemoryAdvisor.builder(service)
                .defaultUserId("xushu")
                .compactionTrigger(new TurnCountTrigger(1))
                .compactionStrategy(
                        RecursiveSummarizationCompactionStrategy.builder(
                                        ChatClient.builder(deepSeekchatModel).build()
                                )
                                // 保留不压缩的条数
                                .maxEventsToKeep(2)
                                // 除压缩外需要提供给LLM的额外条数
                                .overlapSize(0)
                                .build()
                )
                .build();
    }

}
