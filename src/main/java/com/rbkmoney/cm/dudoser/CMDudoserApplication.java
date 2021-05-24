package com.rbkmoney.cm.dudoser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SuppressWarnings("AbbreviationAsWordInName")
@ServletComponentScan
@SpringBootApplication
public class CMDudoserApplication extends SpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(CMDudoserApplication.class, args);
    }
}
