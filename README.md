# Esoluzion Backend Tech Test

Implementacion del endpoint requerido por la prueba:

- `GET /product/{productId}/similar`

## Stack

- Java 17
- Spring Boot 3
- Maven

## Comportamiento

- Expone la API en el puerto `5000`.
- Consulta API existente en `http://localhost:3001` para:
  - `GET /product/{productId}/similarids`
  - `GET /product/{id}`
- Responde una lista de `ProductDetail` ordenada por similitud.
- Estrategia de resiliencia:
  - Obtencion de detalles en paralelo.
  - Timeout por detalle.
  - Si un detalle falla (`404`, `500` o timeout), se omite y la respuesta sigue.

## Configuracion

Archivo: `src/main/resources/application.yml`

- `server.port: 5000`
- `external.api.base-url: http://localhost:3001`
- `management.endpoints.web.exposure.include: health,info,prometheus`
- `similar-products.*` para tuning de concurrencia y timeouts

## Ejecucion local

```bash
cd "d:\PROYECTOS\PRUEBA ESoluzion\back"
mvn spring-boot:run
```

## Build y test

```bash
mvn clean test
mvn clean package
```

## Observabilidad

Con Actuator habilitado:

- `GET /actuator/health`
- `GET /actuator/info`
- `GET /actuator/prometheus`

Esto permite monitorear salud del servicio y exponer metricas para scraping.

## OpenAPI / Swagger (API-first)

Se publico el contrato OpenAPI en YAML y se habilito Swagger UI:

- Contrato: `/openapi/similar-products.yaml`
- Swagger UI: `/swagger-ui.html`
- API docs JSON (springdoc): `/v3/api-docs`

Con esto se puede validar visualmente el endpoint y mantener el enfoque API-first.

## Postman Collection

Se incluye una collection de Postman lista para importar:

- `docs/postman/Esoluzion-Backend-Tech-Test.postman_collection.json`

Incluye requests para:

- Endpoint principal: `GET /product/{productId}/similar`
- Observabilidad: `health`, `info`, `prometheus`
- API-first: contrato YAML y Swagger UI

Variables incluidas en la collection:

- `baseUrl` (default: `http://localhost:5000`)
- `productId` (default: `1`)

## Benchmark local (k6 + mocks)

El proyecto `backendDevTest` incluye infraestructura de benchmark y mocks.

Pasos sugeridos:

```bash
cd "d:\PROYECTOS\PRUEBA ESoluzion\backendDevTest"
docker-compose up -d simulado influxdb grafana
```

Validar mocks:

```bash
curl http://localhost:3001/product/1/similarids
```

Con este backend corriendo en `5000`, lanzar benchmark:

```bash
docker-compose run --rm k6 run scripts/test.js
```

Dashboard de resultados:

- `http://localhost:3000/d/Le2Ku9NMk/k6-performance-test`

## CI

Se incluyo workflow en GitHub Actions:

- `.github/workflows/backend-ci.yml`
- Ejecuta `mvn -B clean test` en cada `push` y `pull_request` a `main`.

## Scripts (equivalentes)

No se usa `package.json` en backend Java, pero los comandos equivalentes son:

- START: `mvn spring-boot:run`
- BUILD: `mvn clean package`
- TEST: `mvn test`
- LINT: se puede incorporar con Checkstyle/SpotBugs en siguiente hito

## Hito 2

- Migracion a Java 17 + Spring Boot 3.
- Pruebas de gateway HTTP con `MockWebServer` para validar parsing y resiliencia.
- Prueba adicional de controller para respuesta `404`.
- CI automatizado con GitHub Actions.

## Hito 3

- Servicio de similares configurable por propiedades:
  - `similar-products.detail-timeout-ms`
  - `similar-products.max-similar-ids`
  - `similar-products.pool-size`
  - `similar-products.queue-capacity`
- Executor dedicado para obtencion de detalles en paralelo.
- Normalizacion de IDs similares (filtro de vacios, deduplicacion y limite por request).
- Cobertura adicional de tests para limite/deduplicacion y caso sin IDs similares.

## Hito 4

- Observabilidad: Spring Boot Actuator + endpoint Prometheus.
- Endpoint de salud para readiness/liveness (`/actuator/health`).
- Test de integracion que valida la disponibilidad del health endpoint.
- Documentacion para ejecutar benchmark local con mocks y k6.

## Hito 5

- Cobertura adicional de ramas de error en gateway:
  - `404` -> `ProductNotFoundException`
  - `500` inesperado -> `IllegalStateException`
- Cobertura adicional en service:
  - filtrado de `null` y `""`
  - caso sin IDs similares
- Documentacion consolidada de la estrategia de resiliencia.
