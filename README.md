# Employee Management & Directory System

A Spring Boot REST API for managing employee records, supporting full CRUD operations,
soft-delete deactivation, and security-protected salary endpoints.

## Architecture Overview

```
EmployeeController  →  EmployeeService  →  EmployeeRepository  →  H2 (in-memory)
```

- **Controller layer** (`com.app.controller`): Handles HTTP request/response mapping, input validation via `@Valid`.
- **Service layer** (`com.app.service`): Business logic, duplicate-email checks, soft-delete logic.
- **Repository layer** (`com.app.repository`): Spring Data JPA repository backed by H2 in-memory database.
- **Security** (`com.app.config.SecurityConfig`): Spring Security with HTTP Basic. The `GET /api/employees/{id}/salary` endpoint requires authentication; all other employee endpoints are public.
- **Exception handling** (`com.app.exception.GlobalExceptionHandler`): Centralised `@RestControllerAdvice` for validation errors (400), conflicts (409), not-found (404), and unexpected errors (500).

## How to Build

```bash
mvn install -DskipTests --quiet
```

## How to Run

```bash
mvn spring-boot:run --quiet
```

The server starts on port **8080** by default.

## How to Run Tests

```bash
mvn test
```

Tests use the `test` Spring profile, which configures an in-memory H2 database with `create-drop` DDL so each test run starts from a clean schema.

## Environment Variables

| Variable | Description | Default |
|---|---|---|
| `PORT` | HTTP port the server listens on | `8080` |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile (`test` for test DB) | _(none)_ |

Example:
```bash
SPRING_PROFILES_ACTIVE=test mvn spring-boot:run --quiet
```

## Security Model

| Endpoint | Auth Required |
|---|---|
| `GET /actuator/health` | No |
| `GET /api/employees` | No |
| `POST /api/employees` | No |
| `GET /api/employees/{id}` | No |
| `PATCH /api/employees/{id}` | No |
| `POST /api/employees/{id}/deactivate` | No |
| `GET /api/employees/{id}/salary` | **Yes** — HTTP Basic |

Unauthenticated requests to `/api/employees/{id}/salary` receive HTTP **401** or **403**.

## Soft-Delete Design

Deactivation (`POST /api/employees/{id}/deactivate`) sets `active = false` on the record and saves it.
The record is **never deleted** from the database. This means:

- `GET /api/employees/{id}` continues to return the record with `"active": false`.
- Audit history is preserved.
- The unique email constraint still applies, preventing re-registration with the same email address while the inactive record exists.

This approach is preferable to hard-delete in HR systems where employee history, payroll records, and audit trails must remain intact even after a person leaves the organisation.

## API Quick Reference

```
GET    /actuator/health                → 200
GET    /api/employees                  → 200 { "employees": [...] }
POST   /api/employees                  → 201 { "id": ..., "name": ..., ... }
GET    /api/employees/{id}             → 200 | 404
PATCH  /api/employees/{id}             → 200 | 404
POST   /api/employees/{id}/deactivate  → 200 { "id": ..., "active": false } | 404
GET    /api/employees/{id}/salary      → 200 (auth) | 401/403
```
