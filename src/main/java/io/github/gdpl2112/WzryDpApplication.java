package io.github.gdpl2112;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WzryDpApplication {
    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "true"); // 关闭 GUI 支持
        SpringApplication.run(WzryDpApplication.class, args);
    }
}