package com.qa.dummyjson.base;

import com.qa.dummyjson.config.ApiConfig;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;

/**
 * Clase base: construye una única RequestSpecification reutilizable.
 * El filtro AllureRestAssured adjunta request/response de cada llamada al reporte.
 */
public abstract class BaseApiTest {

    protected static RequestSpecification spec;

    @BeforeAll
    static void initSpec() {
        spec = new RequestSpecBuilder()
                .setBaseUri(ApiConfig.BASE_URI)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilter(new AllureRestAssured())
                .build();
    }
}
