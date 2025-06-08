package com.sadi.backend.configs;


import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutionException;

@Configuration
@Slf4j
public class VectorStoreConfig {
    public final static String LOG_COLLECTION_NAME = "logs";

    @Bean
    public VectorStore logVectorStore(QdrantClient qdrantClient, EmbeddingModel embeddingModel) {
        try {
            Boolean exists = qdrantClient.collectionExistsAsync(LOG_COLLECTION_NAME).get();
            if (!exists) {
                log.info("Collection '{}' does not exist. Creating new collection...", LOG_COLLECTION_NAME);

                Collections.CollectionOperationResponse collectionOperationResponse = qdrantClient.createCollectionAsync(
                        Collections.CreateCollection.newBuilder()
                                .setCollectionName(LOG_COLLECTION_NAME)
                                .setVectorsConfig(
                                        Collections.VectorsConfig.newBuilder()
                                                .setParams(
                                                        Collections.VectorParams.newBuilder()
                                                                .setSize(embeddingModel.dimensions())
                                                                .setDistance(Collections.Distance.Cosine)
                                                                .build()))
                                .build()
                ).get();

                if (collectionOperationResponse.isInitialized()) {
                    log.info("Successfully created collection '{}'", LOG_COLLECTION_NAME);

                    // Create payload indexes
                    qdrantClient.createPayloadIndexAsync(
                            LOG_COLLECTION_NAME,
                            "userId",
                            Collections.PayloadSchemaType.Keyword,
                            null,
                            true,
                            null,
                            null
                    );
                    qdrantClient.createPayloadIndexAsync(
                            LOG_COLLECTION_NAME,
                            "type",
                            Collections.PayloadSchemaType.Keyword,
                            null,
                            true,
                            null,
                            null
                    );
                    qdrantClient.createPayloadIndexAsync(
                            LOG_COLLECTION_NAME,
                            "createdAt",
                            Collections.PayloadSchemaType.Datetime,
                            null,
                            true,
                            null,
                            null
                    );

                    log.info("Successfully created payload indexes for collection '{}'", LOG_COLLECTION_NAME);
                } else {
                    log.error("Failed to create collection '{}'. Collection operation response not initialized.", LOG_COLLECTION_NAME);
                    throw new RuntimeException("Failed to create collection: " + LOG_COLLECTION_NAME + ". Application startup aborted.");
                }
            } else {
                log.info("Collection '{}' already exists. Skipping creation.", LOG_COLLECTION_NAME);
            }

            return QdrantVectorStore.builder(qdrantClient, embeddingModel)
                    .collectionName(LOG_COLLECTION_NAME)
                    .batchingStrategy(new TokenCountBatchingStrategy())
                    .build();
        } catch (InterruptedException e) {
            log.error("Thread interrupted while creating collection '{}': {}", LOG_COLLECTION_NAME, e.getMessage(), e);
            Thread.currentThread().interrupt(); // Restore interrupted status
            throw new RuntimeException("Application startup failed due to interrupted collection creation: " + LOG_COLLECTION_NAME, e);
        } catch (ExecutionException e) {
            log.error("Execution failed while creating collection '{}': {}", LOG_COLLECTION_NAME, e.getMessage(), e);
            throw new RuntimeException("Application startup failed due to collection creation error: " + LOG_COLLECTION_NAME, e);
        } catch (Exception e) {
            log.error("Unexpected error while setting up collection '{}': {}", LOG_COLLECTION_NAME, e.getMessage(), e);
            throw new RuntimeException("Application startup failed due to unexpected error in collection setup: " + LOG_COLLECTION_NAME, e);
        }
    }
}
