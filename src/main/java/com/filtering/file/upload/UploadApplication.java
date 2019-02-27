package com.filtering.file.upload;

import com.filtering.file.upload.storage.StorageProperties;
import com.filtering.file.upload.storage.StorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
//@EnableConfigurationProperties(StorageProperties.class)
public class UploadApplication {

    public static void main(String[] args) {
        SpringApplication.run(UploadApplication.class, args);
    }

    @Bean
    CommandLineRunner init(StorageService storageService) {
        return (args) ->{
            storageService.deleteAll();
            storageService.init();
        };
    }

}
