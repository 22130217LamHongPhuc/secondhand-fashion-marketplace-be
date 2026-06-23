package com.be;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Arrays;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
@EnableScheduling
public class BeApplication {

    public static void main(String[] args) {
        SpringApplication.run(BeApplication.class, args);
    }

    @Bean
    CommandLineRunner checkConfig(Environment env) {
        return args -> {
            System.out.println("ACTIVE PROFILES = " + Arrays.toString(env.getActiveProfiles()));
            System.out.println("DATASOURCE URL = " + env.getProperty("spring.datasource.url"));
            System.out.println("R2 ACCOUNT ID = " + env.getProperty("cloudflare.r2.account-id"));
            System.out.println("R2 ENDPOINT = " + env.getProperty("cloudflare.r2.endpoint"));
        };
    }

}
