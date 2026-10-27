# Plan de Pruebas — Suite de automatización DummyJSON + SauceDemo

| | |
|---|---|
| **Versión** | 1.0 |
| **Tipo de documento** | Plan de pruebas (IEEE 829 simplificado) |
| **Repositorio** | `qa-dummyjson-suite` |
| **Responsable** | QA Automation |

## 1. Objetivo

Verificar de forma automatizada y repetible que:

1. La API pública **DummyJSON** cumple su contrato funcional (códigos de estado, estructura JSON) y un SLA de tiempo de respuesta en sus operaciones de lectura, escritura y autenticación.
2. Los flujos críticos de usuario de una tienda en línea (**login, navegación y checkout**) funcionan de extremo a extremo en navegador.
3. Cada cambio en el código dispare la suite, genere evidencia (reportes) y notifique el resultado al equipo.

## 2. Alcance

### 2.1 Dentro del alcance

| Capa | Funcionalidad | Cobertura |
|---|---|---|
| API | `GET /products`, `/products/{id}`, `/products/search`, `/products/categories` | 200, esquema, paginación, búsqueda, 404 |
| API | `POST /products/add` | Creación simulada, id asignado, esquema |
| API | `PUT /products/{id}` | Actualización simulada, 404 en id inexistente |
| API | `DELETE /products/{id}` | `isDeleted=true`, `deletedOn`, 404 |
| API | `POST /auth/login`, `GET /auth/me` | Login válido/ inválido, token válido/ inválido |
| API (no funcional) | Todas las anteriores | Tiempo de respuesta < `maxResponseMs` (3000 ms por defecto) |
| E2E | Login (SauceDemo) | Éxito, credenciales inválidas, usuario bloqueado, campos vacíos, logout, acceso sin sesión |
| E2E | Navegación | Ordenamiento, detalle de producto, contador de carrito |
| E2E | Checkout | Flujo completo, validaciones de formulario, cancelación, coherencia de importes |

### 2.2 Fuera del alcance

- Pruebas de carga, estrés y seguridad (pentesting).
- Pruebas en navegadores distintos de Chromium y en dispositivos móviles (se documenta como mejora, ver §10).
- Endpoints de DummyJSON no listados arriba (`/carts`, `/users`, `/posts`, `/recipes`, etc.).
- Validación de persistencia: **DummyJSON simula las escrituras y no guarda datos**; se valida la *respuesta*, no el estado posterior.
- Accesibilidad y localización.

