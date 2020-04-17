package com.rbkmoney.cm.dudoser;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.core.io.ClassPathResource;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestHelper {

    public static ClassPathResource getClassPathResource(String filePath, Class<?> clazz) {
        return new ClassPathResource(filePath, clazz);
    }

}
