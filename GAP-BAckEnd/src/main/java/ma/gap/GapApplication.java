package ma.gap;

import lombok.AllArgsConstructor;
import ma.gap.config.GlobalVariableConfig;

import ma.gap.security.SecurityAuditorAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.web.client.RestTemplate;

@AllArgsConstructor
@EnableJpaAuditing(auditorAwareRef="auditorAware")
@EnableConfigurationProperties(GlobalVariableConfig.class)
@EnableGlobalMethodSecurity(prePostEnabled = true)
@SpringBootApplication
public class GapApplication extends SpringBootServletInitializer implements CommandLineRunner {
	@Autowired
	GlobalVariableConfig globalVariableConfig;
	public static void main(String[] args) {
		SpringApplication.run(GapApplication.class, args);

	}
	public GapApplication() {
		super();
	}
	@Bean
	public RestTemplate getRestTemplate() {
		return new RestTemplate();
	}

	@Bean
	public AuditorAware<String> auditorAware() {
		return new SecurityAuditorAware(); // ← Sans paramètre
	}



	@Override
	public void run(String... args) throws Exception {
	}
	}

