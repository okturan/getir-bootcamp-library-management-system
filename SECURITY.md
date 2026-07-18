# Security policy

## Supported version

Security fixes target the `main` branch. This repository is a local reference
application rather than a hosted service; historical commits and independently
deployed copies are not patched in place.

## Reporting a vulnerability

Please use [GitHub's private vulnerability reporting form](https://github.com/okturan/getir-bootcamp-library-management-system/security/advisories/new)
instead of opening a public issue. Include the affected endpoint, the role of
each test account, clear reproduction steps, and the security impact.

Relevant reports include:

- JWT signing, parsing, expiry, or authentication weaknesses;
- an authorization bypass between patrons, librarians, and administrators;
- access to another patron's profile or borrowing history, or unauthorized
  catalog and circulation changes;
- injection, unsafe OpenAPI or SSE output, CSRF, CORS, secret exposure, or
  container and dependency vulnerabilities with a demonstrated impact;
- production-profile behavior that falls back to tracked development
  credentials or enables mock data unexpectedly.

The documented H2 console, sample accounts, and local JWT default belong only
to the development profile. Their presence is not a vulnerability unless they
can affect the production profile or an external deployment contrary to the
documented boundary.

Test with synthetic books, users, and loans. Do not access real patron data or
include active credentials in a report. The maintainer will coordinate
validation, remediation, and disclosure through the private advisory.
