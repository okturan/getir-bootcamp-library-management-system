package com.okturan.getirbootcamplibrarymanagementsystem.config;

import io.swagger.v3.oas.models.info.Info;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class OpenApiConfigTest {

    @Test
    void publishesRepositoryContactAndMitLicenseMetadata() {
        Info info = new OpenApiConfig().customOpenAPI().getInfo();

        assertAll(
            () -> assertEquals("okturan", info.getContact().getName()),
            () -> assertEquals("https://github.com/okturan", info.getContact().getUrl()),
            () -> assertNull(info.getContact().getEmail()),
            () -> assertEquals("MIT License", info.getLicense().getName()),
            () -> assertEquals(
                "https://github.com/okturan/getir-bootcamp-library-management-system/blob/main/LICENSE",
                info.getLicense().getUrl()
            )
        );
    }
}
