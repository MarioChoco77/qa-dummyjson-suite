# qa-dummyjson-suite

Suite de automatización con **REST Assured (Java)** para la API pública [DummyJSON](https://dummyjson.com) y **Playwright (TypeScript)** para flujos E2E, integrada en **GitHub Actions** con reportes **Allure** y notificación a Slack.

```
qa-dummyjson-suite/
├── .github/workflows/ci.yml     # Pipeline: API → E2E → reporte Allure → notificación
├── api-tests/                   # Maven · REST Assured · JUnit 5 · Allure
│   ├── pom.xml
│   └── src/test
│       ├── java/com/qa/dummyjson/{config,base,products,auth}
│       └── resources/schemas/   # JSON Schemas (contratos)
├── e2e-tests/                   # Playwright · Page Objects · fixtures
│   ├── pages/  tests/  playwright.config.ts
└── docs/
    ├── TEST_PLAN.md             # Plan de pruebas
    └── EXECUTION_REPORT_TEMPLATE.md
```

## Qué se prueba

| Suite | Pruebas | Qué valida |
|---|---|---|
| API | 20 (contando casos parametrizados) | GET / POST / PUT / DELETE, login, **status code + JSON Schema + tiempo de respuesta** |
| E2E | 14 | Login, navegación (ordenar, detalle, carrito) y checkout completo |

> **Importante:** DummyJSON es solo API y **simula** las escrituras (POST/PUT/DELETE responden bien pero no guardan datos). Como no tiene interfaz web, el E2E se ejecuta contra [SauceDemo](https://www.saucedemo.com), tienda pública hecha para practicar login/checkout.

## Ejecutar en local

Requisitos: Java 17+, Maven 3.9+, Node.js 20+.

```bash
# --- API ---
cd api-tests
mvn clean verify                          # corre todo
mvn verify -Dgroups=smoke                 # solo smoke
mvn verify -DmaxResponseMs=1500           # SLA más estricto
mvn verify -DbaseUri=https://otro-host    # otro ambiente

# --- E2E ---
cd e2e-tests
npm ci
npx playwright install chromium
npx playwright test                       # headless
npx playwright test --headed              # viendo el navegador
npx playwright test --grep @smoke         # solo smoke
npx playwright show-report                # reporte HTML
```

### Ver el reporte Allure en local
```bash
npm install -g allure-commandline
# Une los resultados de ambas suites
mkdir -p allure-results
cp -r api-tests/target/allure-results/* allure-results/
cp -r e2e-tests/allure-results/*        allure-results/
allure serve allure-results
```

## Pipeline de CI (`.github/workflows/ci.yml`)

```
push / PR / cron / manual
        │
   ┌────┴─────┐
 api-tests  e2e-tests       (en paralelo)
   └────┬─────┘
      report                (une resultados → Allure HTML → artefacto)
     ┌──┴───┐
 deploy-pages  notify       (Pages opcional · Slack + Job Summary)
```

| Job | Qué hace |
|---|---|
| `api-tests` | Java 17 + caché Maven, `mvn verify`, sube `allure-results` y XML de Surefire |
| `e2e-tests` | Node 20, `npm ci`, instala Chromium, ejecuta Playwright, sube resultados y traces si falla |
| `report` | Se ejecuta **siempre** (`if: always()`), fusiona ambos `allure-results` y genera un único reporte |
| `deploy-pages` | Publica el reporte en GitHub Pages (opcional) |
| `notify` | Escribe un resumen en la ejecución y avisa por Slack |

### Configuración inicial (una sola vez)

1. **Subir el repo** a GitHub (`git init`, `git add .`, `git commit`, `git remote add origin ...`, `git push`).
2. **Notificaciones Slack (opcional):** en Slack crea una *Incoming Webhook* (api.slack.com/apps → Incoming Webhooks) → en GitHub: *Settings → Secrets and variables → Actions → New repository secret* → nombre `SLACK_WEBHOOK_URL`. Si no existe, el paso se omite sin fallar.
3. **Reporte publicado como sitio web (opcional):** *Settings → Pages → Source: GitHub Actions*, y crea la variable de repositorio `ENABLE_PAGES = true` (*Settings → Secrets and variables → Actions → Variables*). Solo se publica desde `main`.
4. Haz un push: la primera ejecución aparece en la pestaña **Actions**. Descarga el artefacto `allure-report` (o abre la URL de Pages).

### Dónde ver los resultados
- **Resumen rápido:** pestaña *Actions* → ejecución → *Summary*.
- **Reporte Allure:** artefacto `allure-report` (abrir con `allure open` o un servidor estático; no funciona abriendo `index.html` con doble clic) o la URL de Pages.
- **Reporte Playwright con traces:** artefacto `playwright-report`.

## Decisiones de diseño

- **Esquemas en draft-04:** es la versión que soporta el validador de REST Assured.
- **POST `/products/add` acepta 200 o 201:** la documentación de DummyJSON no fija el código, así que la aserción tolera ambos y valida el cuerpo con rigor. Si el servicio se estabiliza, conviene fijar un único valor.
- **Sin reintentos en API:** un reintento ocultaría defectos reales; en E2E sí hay (2, solo en CI).
- **Allure vía `npm install -g allure-commandline`:** evita depender de una acción de terceros para generar el reporte.

## Documentación
- Plan de pruebas: [`docs/TEST_PLAN.md`](docs/TEST_PLAN.md)
- Plantilla de reporte de ejecución: [`docs/EXECUTION_REPORT_TEMPLATE.md`](docs/EXECUTION_REPORT_TEMPLATE.md)
