package vn.vuonsen.fnb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// Chạy file này để khởi động server
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableJpaAuditing
public class FnbApplication {

    public static void main(String[] args) {
        SpringApplication.run(FnbApplication.class, args);
    }
}
