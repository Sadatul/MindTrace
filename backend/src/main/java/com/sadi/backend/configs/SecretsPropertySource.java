package com.sadi.backend.configs;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import lombok.NonNull;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

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

    private static final Map<String, String> TEST_SECRET_MAPPINGS = Map.ofEntries(
            Map.entry("bot-token", "BOT_TOKEN"),
            Map.entry("email-password", "EMAIL_PASSWORD"),
            Map.entry("azure-openai-endpoint", "AZURE_OPENAI_ENDPOINT"),
            Map.entry("azure-openai-key", "AZURE_OPENAI_KEY"),
            Map.entry("firebase-credential", "FIREBASE_CREDENTIAL")
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
            loadAllSecretsFromDockerSecrets(secrets);
        } else if ("test".equalsIgnoreCase(profile)) {
            System.out.println("Loading Azure credentials from Docker secrets and other secrets from Azure Key Vault (test mode)");
            loadSecretsFromAzureKeyVault(secrets, TEST_SECRET_MAPPINGS);
        } else {
            System.out.println("Loading secrets from Azure Key Vault (dev mode)");
            loadSecretsFromAzureKeyVault(secrets, SECRET_MAPPINGS);
        }

        return secrets;
    }

    private static void loadAllSecretsFromDockerSecrets(Map<String, String> secrets) {
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
    }

    private static void loadSecretsFromAzureKeyVault(Map<String, String> secrets, Map<String, String> secretMappings) {
        SecretClient secretClient = new SecretClientBuilder()
                .vaultUrl(keyVaultUrl)
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();

        for (Map.Entry<String, String> entry : secretMappings.entrySet()) {
            String secretName = entry.getKey();
            String envName = entry.getValue();

            try {
                KeyVaultSecret secret = secretClient.getSecret(secretName);
                secrets.put(envName, secret.getValue());
                System.out.println("Fetched from Azure Key Vault: " + secretName + " -> " + envName);
            } catch (Exception e) {
                System.err.println("Failed to fetch secret from Azure Key Vault: " + secretName);
                throw new RuntimeException("Failed to fetch secret: " + secretName, e);
            }
        }
    }

    private static String readSecretFile(String path) throws IOException {
        return Files.readString(Paths.get(path)).trim();
    }
}