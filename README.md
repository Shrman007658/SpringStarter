# SpringStarter

SpringStarter is a multi-module starter project for Spring-based services. This repository is a parent POM (packaging = pom) that aggregates four modules and centralizes some common build configuration and dependencies. The project targets Java 21 and Spring Boot 4.0.1.

This README explains the purpose of each module, enumerates the dependencies and build plugins declared in the root POM, provides detailed local run instructions, documents the design decisions and assumptions made for this starter, and proposes a practical roadmap.

---

Table of contents
- Project structure
- Modules (what each module does)
- Root-level dependencies (exact declarations from the root POM)
- Root-level build plugins (exact declarations from the root POM)
- How to run (local)
- Tests
- Design decisions
- Assumptions & trade-offs
- Future roadmap
- Contributing
- License

---

Project structure (root POM)
- groupId: `com.starter`
- artifactId: `SpringStarter`
- version: `0.0.1-SNAPSHOT`
- packaging: `pom`
- Spring Boot parent: `org.springframework.boot:spring-boot-starter-parent:4.0.1`
- java.version: `21`
- Declared modules:
    - `SpringStarterCommon`
    - `SpringStarterBusiness`
    - `SpringStarterService`
    - `SpringStarterApi`

Because the root packaging is `pom`, the runnable Spring Boot application is expected to be inside one of the child modules — in this case `SpringStarterService`.

---

Modules and responsibilities

- SpringStarterCommon
    - Purpose: Hold shared domain types (POJOs), common DTOs, shared constants, utility classes, and any code that is reused across multiple modules.
    - Typical contents: validation annotations / handlers, global exception types, common mapping utilities, and light-weight third-party helper wrappers.
    - Dependency guidance: keep this module minimal and avoid bringing heavy runtime dependencies so it remains easily reusable.

- SpringStarterBusiness
    - Purpose: Contain core business/domain logic and use-case implementations that are independent of transport and persistence details.
    - Typical contents: domain services, business validators, policy classes, and business-level interfaces consumed by the service layer.
    - Dependency guidance: depends on SpringStarterCommon but should not depend on web specifics.

- SpringStarterService
    - Purpose: Implement service orchestration, cross-cutting concerns (retries, bulkheads if used), external integrations (HTTP clients, messaging), and transactional composition across repositories and business logic.
    - Typical contents: service implementations that coordinate repositories, external clients, and business logic.
    - Dependency guidance: may contain configuration for HTTP clients, integration tests, and any circuit-breaker/observability hooks.

- SpringStarterApi
    - Purpose: Expose REST controllers, request/response mapping, API versioning, the Spring Boot `main` class, actuator configuration, and API-level configuration.
    - Typical contents: controllers, Spring Boot application entry point, OpenAPI/Swagger config (if present), controller advices for error handling, and API-specific security config.
    - Run target: this is the module you should run locally (see How to run).

Note: The module names above are taken exactly from the root POM. Each child module is expected to have its own `pom.xml` that declares module-scoped dependencies and the main application class (for SpringStarterApi).

---

Root-level dependencies (copied from the root POM)
These dependencies are declared at the parent level of the project and available to child modules unless overridden in module POMs:

- org.springframework.boot:spring-boot-micrometer-tracing-brave
- org.springframework.boot:spring-boot-starter-actuator

(Commented-out in POM)
- org.springframework.boot:spring-boot-starter-data-jpa
    - Note: This dependency is commented out in the root POM with the comment "Disabled for the time being because we dont need Database".

- org.springframework.boot:spring-boot-starter-validation
- org.springframework.boot:spring-boot-starter-webmvc
- io.micrometer:micrometer-tracing-bridge-brave

Development / optional dependencies:
- org.springframework.boot:spring-boot-devtools (scope: runtime, optional: true)
- org.projectlombok:lombok (optional: true)

Test dependencies (scope: test):
- org.springframework.boot:spring-boot-micrometer-tracing-test (test)
- org.springframework.boot:spring-boot-restdocs (test)
- org.springframework.boot:spring-boot-starter-actuator-test (test)
- org.springframework.boot:spring-boot-starter-data-jpa-test (test)
- org.springframework.boot:spring-boot-starter-validation-test (test)
- org.springframework.boot:spring-boot-starter-webmvc-test (test)
- org.springframework.restdocs:spring-restdocs-mockmvc (test)

Notes about dependencies:
- Micrometer / Brave: The project includes both `spring-boot-micrometer-tracing-brave` and `micrometer-tracing-bridge-brave` for distributed tracing / observability integration using Brave.
- Validation: `spring-boot-starter-validation` is included at the root so DTO validation support is available.
- Web MVC: `spring-boot-starter-webmvc` provides the MVC stack (servlet-based) — controllers and MockMvc tests are expected.
- Lombok is declared optional and included in the annotation processor configuration to reduce boilerplate when enabled locally.
- The data/JPA dependency is intentionally commented out because the current starter configuration does not require a database.

---

Root-level build plugins

- maven-compiler-plugin
    - Configured annotationProcessorPaths to reference Lombok:
        - annotationProcessorPaths -> path -> org.projectlombok:lombok

