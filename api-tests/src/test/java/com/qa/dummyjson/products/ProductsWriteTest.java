package com.qa.dummyjson.products;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

import com.qa.dummyjson.base.BaseApiTest;
import com.qa.dummyjson.config.ApiConfig;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * DummyJSON SIMULA las escrituras: responde como si se hubieran guardado, pero no persiste nada.
 * Por eso estas pruebas son seguras de ejecutar en paralelo y no necesitan limpieza de datos.
 */
@Epic("DummyJSON API")
@Feature("Products - POST / PUT / DELETE")
@Tag("api")
@Tag("regression")
class ProductsWriteTest extends BaseApiTest {

    @Test
    @DisplayName("POST /products/add crea un producto (simulado) y devuelve un id nuevo")
    @Severity(SeverityLevel.CRITICAL)
    void createProduct() {
        Map<String, Object> body = Map.of(
                "title", "BMW Pencil",
                "price", 12.5,
                "category", "stationery");

        given().spec(spec)
                .body(body)
        .when()
                .post("/products/add")
        .then()
                // La documentación no fija el código; se aceptan 200 y 201 (ambos indican creación correcta).
                .statusCode(anyOf(is(200), is(201)))
                .time(lessThan(ApiConfig.MAX_RESPONSE_MS))
                .body(matchesJsonSchemaInClasspath("schemas/product-created.json"))
                .body("id", greaterThan(0))
                .body("title", equalTo("BMW Pencil"))
                .body("price", equalTo(12.5f));
    }

    @Test
    @DisplayName("PUT /products/{id} actualiza el título y conserva el id")
    @Severity(SeverityLevel.CRITICAL)
    void updateProduct() {
        given().spec(spec)
                .body(Map.of("title", "iPhone Galaxy +1"))
        .when()
                .put("/products/1")
        .then()
                .statusCode(200)
                .time(lessThan(ApiConfig.MAX_RESPONSE_MS))
                .body(matchesJsonSchemaInClasspath("schemas/product-created.json"))
                .body("id", equalTo(1))
                .body("title", equalTo("iPhone Galaxy +1"));
    }

    @Test
    @DisplayName("DELETE /products/{id} devuelve el producto con isDeleted=true y deletedOn")
    @Severity(SeverityLevel.CRITICAL)
    void deleteProduct() {
        given().spec(spec)
        .when()
                .delete("/products/1")
        .then()
                .statusCode(200)
                .time(lessThan(ApiConfig.MAX_RESPONSE_MS))
                .body(matchesJsonSchemaInClasspath("schemas/product-deleted.json"))
                .body("id", equalTo(1))
                .body("isDeleted", equalTo(true));
    }

    @Test
    @DisplayName("DELETE /products/{id} inexistente devuelve 404")
    @Severity(SeverityLevel.NORMAL)
    void deleteNonExistentProduct() {
        given().spec(spec)
        .when()
                .delete("/products/999999")
        .then()
                .statusCode(404)
                .time(lessThan(ApiConfig.MAX_RESPONSE_MS))
                .body(matchesJsonSchemaInClasspath("schemas/error.json"))
                .body("message", containsString("not found"));
    }

    @Test
    @DisplayName("PUT /products/{id} inexistente devuelve 404")
    @Severity(SeverityLevel.NORMAL)
    void updateNonExistentProduct() {
        given().spec(spec)
                .body(Map.of("title", "Does not exist"))
        .when()
                .put("/products/999999")
        .then()
                .statusCode(404)
                .time(lessThan(ApiConfig.MAX_RESPONSE_MS))
                .body(matchesJsonSchemaInClasspath("schemas/error.json"));
    }
}
