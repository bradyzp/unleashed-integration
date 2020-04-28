package net.jastrab.unleashedintegration;

import net.jastrab.unleashedspringclient.EnableUnleashedClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@EnableUnleashedClient
public class UnleashedIntegrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(UnleashedIntegrationApplication.class, args);
    }

}
