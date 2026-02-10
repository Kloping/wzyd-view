package io.github.gdpl2112;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@SpringBootApplication
@EnableScheduling
public class WzryDpApplication {

    public static final Lock LOCK = new ReentrantLock();

    public static void main(String[] args) {
        System.out.println("WzydView v1.3 pre start");
        SpringApplication.run(WzryDpApplication.class, args);
        System.out.println("WzydView v1.3 started successful!");
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Scheduled(cron = "0 */15 * * * *")
    public void gc() {
        System.gc();
    }
}