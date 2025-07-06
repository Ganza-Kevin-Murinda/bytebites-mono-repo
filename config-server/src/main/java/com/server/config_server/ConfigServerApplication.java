package com.server.config_server;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure()
				.filename(".env")
				.ignoreIfMissing()  // don't crash if .env is missing
				.load();

		dotenv.entries().forEach(entry ->
				System.setProperty(entry.getKey(), entry.getValue())
		);


		SpringApplication.run(ConfigServerApplication.class, args);
	}

}
