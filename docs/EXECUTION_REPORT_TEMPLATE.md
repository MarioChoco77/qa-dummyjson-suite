# Reporte de ejecución

> Este documento se completa con los datos de **una corrida real** del pipeline
> (pestaña *Actions* → ejecución → artefacto `allure-report`). Los números los genera Allure;
> aquí solo se consolidan para quien no abre el reporte interactivo.

| Campo | Valor |
|---|---|
| Fecha / hora (UTC) | |
| Commit / rama | |
| Disparador | push / PR / schedule / manual |
| URL de la ejecución | |
| Ambiente | DummyJSON (`https://dummyjson.com`) · SauceDemo (`https://www.saucedemo.com`) |

## 1. Resumen

| Suite | Total | Aprobadas | Fallidas | Omitidas | % éxito | Duración |
|---|---|---|---|---|---|---|
| API (REST Assured) | 20 | | | | | |
| E2E (Playwright) | 14 | | | | | |
| **Total** | **34** | | | | | |

**Veredicto contra criterios de salida (TEST_PLAN §7):** ☐ Aprobado  ☐ Rechazado

## 2. Tiempos de respuesta de API

| Endpoint | Promedio (ms) | Máx. (ms) | SLA (ms) | Cumple |
|---|---|---|---|---|
| GET /products | | | 3000 | |
| POST /products/add | | | 3000 | |
| PUT /products/{id} | | | 3000 | |
| DELETE /products/{id} | | | 3000 | |
| POST /auth/login | | | 3000 | |

## 3. Fallos y análisis

| Prueba | Error | Causa raíz | Acción | Ticket |
|---|---|---|---|---|
| | | | | |

## 4. Observaciones

(Pruebas inestables, cambios detectados en el servicio público, mejoras propuestas.)
