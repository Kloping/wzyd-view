package io.github.gdpl2112;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class WzryDpApplication {
    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "true"); // 关闭 GUI 支持
        SpringApplication.run(WzryDpApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}