- spring-boot-maven-plugin
    - Configuration:
        - `<skip>true</skip>` (the root plugin is configured to skip; child modules that are executable should configure the plugin to package or run)
        - excludes: excludes org.projectlombok:lombok from the repackage step

Notes:
- The root `spring-boot-maven-plugin` is configured to skip repackaging at the parent level; executable child modules (like `SpringStarterApi`) should enable or configure `spring-boot-maven-plugin` in their POM if you want `mvn package` to produce runnable jars from the child modules.
- The asciidoctor plugin references `${spring-restdocs.version}` — if the property is not set in parent or child POM(s), the build that touches the plugin's dependency resolution must supply it.

---

How to run (locally)

Prerequisites
- JDK 21 (the root POM sets `<java.version>21</java.version>`)
- Maven (the repository is expected to include the Maven wrapper `mvnw`; if not, use an installed Maven 3.6+)
- Git
- (Optional) Docker & Docker Compose if you later enable DB or other services

Clone and build
1. Clone the repository:
   ```
   git clone https://github.com/Shrman007658/SpringStarter.git
   cd SpringStarter
   ```

2. Build the full multi-module project:
   ```
   ./mvnw clean install
   ```
   This builds all modules (Common, Business, Service, Api). If you rely on the Maven wrapper, use `./mvnw`. If you use a local Maven installation, use `mvn`.

Run the API module (SpringStarterApi)
- The parent POM is packaging = pom; run the Spring Boot application in the `SpringBootService` module:

Running tests
- Run the whole test suite:
  ```
  ./mvnw test
  ```
- Run tests for a specific module (example: SpringStarterService):
  ```
  ./mvnw -pl SpringStarterService -am test
  ```

Generating docs
- The root POM configures `asciidoctor-maven-plugin` to run in `prepare-package`. If you use that plugin to generate API docs from tests (Spring REST Docs), ensure the property `${spring-restdocs.version}` is defined (root or child POM) and that the child modules run the integration tests which generate snippets used by asciidoctor.

---

Design decisions (why the code is structured this way)

1. Multi-module parent POM
    - Reason: Provides clear separation of concerns (common, business logic, integration/service orchestration, API) and allows teams to evolve parts of the system independently. It also reduces artifact size for consumers who only need specific modules.

2. Parent-managed dependencies for cross-cutting concerns
    - Reason: Centralizing dependencies like Micrometer tracing, actuator and webmvc at the parent level enforces consistent versions across modules and reduces duplication in child POMs.

3. Observability enabled by default
    - Reason: Including Micrometer + Brave at the parent level ensures that tracing and metrics are first-class concerns for any service bootstrapped from this starter.

4. Validation and Web MVC roots
    - Reason: `spring-boot-starter-validation` and `spring-boot-starter-webmvc` are present at root because typical microservices expose endpoints and require request validation; putting them at the parent level makes DTO validation and controllers uniformly available.

5. JPA commented out in the parent
    - Reason: The root POM intentionally comments out `spring-boot-starter-data-jpa` with a note indicating that database is not needed at present. This keeps the parent lean while allowing child modules to opt-in to persistence if/when required.

6. Documentation automation
    - Reason: The `asciidoctor-maven-plugin` inclusion suggests the project expects REST Docs-driven documentation. This enables reproducible doc generation from test snippets.

---

Assumptions & trade-offs

Assumptions
- SpringStarterApi contains the Spring Boot `main` entry point and is the intended execution module for local runs.
- Child modules declare appropriate module-scoped dependencies (e.g., if you want data persistence you should enable `spring-boot-starter-data-jpa` in the child that requires it or in the parent if all modules need it).
- The property `${spring-restdocs.version}` referenced by the asciidoctor plugin will be defined in a POM (root or child) before performing doc generation builds that need it.

Trade-offs
- Multi-module architecture increases initial complexity versus a single-module project. The benefit is better separation and faster incremental builds, but it requires more explicit module management (dependency scope, inter-module references).
- Parent-level dependency declarations reduce duplication but risk dragging unnecessary artifacts into child modules if not carefully managed. To mitigate this, heavy or optional dependencies (devtools, Lombok) are marked optional or scoped appropriately.
- The root `spring-boot-maven-plugin` is set to skip packaging in the parent. This avoids accidental repackaging at the parent level but requires explicit configuration in child modules to produce runnable jars.

---

Future roadmap (concrete next steps)
Short term
- Add an explicit property for `spring-restdocs.version` in the root POM to let asciidoctor plugin resolve its dependency.
- Add a simple `README`-level example endpoint in `SpringStarterApi` along with example curl commands.

Medium term
- Add optional `SpringStarterPersistence` (or enable JPA in a child) and example Flyway or Liquibase migrations.
- Add OpenAPI (springdoc) configuration and an automatically generated API spec.
- Add CI pipeline (GitHub Actions) for build, test, and code scan.

Long term
- Publish reusable modules (e.g., `SpringStarterCommon`) to an internal artifact repository (Nexus/Artifactory).
- Add sample microservice templates (with auth, DB, messaging) demonstrating different integrations and patterns.

---

Contributing
- Fork the repo and open a PR for any non-trivial change.

