package com.qa.dummyjson.products;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;

import com.qa.dummyjson.base.BaseApiTest;
import com.qa.dummyjson.config.ApiConfig;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Epic("DummyJSON API")
@Feature("Products - GET")
@Tag("api")
@Tag("smoke")
class ProductsGetTest extends BaseApiTest {

    @Test
    @DisplayName("GET /products devuelve 200, cumple el esquema y responde dentro del SLA")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Valida código de estado, contrato JSON y tiempo de respuesta del listado.")
    void getAllProducts() {
        given().spec(spec)
        .when()
                .get("/products")
        .then()
                .statusCode(200)
                .time(lessThan(ApiConfig.MAX_RESPONSE_MS))
                .body(matchesJsonSchemaInClasspath("schemas/product-list.json"))
                .body("products", hasSize(30))       // límite por defecto de la API
                .body("limit", equalTo(30))
                .body("total", greaterThan(0));
    }

    @Test
    @DisplayName("GET /products?limit&skip respeta la paginación")
    @Severity(SeverityLevel.CRITICAL)
    void getProductsWithPagination() {
        given().spec(spec)
                .queryParam("limit", 5)
                .queryParam("skip", 10)
        .when()
                .get("/products")
        .then()
                .statusCode(200)
                .time(lessThan(ApiConfig.MAX_RESPONSE_MS))
                .body(matchesJsonSchemaInClasspath("schemas/product-list.json"))
                .body("products", hasSize(5))
                .body("limit", equalTo(5))
                .body("skip", equalTo(10));
    }

    @ParameterizedTest(name = "GET /products/{0} devuelve el producto solicitado")
    @ValueSource(ints = {1, 10, 50, 100})
    @DisplayName("GET /products/{id} devuelve 200 y cumple el esquema")
    @Severity(SeverityLevel.CRITICAL)
    void getProductById(int id) {
        given().spec(spec)
                .pathParam("id", id)
        .when()
                .get("/products/{id}")
        .then()
                .statusCode(200)
                .time(lessThan(ApiConfig.MAX_RESPONSE_MS))
                .body(matchesJsonSchemaInClasspath("schemas/product.json"))
                .body("id", equalTo(id))
                .body("title", notNullValue());
    }

    @Test
    @DisplayName("GET /products/{id} inexistente devuelve 404 con mensaje de error")
    @Severity(SeverityLevel.NORMAL)
    void getProductByIdNotFound() {
        given().spec(spec)
        .when()
                .get("/products/999999")
        .then()
                .statusCode(404)
                .time(lessThan(ApiConfig.MAX_RESPONSE_MS))
                .body(matchesJsonSchemaInClasspath("schemas/error.json"))
                .body("message", containsString("not found"));
    }

    @Test
    @DisplayName("GET /products/search?q=phone devuelve resultados")
    @Severity(SeverityLevel.NORMAL)
    void searchProducts() {
        given().spec(spec)
                .queryParam("q", "phone")
        .when()
                .get("/products/search")
        .then()
                .statusCode(200)
                .time(lessThan(ApiConfig.MAX_RESPONSE_MS))
                .body(matchesJsonSchemaInClasspath("schemas/product-list.json"))
                .body("total", greaterThan(0))
                .body("products.size()", greaterThan(0));
    }

    @Test
    @DisplayName("GET /products/categories devuelve una lista no vacía")
    @Severity(SeverityLevel.MINOR)
    void getCategories() {
        given().spec(spec)
        .when()
                .get("/products/categories")
        .then()
                .statusCode(200)
                .time(lessThan(ApiConfig.MAX_RESPONSE_MS))
                .body("size()", greaterThan(0));
    }
}
