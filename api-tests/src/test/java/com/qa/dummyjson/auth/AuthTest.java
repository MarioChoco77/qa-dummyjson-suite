package com.qa.dummyjson.auth;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;

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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@Epic("DummyJSON API")
@Feature("Auth")
@Tag("api")
@Tag("smoke")
class AuthTest extends BaseApiTest {

    private static Map<String, String> credentials(String user, String pass) {
        return Map.of("username", user, "password", pass);
    }

    @Test
    @DisplayName("POST /auth/login con credenciales válidas devuelve 200 y un accessToken")
    @Severity(SeverityLevel.BLOCKER)
    void loginSuccess() {
        given().spec(spec)
                .body(credentials(ApiConfig.USERNAME, ApiConfig.PASSWORD))
        .when()
                .post("/auth/login")
        .then()
                .statusCode(200)
                .time(lessThan(ApiConfig.MAX_RESPONSE_MS))
                .body(matchesJsonSchemaInClasspath("schemas/login-response.json"))
                .body("username", equalTo(ApiConfig.USERNAME))
                .body("accessToken", not(blankOrNullString()));
    }

    @ParameterizedTest(name = "login inválido [{0} / {1}] devuelve 400")
    @CsvSource({
            "emilys,wrongpassword",
            "usuario_inexistente,emilyspass",
            "usuario_inexistente,incorrecta"
    })
    @DisplayName("POST /auth/login con credenciales inválidas devuelve 400")
    @Severity(SeverityLevel.CRITICAL)
    void loginInvalidCredentials(String user, String pass) {
        given().spec(spec)
                .body(credentials(user, pass))
        .when()
                .post("/auth/login")
        .then()
                .statusCode(400)
                .time(lessThan(ApiConfig.MAX_RESPONSE_MS))
                .body(matchesJsonSchemaInClasspath("schemas/error.json"))
                .body("message", equalTo("Invalid credentials"));
    }

    @Test
    @DisplayName("GET /auth/me con token válido devuelve el usuario autenticado")
    @Severity(SeverityLevel.CRITICAL)
    void getCurrentUserWithToken() {
        String token = given().spec(spec)
                .body(credentials(ApiConfig.USERNAME, ApiConfig.PASSWORD))
                .post("/auth/login")
                .then().statusCode(200)
                .extract().path("accessToken");

        given().spec(spec)
                .header("Authorization", "Bearer " + token)
        .when()
                .get("/auth/me")
        .then()
                .statusCode(200)
                .time(lessThan(ApiConfig.MAX_RESPONSE_MS))
                .body("username", equalTo(ApiConfig.USERNAME));
    }

    @Test
    @DisplayName("GET /auth/me sin token o con token inválido es rechazado")
    @Severity(SeverityLevel.CRITICAL)
    void getCurrentUserWithInvalidToken() {
        given().spec(spec)
                .header("Authorization", "Bearer token.invalido.123")
        .when()
                .get("/auth/me")
        .then()
                .statusCode(anyOf(is(401), is(403)))
                .time(lessThan(ApiConfig.MAX_RESPONSE_MS))
                .body(matchesJsonSchemaInClasspath("schemas/error.json"));
    }
}
