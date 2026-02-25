# Spring Boot Backend Development Guidelines
Project: **AI Personal Finance Manager**

## Tech Stack
-   Runtime: Java 17
-   Framework: Spring Boot
-   ORM: JPA (Hibernate), QueryDSL
-   Database: H2 (Test), MySQL (Production)
-   Auth: JWT, Oauth2(Google)

## 3. Project Structure

    src/
    ├── controller          # API entry points
    ├── domain/{domain}     # Domain layer (Service, Entity, Repository)
    ├── domain/{domain}/dto # *request, *response
    ├── module/             # Shared modules (config, exception, response, aop, util, etc.)

### Architectural Principles
-   Controllers handle request/response mapping only (records allowed for simple DTOs).
-   Authorization must be implemented using AOP-based access control.
-   Business logic belongs strictly to the domain layer.
-   Shared utilities must reside in the module package.
-   Avoid unnecessary layering in a monolithic architecture.
-   Test code is mandatory.
-   Delete methods naming:
  -   `sDelete()` → Soft delete
  -   `hDelete()` → Hard delete
-   Every service method must include a single-line comment explaining
    its purpose.


## 4. API Conventions
-   Follow RESTful design principles.
-   Client API prefix: `/api/v1/client/**`
-   Admin API prefix: `/api/v1/admin/**`
-   Do NOT use class-level `@RequestMapping` prefixes.
-   All responses must use `ApiResponseUtil`.
Example:
``` java
return ApiResponseUtil.sendApiResponse(
    HttpStatus.OK,
    "sm.common.success.default",
    "success",
    data,
    null
);
```

## 5. Exception Handling
-   All Unchecked exceptions must use `CustomException`.
Error Code Format:
    {project}.{method}.{success|fail}.{detail}
Example:
    sm.create_budget.fail.duplicate_period
-   All exceptions must be handled in `GlobalExceptionHandler`.

## 6. Entity Design Rules
-   All tables must use auto-increment primary keys.
-   Primary key naming: `{table}_idx`
-   Date-only fields: `Date`
-   Date-time fields: `~At` suffix
-   All entities must extend `BaseEntity`.

## 7. Authentication Handling
-   Use `@AuthenticationPrincipal UserPrincipal` in Controllers.
-   Pass `userPrincipal.getAuthDto()` to Service methods.

## 8. Request DTO Rules
- All Request DTOs need validation check(spring-boot-starter-validation) 
- All Request DTOs must implement a `toEntity()` method. 
- Use `@Valid` on all Request DTOs.
- All Response DTOs must implement a `toResponse()` method.

## 10. Service Layer Rules
-   Apply `@Transactional(readOnly = true)` at class level.
-   Annotate write operations with `@Transactional`.
-   Follow Single Responsibility Principle (SRP).
-   Naming: `{Domain}Service`

------------------------------------------------------------------------

## 11. Full Flow Examples

### Create (POST)
``` java
@PostMapping("/api/v1/client/resources")
public ResponseEntity<?> create(
    @AuthenticationPrincipal UserPrincipal userPrincipal,
    @Valid @RequestBody CreateRequest request
) {
    return service.create(userPrincipal.getAuthDto(), request);
}
```

### Update (PUT)
``` java
@PutMapping("/api/v1/client/resources/{id}")
public ResponseEntity<?> update(
    @AuthenticationPrincipal UserPrincipal userPrincipal,
    @PathVariable Long id,
    @Valid @RequestBody UpdateRequest request
) {
    return service.update(userPrincipal.getAuthDto(), id, request);
}
```
