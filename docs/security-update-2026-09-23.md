# Dependency security update — 2026-09-23

The baseline is commit `8cdfaa83a119f4d4cf5b7232f44100d8cd8a13e1`
(the former `master` tip). `main` preserves that history, and `development`
was created from `main`.

## Scan results

Maven Dependency Plugin 3.11.0 resolved the dependencies of all three modules,
including transitive and test dependencies. Public Maven coordinates and versions
were queried against the [OSV API](https://google.github.io/osv.dev/api/).
Repeated coordinates at the same version are counted once across modules.

| Measurement | Baseline | Updated project |
| --- | ---: | ---: |
| Unique dependency versions checked | 131 | 115 |
| Affected dependency versions | 42 | 0 |
| Distinct advisory identifiers | 138 | 0 |
| Dependency/version/advisory pairs | 245 | 0 |

The final lookup completed at `2026-09-23T17:52:41Z`. These results describe
known dependency advisories at scan time, not a guarantee that the applications
have no vulnerabilities. Build-plugin dependencies, shaded components, source
code vulnerabilities, and deployment configuration are outside this scan.

## Changes

- Spring Boot 2.2.1/2.3.1 → 4.1.1, with Java 17 as the minimum.
- Embedded Tomcat explicitly pinned to 11.0.26 in both applications. Boot's
  managed 11.0.24 still triggered `GHSA-9xv2-5v5q-p794`,
  `GHSA-gcx9-497g-6cp6`, and `GHSA-h3x4-894j-xpx5`. The override also includes
  the fixes listed in the [Tomcat 11 security advisories](https://tomcat.apache.org/security-11.html).
  Keep the override until the Boot-managed version is at least as recent.
- ModelMapper 2.3.0 → 3.2.6; removed the DevTools runtime dependency.
- Jakarta persistence/validation imports, current Spring test annotations,
  Jackson 3, and Boot-managed Jupiter/JUnit 6, Mockito, and AssertJ.
- Official Maven Wrapper 3.3.4 with Maven 3.9.16 and a pinned SHA-256 checksum;
  removed the old Takari binary/downloader and tracked compiled classes.
- Added a root reactor build, CI for Java 17/21, weekly OSV checks, and Dependabot.

The framework migration follows the
[Spring Boot migration guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide)
and [Java requirements](https://docs.spring.io/spring-boot/system-requirements.html).

## Regression coverage

The suite contains 53 tests: 12 introductory tests, one first-REST-application
startup/endpoint test, and 40 library API tests. This includes the six
`PrimeiroTeste` methods that were previously outside Surefire's default filename
patterns. New full-context tests exercise persistence, JSON, and Jakarta
validation. They also exposed and now guard against the missing `@Service`
registration on `LoanServiceImpl`.

All 53 tests pass locally on Java 17.0.19 and 21.0.11, with no failures or skips.
CI additionally starts each packaged JAR and checks an actual HTTP response on
both Java versions. Local packaged-server checks encountered a Windows JDK
`UnixDomainSockets.connect` / `Unable to establish loopback connection` error;
the mock HTTP and database integration tests are unaffected.

See the root README for the build and scan commands. The scan writes
`target/osv-dependencies.json`; CI retains dependency trees and test/scan reports.
The course applications still have no authentication or authorization design.
