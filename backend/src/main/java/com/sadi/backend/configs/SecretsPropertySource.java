package com.sadi.backend.configs;

import lombok.NonNull;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;

public class SecretsPropertySource extends PropertySource<Map<String, String>> {

    static final String keyVaultUrl = "https://mindtrace-vault.vault.azure.net/";

    private static final Map<String, String> SECRET_MAPPINGS = Map.ofEntries(
            Map.entry("db-url", "DB_URL"),
            Map.entry("db-username", "DB_USERNAME"),
            Map.entry("db-password", "DB_PASSWORD"),
            Map.entry("redis-host", "REDIS_HOST"),
            Map.entry("redis-password", "REDIS_PASSWORD"),
            Map.entry("bot-token", "BOT_TOKEN"),
            Map.entry("qdrant-api-key", "QDRANT_API_KEY"),
            Map.entry("qdrant-host", "QDRANT_HOST"),
            Map.entry("email-password", "EMAIL_PASSWORD"),
            Map.entry("azure-openai-endpoint", "AZURE_OPENAI_ENDPOINT"),
            Map.entry("azure-openai-key", "AZURE_OPENAI_KEY"),
            Map.entry("firebase-credential", "FIREBASE_CREDENTIAL"),
            Map.entry("rabbitmq-user", "RABBITMQ_USER"),
            Map.entry("rabbitmq-password", "RABBITMQ_PASSWORD")
    );

    public SecretsPropertySource(Environment environment) {
        super("azureSecrets", loadSecrets(environment));
    }

    @Override
    public Object getProperty(@NonNull String name) {
        return source.get(name);
    }

    private static Map<String, String> loadSecrets(Environment environment) {
        String profile = environment.getProperty("spring.profiles.active", "dev");
        Map<String, String> secrets = new HashMap<>();

        if ("prod".equalsIgnoreCase(profile)) {
            System.out.println("Loading secrets from Docker secrets (prod mode)");
            for (Map.Entry<String, String> entry : SECRET_MAPPINGS.entrySet()) {
                String secretName = entry.getKey();
                String envName = entry.getValue();
                String path = "/run/secrets/" + secretName;
                try {
                    String value = readSecretFile(path);
                    secrets.put(envName, value);
                    System.out.println("Loaded secret from file: " + path + " -> " + envName);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to read secret: " + path, e);
                }
            }
        } else {
            System.out.println("Loading secrets from Azure Key Vault (dev mode)");
            SecretClient secretClient = new SecretClientBuilder()
                    .vaultUrl(keyVaultUrl)
                    .credential(new DefaultAzureCredentialBuilder().build())
                    .buildClient();

            for (Map.Entry<String, String> entry : SECRET_MAPPINGS.entrySet()) {
                String secretName = entry.getKey();
                String envName = entry.getValue();

                KeyVaultSecret secret = secretClient.getSecret(secretName);
                secrets.put(envName, secret.getValue());
                System.out.println("Fetched from Azure: " + secretName + " -> " + envName);
            }
        }

        return secrets;
    }

    private static String readSecretFile(String path) throws IOException {
        return Files.readString(Paths.get(path)).trim();
    }
}
