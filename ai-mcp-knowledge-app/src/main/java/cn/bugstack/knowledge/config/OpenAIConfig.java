package cn.bugstack.knowledge.config;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.DefaultChatClientBuilder;
import org.springframework.ai.chat.client.observation.ChatClientObservationConvention;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @Auther: yangjian
 * @Date: 2025-10-16 - 10 - 16 - 10:20
 * @Description: cn.bugstack.knowledge.config
 * @version: 1.0
 */
@Configuration
public class OpenAIConfig {

    @Bean
    public TokenTextSplitter tokenTextSplitter() {
        return new TokenTextSplitter();
    }

    @Bean
    public OpenAiApi openAiApi(@Value("${spring.ai.openai.base-url}") String baseUrl
            , @Value("${spring.ai.openai.api-key}") String apikey) {
        return OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apikey)
                .build();
    }

    @Bean
    public ChatClient chatClient(OpenAiChatModel openAiChatModel, ToolCallbackProvider tools
            , @Value("${spring.ai.openai.chat-model}") String chatModel) {
        DefaultChatClientBuilder defaultChatClientBuilder
                = new DefaultChatClientBuilder(openAiChatModel, ObservationRegistry.NOOP, (ChatClientObservationConvention) null);
        return defaultChatClientBuilder
                .defaultTools(tools)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(chatModel)
                        .build())
                .build();
    }

    @Bean("openAiSimpleVectorStore")
    public SimpleVectorStore vectorStore(OpenAiApi openAiApi) {
        OpenAiEmbeddingModel embeddingModel = new OpenAiEmbeddingModel(openAiApi);
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    /**
     * -- 删除旧的表（如果存在）
     * DROP TABLE IF EXISTS public.vector_store_openai;
     *
     * -- 创建新的表，使用UUID作为主键
     * CREATE TABLE public.vector_store_openai (
     *     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
     *     content TEXT NOT NULL,
     *     metadata JSONB,
     *     embedding VECTOR(1536)
     * );
     *
     * SELECT * FROM vector_store_openai
     */
    @Bean("openAiPgVectorStore")
    public PgVectorStore pgVectorStore(OpenAiApi openAiApi, JdbcTemplate jdbcTemplate
            , @Value("${spring.ai.openai.embedding-model}") String embeddingModelStr
            , @Value("${spring.ai.openai.embedding.options.num-batch}") Integer dimensions) {
        OpenAiEmbeddingOptions openAiEmbeddingOptions =
                OpenAiEmbeddingOptions.builder()
                        .model(embeddingModelStr).dimensions(dimensions).build();
        OpenAiEmbeddingModel embeddingModel = new OpenAiEmbeddingModel(openAiApi
                , MetadataMode.EMBED, openAiEmbeddingOptions);
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .vectorTableName("vector_store_openai")
                .build();
    }
}
