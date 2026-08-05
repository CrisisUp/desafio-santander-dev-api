package me.dio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;

/**
 * Initializes our RESTful API.
 * <p>
 * The {@link OpenAPIDefinition} annotation declares a default server URL ("/") for
 * the OpenAPI spec, so Swagger UI resolves API calls against the same origin that
 * serves the UI.
 */

@OpenAPIDefinition(servers = {@Server(url = "/", description = "Default Server URL")})
@SpringBootApplication
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
