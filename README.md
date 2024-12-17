# Artists Backend

A simple Spring Boot application that provides a RESTful API to retrieve and compare artists. This backend is configured to use an **in-memory database (H2)** by default, so no external database setup is required.

## Prerequisites

- **Java 17** (or a compatible JDK)
- **Maven 3.9+** (or use the Maven Wrapper included in the project)

## Getting Started

1. **Clone the repository** (not applicable in the zip file case):
2. Build and package the application:
   ```bash
   mvn clean install
    ```
3. Run the application:
   ```bash
   mvn spring-boot:run
    ```
4. Verify that the application is running by accessing the following URL in your browser:
   ```
   http://localhost:8080
   ```

## Using the API
**Retrieve Artist by ID:**
```
GET /artists/{artistId}
```

**List/Filter Albums by Artist:**
```
GET /artists/{artistId}/albums?genre=Rock&title=SomeTitle&year=1999&page=1&size=20
```

**Compare multiple artists:**
```
GET /artists/compare?artistIds=1001&artistIds=1002
```

## Additional Notes

*Things that I liked the most:*
- The use of Spring Boot and Spring Data JPA for rapid development.
- The use of Lombok to reduce boilerplate code.
- The use of JUnit 5 and Mockito for testing.
- The use of the H2 in-memory database for testing.
- JPA Specifications for dynamic query building on album search.

*Things that I would have loved to use:* 
- Swagger for API documentation.
- Spring Security for authentication and authorization.
- MapsStruct for mapping between entities and DTOs.
- Feign Client for consuming external APIs in an easier way, with an interceptor to inject the token on the fly.

*Things that I would never use:*
- Hardcoding the database schema in the application.
- Using the H2 in-memory database in production.
- Using the H2 Console in production.
- Using JPA Rest for exposing the repositories directly.

The application automatically creates and uses an H2 in-memory database on startup.
To inspect H2 data, you can use the H2 Console:
- Browse to http://localhost:8080/h2-console (if enabled).
  - JDBC URL: jdbc:h2:mem:artists 
  - User: sa
  - Password: (leave blank or sa, depending on config)

No database installation is needed; data is lost upon shutdown (unless configured otherwise).

Enjoy exploring the Artists Backend!