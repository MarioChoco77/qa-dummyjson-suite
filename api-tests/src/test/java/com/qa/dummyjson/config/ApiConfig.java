package com.qa.dummyjson.config;

/**
 * Configuración central. Todo se puede sobrescribir con -Dpropiedad=valor,
 * lo que permite apuntar la misma suite a otro ambiente sin tocar código.
 */
public final class ApiConfig {

    private ApiConfig() {}

    public static final String BASE_URI =
            System.getProperty("baseUri", "https://dummyjson.com");

    /** SLA de tiempo de respuesta en milisegundos (criterio de aceptación no funcional). */
    public static final long MAX_RESPONSE_MS =
            Long.parseLong(System.getProperty("maxResponseMs", "3000"));

    /** Credenciales de usuario de prueba publicadas por DummyJSON. */
    public static final String USERNAME = System.getProperty("api.username", "emilys");
    public static final String PASSWORD = System.getProperty("api.password", "emilyspass");
}
