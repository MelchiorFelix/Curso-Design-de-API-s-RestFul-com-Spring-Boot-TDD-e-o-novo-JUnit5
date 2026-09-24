# Library API

A Spring Boot REST API for a library catalog, registered members, physical book
copies, and day-to-day lending. The repository contains one application module:
`library-api`.

## Features

- Register members, update contact details, search by name/card/email, and suspend
  or reactivate borrowing privileges.
- Track each physical copy with a unique barcode, shelf location, and availability.
  Multiple copies of one ISBN can circulate independently.
- Check out an available copy to a member, calculate a due date, renew within
  configured limits, and record returns.
- Browse member and book borrowing history, filter loans, and report overdue items.
- Enforce borrowing limits, block suspended or overdue members, and serialize
  concurrent lending operations to protect inventory and member limits.

The circulation scope is informed by workflows described in the
[Koha circulation manual](https://koha-community.org/manual/25.11/en/html/circulation.html).
The specific default rules below are this project's configurable choices.

## Requirements and build

Use JDK 17 or later with `JAVA_HOME` configured. The Maven Wrapper downloads Maven
3.9.16 and verifies its SHA-256 checksum. The application uses Spring Boot 4.1.1,
Spring MVC, Spring Data JPA, Jakarta Validation, ModelMapper, and H2.

From the repository root:

```powershell
.\mvnw.cmd -B -ntp clean verify
.\mvnw.cmd -pl library-api spring-boot:run
```

On Linux/macOS, replace `.\mvnw.cmd` with `./mvnw`. To run the packaged application:

```sh
java -jar library-api/target/library-api-0.0.1-SNAPSHOT.jar
```

The API listens on `http://localhost:8080`. H2 is in memory by default; data is
lost when the application stops. Authentication and authorization are not
implemented. This is a learning project, not a public-facing production service.

## Swagger UI and OpenAPI

Start the application, then open [Swagger UI](http://localhost:8080/swagger-ui.html)
to browse and try the API. Endpoints are organized under Books, Copies, Members,
and Loans, with request examples, validation requirements, pagination, and error
responses.

- [OpenAPI JSON](http://localhost:8080/v3/api-docs)
- [OpenAPI YAML](http://localhost:8080/v3/api-docs.yaml)

The documentation uses springdoc 3.1.1 for Spring Boot 4 and includes only
`/api/**` routes. The API version comes from the Maven project version. Swagger
UI uses the current server, so it also works when the application port changes.
To try checkout, create a member, a book, and a copy first, then use their IDs.
The **Try it out** actions execute real API requests against that running instance.

Configuration is in `library-api/src/main/resources/application.properties`.
Set `springdoc.api-docs.enabled=false` and `springdoc.swagger-ui.enabled=false`
to disable documentation in an environment.

## Lending rules

| Setting | Default | Meaning |
| --- | ---: | --- |
| `library.circulation.loan-days` | 14 | Days from checkout to the due date |
| `library.circulation.renewal-days` | 14 | Days added to the existing due date per renewal |
| `library.circulation.max-renewals` | 2 | Renewals allowed per loan; zero disables renewal |
| `library.circulation.max-active-loans` | 5 | Active loans allowed per member |
| `library.circulation.time-zone` | UTC | Time zone used for checkout, due dates, returns, and overdue reports |

Configure these in `library-api/src/main/resources/application.properties` or
through Spring Boot configuration overrides. For example:

```sh
java -jar library-api/target/library-api-0.0.1-SNAPSHOT.jar --library.circulation.loan-days=21 --library.circulation.time-zone=America/Rio_Branco
```

A loan becomes overdue the day after its due date. Any overdue loan blocks that
member's new checkouts and renewals until returned. Suspended members can still
return books. Repeating a return is safe: it preserves the original return date
and cannot release a copy that has subsequently been borrowed again.

Register copies explicitly after creating a book. Checkout accepts an optional
`copyId`; otherwise it selects the available copy with the lowest ID. Withdrawn
copies remain in inventory/history but cannot be loaned. A copy on loan cannot
be withdrawn. Books with inventory cannot be deleted.

Card numbers are trimmed, normalized to uppercase, and unique. Barcodes and ISBNs
are trimmed and unique (barcode matching is case-sensitive). A loan retains the
member's name at checkout as well as a link to their current member record.

## API

| Method | Path | Purpose |
| --- | --- | --- |
| POST | `/api/books` | Create a catalog record with title, author, and unique ISBN |
| GET | `/api/books` | Filter catalog records by their properties |
| GET | `/api/books/{id}` | Retrieve a book |
| PUT | `/api/books/{id}` | Update title and author from JSON; ISBN stays unchanged |
| DELETE | `/api/books/{id}` | Delete a book without inventory |
| GET | `/api/books/{id}/loans` | Browse a book's borrowing history |
| POST | `/api/books/{id}/copies` | Register a barcode and optional shelf location |
| GET | `/api/books/{id}/copies` | List copies; optional `status=AVAILABLE\|ON_LOAN\|WITHDRAWN` |
| POST | `/api/copies/{id}/withdraw` | Withdraw an available copy |
| POST | `/api/members` | Register card number, name, and email |
| GET | `/api/members` | Search using `query` and optional `active` |
| GET | `/api/members/{id}` | Retrieve a member |
| PUT | `/api/members/{id}` | Update card number, name, and email |
| PATCH | `/api/members/{id}` | Set `{"active":false}` or `{"active":true}` |
| GET | `/api/members/{id}/loans` | Browse a member's borrowing history |
| POST | `/api/loans` | Check out by ISBN and member ID; optional copy ID; returns the loan ID |
| GET | `/api/loans` | Filter by `isbn`, `customer`, `memberId`, `returned`, and `overdue` |
| GET | `/api/loans/{id}` | Retrieve loan dates, member, copy, and renewal count |
| GET | `/api/loans/overdue` | List outstanding loans whose due date has passed |
| PATCH | `/api/loans/{id}` | Return with `{"returned":true}` |
| POST | `/api/loans/{id}/renew` | Renew and return the updated loan details |

Lists are paginated using `page` (zero-based), `size`, and `sort`, for example
`/api/loans?returned=false&sort=dueDate,asc&page=0&size=20`. Loan filters combine
with AND; no filters lists all loans. The `customer` filter is a case-insensitive
exact match against the checkout name snapshot. Use `memberId` for a member's
complete history, including after name changes.

Business and validation errors return HTTP 400 with an English `errors` array.
Missing records return 404. Database uniqueness conflicts and lock contention
return 409; a lock-contention response can be retried.

See [the circulation walkthrough](docs/circulation.md) for a complete executable
PowerShell example and compatibility notes.

## Verification and dependency checks

The test suite covers controllers, services, repositories, complete HTTP/database
workflows, date boundaries, and concurrent checkouts. GitHub Actions runs Java 17
and 21 builds and tests the packaged server through registration, inventory,
checkout, renewal, return, and history. The smoke test script creates synthetic
records and is intended for a disposable development database:

```powershell
pwsh -File scripts/Test-Circulation.ps1 -BaseUrl http://localhost:8080
```

Generate a resolved dependency tree, then query OSV using PowerShell 7:

```powershell
.\mvnw.cmd -B -ntp org.apache.maven.plugins:maven-dependency-plugin:3.11.0:tree '-DoutputFile=target/dependencies.json' '-DoutputType=json'
pwsh -File scripts/Test-Dependencies.ps1
```

The scanner sends public Maven names and versions to OSV, including transitive
and test dependencies. It writes `target/osv-dependencies.json` and fails on known
vulnerabilities or an incomplete query. Source code, build plugins, and shaded
components are outside that scan.

CI runs on pushes and pull requests to `main` and `development`, and weekly.
Dependabot tracks Maven and GitHub Actions updates. The
[dependency migration report](docs/security-update-2026-09-23.md) documents the
earlier update before the introductory course modules were removed.

## Development

Open feature pull requests against `development`; `main` is the default branch.
Use English for identifiers, comments, test descriptions, documentation, and API
messages.
