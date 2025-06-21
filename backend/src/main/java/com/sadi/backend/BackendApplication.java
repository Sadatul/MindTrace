package com.sadi.backend;

import com.sadi.backend.configs.SecretsPropertySource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.ConfigurableEnvironment;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(BackendApplication.class);

		app.addInitializers(applicationContext -> {
			ConfigurableEnvironment environment = applicationContext.getEnvironment();
			environment.getPropertySources().addFirst(new SecretsPropertySource(environment));
		});

		app.run(args);
	}

}