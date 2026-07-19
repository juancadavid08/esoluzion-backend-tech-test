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

## Scripts (equivalentes)

No se usa `package.json` en backend Java, pero los comandos equivalentes son:

- START: `mvn spring-boot:run`
- BUILD: `mvn clean package`
- TEST: `mvn test`
- LINT: se puede incorporar con Checkstyle/SpotBugs en siguiente hito
