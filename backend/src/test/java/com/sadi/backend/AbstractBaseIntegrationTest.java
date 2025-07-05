package com.sadi.backend;

import com.sadi.backend.configs.SecretsPropertySource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = AbstractBaseIntegrationTest.TestContextInitializer.class)
public abstract class AbstractBaseIntegrationTest {

    protected static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER;
    protected static final GenericContainer<?> REDIS_CONTAINER;
    protected static final GenericContainer<?> QDRANT_CONTAINER;
    protected static final GenericContainer<?> RABBITMQ_CONTAINER;

    static {
        POSTGRE_SQL_CONTAINER = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"))
                .withDatabaseName("pinklifeline")
                .withUsername("pinklifeline")
                .withPassword("pinklifeline");

        REDIS_CONTAINER = new GenericContainer<>(DockerImageName.parse("redis:7.2"))
                .withExposedPorts(6379)
                .withCommand("redis-server --requirepass testpass");

        QDRANT_CONTAINER = new GenericContainer<>(DockerImageName.parse("qdrant/qdrant:latest"))
                .withExposedPorts(6334)
                .withEnv("QDRANT__SERVICE__GRPC_PORT", "6334");

        RABBITMQ_CONTAINER = new GenericContainer<>(DockerImageName.parse("sadatul/rabbitmq_mindtrace:latest"))
                .withExposedPorts(5672);


        // Start all containers
        POSTGRE_SQL_CONTAINER.start();
        REDIS_CONTAINER.start();
        QDRANT_CONTAINER.start();
        RABBITMQ_CONTAINER.start();
    }

    @DynamicPropertySource
    public static void dynamicProperties(DynamicPropertyRegistry registry) {
        // Override with test container properties (these will take precedence over Azure Key Vault)
        registry.add("spring.datasource.url", POSTGRE_SQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRE_SQL_CONTAINER::getPassword);
        registry.add("spring.datasource.password", POSTGRE_SQL_CONTAINER::getUsername);

        // Redis properties - override Azure Key Vault settings
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", REDIS_CONTAINER::getFirstMappedPort);

        // Qdrant properties - override Azure Key Vault settings
        registry.add("spring.ai.vectorstore.qdrant.host", QDRANT_CONTAINER::getHost);
        registry.add("spring.ai.vectorstore.qdrant.port", QDRANT_CONTAINER::getFirstMappedPort);

        // RabbitMQ properties - override Azure Key Vault settings
        registry.add("spring.rabbitmq.host", RABBITMQ_CONTAINER::getHost);
        registry.add("spring.rabbitmq.port", RABBITMQ_CONTAINER::getFirstMappedPort);
    }

    /**
     * Custom ApplicationContextInitializer to load Azure Key Vault secrets
     * and then override with test container properties
     */
    public static class TestContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            ConfigurableEnvironment environment = applicationContext.getEnvironment();

            // Ensure test profile is active
            if (!environment.acceptsProfiles("test")) {
                environment.setActiveProfiles("test");
            }

            try {
                // Load Azure Key Vault secrets first
                SecretsPropertySource secretsPropertySource = new SecretsPropertySource(environment);
                environment.getPropertySources().addFirst(secretsPropertySource);

                System.out.println("Successfully loaded Azure Key Vault secrets for test profile");

                // Note: DynamicPropertySource will override these values for containerized services
                System.out.println("Test containers will override DB, Redis, and Qdrant configurations");

            } catch (Exception e) {
                System.err.println("Failed to load Azure Key Vault secrets: " + e.getMessage());
                System.out.println("Proceeding with test container configurations only");
                // Don't fail the test - just log the error and continue with container configs
            }
        }
    }
}