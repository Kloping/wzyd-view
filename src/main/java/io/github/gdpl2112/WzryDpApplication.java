package io.github.gdpl2112;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class WzryDpApplication {
    public static void main(String[] args) {
        System.out.println("WzydView v1.0 pre start");
        SpringApplication.run(WzryDpApplication.class, args);
        System.out.println("WzydView v1.0 started successful!");
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}