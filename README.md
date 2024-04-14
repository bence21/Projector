# Projector

## Auto-generated OpenAPI spec and Swagger Interactive API documentation

The entire API is documented according to the OpenAPI specification.
The spec is auto-generated from the source code and is available on the following URLs:

- **OpenAPI Spec (JSON)**: http://localhost:8080/openapi
- **OpenAPI Spec (YAML)**: http://localhost:8080/openapi.yaml
   - This can be used to generate client SDKs
     _e.g.:_ [Rapini](https://github.com/rametta/rapini) (React Query SDK generator)
- **Swagger UI (interactive API docs)**: http://localhost:8080/swagger-ui.html

## Dev DB (Docker-Compose)

There is a `docker-compose.yml` file in the root of the project that can be used to start a MySQL database
for development purposes.

0. Install Docker and Docker Compose
1. Run `docker-compose up -d` to start the database
2. Run the backend application with the `spring.jpa.hibernate.ddl-auto`
   application property (`./Projector-server/src/main/resources/application.properties`) set to `create`
  - **NOTE:** please do not commit this change, as it may bring some unintended consequences on the production database!
3. Kill the application (if it's not already killed by an error)
4. Reset the `spring.jpa.hibernate.ddl-auto` property to `none`
5. Run the application again normally

These steps are necessary because the database is not initialized with the necessary tables
when the application is started for the first time.