> **Nota sobre las herramientas bajo prueba:** DummyJSON es únicamente una API (no tiene interfaz web). Para cumplir el requisito de E2E se usa **SauceDemo** (https://www.saucedemo.com), sitio público diseñado para practicar automatización de login, catálogo y checkout.

## 3. Ambientes

| Ambiente | Sistema bajo prueba | URL | Uso |
|---|---|---|---|
| Local (dev QA) | DummyJSON / SauceDemo | `https://dummyjson.com`, `https://www.saucedemo.com` | Desarrollo y depuración de pruebas |
| CI (GitHub Actions) | DummyJSON / SauceDemo | Igual que local | Ejecución automática en cada push/PR y corrida diaria |

Ambos sistemas son servicios públicos de terceros: **no existe ambiente propio ni staging**. Las URLs son configurables (`-DbaseUri=...` para API, variable `BASE_URL` para E2E), por lo que la misma suite puede apuntar a un mirror o ambiente propio.

**Configuración de ejecución en CI:** runner `ubuntu-latest`, Java 17 (Temurin), Node.js 20, Chromium (instalado por Playwright).

## 4. Herramientas

| Propósito | Herramienta | Versión |
|---|---|---|
| Pruebas de API | REST Assured | 5.5.0 |
| Validación de contrato | json-schema-validator (JSON Schema draft-04) | 5.5.0 |
| Framework de ejecución (API) | JUnit 5 | 5.11.3 |
| Build | Maven + Surefire | 3.5.1 |
| Pruebas E2E | Playwright (TypeScript) | ^1.48 |
| Patrón E2E | Page Object Model + fixtures | — |
| Reportes | Allure (API + E2E unificado) y Playwright HTML Report | 2.29 / 2.15 |
| CI/CD | GitHub Actions | — |
| Notificaciones | Slack Incoming Webhook + GitHub Job Summary | — |
| Control de versiones | Git / GitHub | — |

## 5. Estrategia de pruebas

- **Pirámide:** la mayor parte de la cobertura vive en API (rápida, estable); el E2E se limita a los flujos críticos de negocio.
- **Independencia:** cada prueba es autónoma y no depende del orden de ejecución ni del estado de otra.
- **Datos:** usuarios y credenciales públicas de las propias plataformas (`emilys / emilyspass`; `standard_user / secret_sauce`). Sin datos personales reales.
- **Validación en tres niveles (API):** (1) código de estado, (2) esquema JSON, (3) tiempo de respuesta, más aserciones de negocio puntuales.
- **Etiquetas:** `@Tag("smoke")`/`@Tag("regression")` en API y `@smoke` en E2E permiten ejecutar subconjuntos.
- **Reintentos:** E2E reintenta 2 veces en CI para absorber inestabilidad de red; los fallos que persisten se reportan con trace, captura y video. Las pruebas de API **no** se reintentan, para no ocultar defectos.

## 6. Criterios de entrada

La ejecución puede iniciar cuando:

1. El código compila (`mvn compile`) y `npm run typecheck` pasa sin errores.
2. Los sistemas bajo prueba responden (HTTP 200 en `https://dummyjson.com/products/1` y `https://www.saucedemo.com`).
3. Las dependencias se resuelven (Maven Central y npm disponibles).
4. Los secretos/variables requeridos están configurados (solo para notificaciones; su ausencia no bloquea la ejecución).

## 7. Criterios de salida (aceptación del build)

| Criterio | Umbral |
|---|---|
| Pruebas **smoke** (API y E2E) aprobadas | 100 % |
| Pruebas totales aprobadas | ≥ 95 %, sin fallos en casos de severidad *blocker* o *critical* |
| Defectos abiertos de severidad crítica/alta | 0 |
| Tiempo de respuesta de API | 100 % de las llamadas < 3000 ms (parametrizable) |
| Reporte Allure generado y publicado como artefacto | Sí |
| Notificación enviada | Sí (si el webhook está configurado) |

**Criterios de suspensión:** se detiene la ejecución cuando el sistema bajo prueba no está disponible (errores 5xx / timeouts generalizados) o cuando falla la instalación de dependencias. Se reanuda al restablecerse el servicio.

## 8. Gestión de defectos y flaky tests

- Un fallo se analiza con el reporte Allure (request/response adjuntos) o el trace de Playwright.
- Si se origina en un cambio de comportamiento del servicio público, se actualiza la aserción y se deja constancia en el historial de Git.
- Una prueba que falla de forma intermitente se marca y se investiga; no se desactiva sin ticket asociado.

## 9. Riesgos y mitigaciones

| Riesgo | Prob. | Impacto | Mitigación |
|---|---|---|---|
| Servicios públicos de terceros cambian su comportamiento sin aviso | Media | Alto | Corrida diaria programada para detectar la deriva pronto |
| Indisponibilidad o latencia de DummyJSON / SauceDemo | Media | Medio | Reintentos en E2E, umbral de tiempo configurable, criterio de suspensión |
| Rate limiting al ejecutar en paralelo | Baja | Medio | Pocas llamadas por corrida; workers limitados a 2 en CI |
| Escrituras simuladas ocultan defectos de persistencia | Alta (por diseño) | Bajo | Documentado en alcance; no se afirman garantías de persistencia |
| Tiempos de respuesta variables en runners compartidos | Media | Bajo | SLA holgado (3 s) y configurable |

## 10. Mejoras futuras

Matriz multi-navegador (Firefox, WebKit) y móvil; historial de tendencias en Allure; pruebas de contrato con Pact; pruebas de carga con k6; cobertura de `/carts` y `/users`.

## 11. Entregables

Código fuente (`api-tests/`, `e2e-tests/`), este plan, workflow de CI, reporte Allure (artefacto `allure-report`), reporte HTML de Playwright, XML de Surefire y el informe de ejecución (`docs/EXECUTION_REPORT_TEMPLATE.md`).

## 12. Matriz de trazabilidad

| ID | Requisito | Prueba(s) |
|---|---|---|
| R-01 | Listar productos con paginación | `ProductsGetTest.getAllProducts`, `getProductsWithPagination` |
| R-02 | Consultar un producto / manejar inexistente | `getProductById`, `getProductByIdNotFound` |
| R-03 | Buscar y listar categorías | `searchProducts`, `getCategories` |
| R-04 | Crear producto | `ProductsWriteTest.createProduct` |
| R-05 | Actualizar producto | `updateProduct`, `updateNonExistentProduct` |
| R-06 | Eliminar producto | `deleteProduct`, `deleteNonExistentProduct` |
| R-07 | Autenticación | `AuthTest.*` |
| R-08 | Rendimiento de API | `.time(lessThan(...))` en todas las pruebas de API |
| R-09 | Login de usuario (UI) | `login.spec.ts` |
| R-10 | Navegación del catálogo | `navigation.spec.ts` |
| R-11 | Compra completa (checkout) | `checkout.spec.ts` |
