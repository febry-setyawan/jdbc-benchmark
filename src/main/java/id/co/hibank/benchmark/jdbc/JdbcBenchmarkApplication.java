package id.co.hibank.benchmark.jdbc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class JdbcBenchmarkApplication {

	public static void main(String[] args) {
		SpringApplication.run(JdbcBenchmarkApplication.class, args);
	}

}
