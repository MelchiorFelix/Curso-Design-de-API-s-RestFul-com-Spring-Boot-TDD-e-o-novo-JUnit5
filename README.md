# Library API

A Spring Boot REST API for managing library books and loans, developed with
test-driven development. The repository focuses on the library domain and
contains one application module: `library-api`.

## Requirements

- JDK 17 or later, with `JAVA_HOME` configured.
- The included Maven Wrapper downloads Maven 3.9.16 and verifies its SHA-256 checksum.

The application uses Spring Boot 4.1.1, Spring MVC, Spring Data JPA, Jakarta
Validation, ModelMapper, and an embedded H2 database. Tests use Spring Boot's
managed JUnit Jupiter, Mockito, and AssertJ dependencies.

## Build and test

From the repository root on Windows:

```powershell
.\mvnw.cmd -B -ntp clean verify
```

On Linux or macOS:

```sh
./mvnw -B -ntp clean verify
```

The root Maven build includes only `library-api`. Its 40 tests cover controllers,
services, repositories, and the full application context.

## Run locally

```powershell
.\mvnw.cmd -pl library-api spring-boot:run
```

On Linux or macOS, use `./mvnw -pl library-api spring-boot:run`. After building,
you can also run the packaged application:

```sh
java -jar library-api/target/library-api-0.0.1-SNAPSHOT.jar
```

The API listens on `http://localhost:8080`. The default H2 database is in memory,
so data is lost when the application stops. Authentication and authorization
are not implemented; this is a learning project.

## API

| Method | Path | Purpose |
| --- | --- | --- |
| POST | `/api/books` | Create a book with a title, author, and unique ISBN |
| GET | `/api/books` | Filter books by their properties, with pagination |
| GET | `/api/books/{id}` | Retrieve a book |
| PUT | `/api/books/{id}` | Update a book's title and author using request parameters |
| DELETE | `/api/books/{id}` | Delete a book |
| GET | `/api/books/{id}/loans` | List loans for a book, with pagination |
| POST | `/api/loans` | Create a loan using an ISBN and customer name; returns the loan ID |
| GET | `/api/loans` | Find loans by ISBN or customer, with pagination |
| PATCH | `/api/loans/{id}` | Set the loan's returned status |

Paginated requests accept `page` (starting at zero) and `size`. A book with an
outstanding loan cannot be loaned again. Business and validation errors return
HTTP 400 with an `errors` array of English messages.

For example, using curl in a Bash-compatible shell:

```sh
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{"title":"The Adventures","author":"Alex Smith","isbn":"9780000000001"}'

curl -X POST http://localhost:8080/api/loans \
  -H "Content-Type: application/json" \
  -d '{"isbn":"9780000000001","customer":"Sam Taylor"}'

curl -X PATCH http://localhost:8080/api/loans/1 \
  -H "Content-Type: application/json" \
  -d '{"returned":true}'
```

Replace `1` in the last request with the loan ID returned by the previous request.

## Dependency checks and CI

Generate the resolved dependency tree, then query OSV using PowerShell 7:

```powershell
.\mvnw.cmd -B -ntp org.apache.maven.plugins:maven-dependency-plugin:3.11.0:tree '-DoutputFile=target/dependencies.json' '-DoutputType=json'
pwsh -File scripts/Test-Dependencies.ps1
```

The script sends public Maven package names and versions to OSV, including
transitive and test dependencies. It writes `target/osv-dependencies.json` and
fails if vulnerabilities are found or the query cannot be completed. It does not
scan source code, build plugins, or components embedded inside other JARs.

GitHub Actions builds and tests the library API on Java 17 and 21, starts the
packaged application for an HTTP smoke test, and runs the OSV check on Java 17.
The workflow runs on pushes and pull requests to `main` and `development`, and
weekly. Dependabot tracks Maven dependencies and GitHub Actions.

The [dependency migration report](docs/security-update-2026-09-23.md) records the
earlier security update, before the introductory course modules were removed.

## Development

Target `development` when opening feature pull requests. `main` is the default
branch. Use English for code identifiers, comments, test descriptions,
documentation, and API messages.
