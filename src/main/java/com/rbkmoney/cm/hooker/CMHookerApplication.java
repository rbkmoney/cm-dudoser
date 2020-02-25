package com.rbkmoney.cm.hooker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@ServletComponentScan
@SpringBootApplication
public class CMHookerApplication extends SpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(CMHookerApplication.class, args);
    }
}
