# Equipment Maintenance module

Implements the six agreed use cases with four screens: list, report, new and detail.

## Package structure

Following the HRM reference project, each controller depends on a service interface. The interface and its Impl class are in the same service package:

```text
features/maintenance/
  controller/MaintenanceController.java
  dto/
    DamageReportRequest.java
    CreateMaintenanceTicketRequest.java
    MaintenanceTicketActionRequest.java
    CompleteMaintenanceTicketRequest.java
    MaintenanceSearchRequest.java
    MaintenanceTicketResponse.java
    MaintenanceTicketDetailResponse.java
    MaintenanceEventResponse.java
    MaintenanceStatsResponse.java
    EquipmentOptionResponse.java
    LoanOptionResponse.java
    CurrentUserResponse.java
  repository/                     # Persistence interfaces
  service/
    MaintenanceService.java       # Interface
    MaintenanceServiceImpl.java   # Business rules and transactions
    MaintenanceIdentityService.java
    MaintenanceIdentityServiceImpl.java
shared/
  entity/                         # Shared ERD entities
  enums/
  exception/
    MaintenanceNotFoundException.java
    MaintenanceRuleException.java
    MaintenanceExceptionHandler.java
```

The exception handler remains scoped to MaintenanceController so it does not change the other members' error handling. The feature layout and shared entities remain as agreed by the team; the reference project's flat root packages are applied inside each feature.

## Controller/service DTO contract

Like DepartmentRequest/DepartmentResponse in the HRM reference, each request and response DTO is a separate JavaBean class with getters/setters. No service interface accepts or returns an Entity, including the current-user service. Controllers only call interfaces with request DTOs/IDs and receive response DTOs.

- List: `list(MaintenanceSearchRequest) -> Page<MaintenanceTicketResponse>`.
- Detail: `detail(Long) -> MaintenanceTicketDetailResponse`, with summary and history DTOs.
- Create/report: request DTO -> `MaintenanceTicketResponse`.
- Assess/progress/complete: ID + request DTO -> `MaintenanceTicketResponse`.
- Current user: `currentUser() -> CurrentUserResponse`, without password fields.

ServiceImpl still reads/writes Entities through repositories and maps them in private `toDto` methods, exactly as in the HRM reference. Entities never become controller model attributes. IDs and roles for the acting user come from persisted authentication data, not from submitted request DTOs.

## Run with SQL Server and the real login feature

1. Run `database/maintenance/01_schema.sql` in SSMS on the existing `lab_equipment` database. Review existing shared tables with the team first. The script creates missing tables and adds maintenance-only columns; it does not replace existing shared schema.
2. Keep the connection settings in `application.properties`. Run the application normally, without the UI preview profile:

```powershell
.\mvnw.cmd spring-boot:run
```

3. After logging in, open `http://localhost:8080/maintenance`. The auth feature must make `Authentication.getName()` return the user's email. The email must match an active row in `users`. Staff/admin actions are checked using persisted `users.role`. A generated default Spring Security user is not a business user.
4. Integrate the layout and navigation with real feature routes. The maintenance menu points to `/maintenance`; other modules' sample `/ui` links remain for their owners to replace.

There is no maintenance demo profile, sample-data seeder or role-switch endpoint in the application. It needs the real database and authentication integration for manual use. Isolated automated tests can run before those modules are ready: their fake users, data and H2 database exist only under `src/test`. H2 is test-only in Maven.

Entities are in `shared/entity`; enums are in `shared/enums`. These are the single shared definitions for the agreed ERD. Maintenance-prefixed repositories are internal to this module, not duplicate Entity definitions. Coordinate edits to User, Category, Equipment and LoanRequest with their module owners.

## Schema additions to the basic ERD

- `maintenance_tickets.updated_at`: latest ticket change.
- `maintenance_tickets.version`: optimistic version used to reject stale browser forms.
- `maintenance_ticket_events`: an append-only history of creation, assessment, progress and completion, including actor and time. Each ticket has many events; each event references one user. Real history requires more than overwriting one resolution note.

## State and integration rules

- Students can report only their own `BORROWED` or `RETURN_REQUESTED` loans. A report never marks the loan returned.
- Staff can create a ticket for equipment in storage, optionally linking an already `RETURNED` loan for the same equipment.
- At most one `OPEN` or `IN_PROGRESS` ticket per equipment. Writes lock equipment first to serialize competing maintenance operations; forms also carry the ticket version to detect stale updates.
- Creation sets the equipment to `UNDER_MAINTENANCE`. Assessment moves `OPEN → IN_PROGRESS` and records the staff member.
- Each progress update appends a history entry. Descriptions and notes are displayed as escaped text.
- Completion requires `IN_PROGRESS` and no active borrowed/return-requested loan for that equipment. `RESOLVED` sets equipment to `AVAILABLE`; `UNREPAIRABLE` sets it to `RETIRED`. Closed tickets are read-only.
- Module 4 must preserve `UNDER_MAINTENANCE` when receiving a damaged device. It sets the loan to `RETURNED` and records `returned_at`; maintenance owns the later decision to make equipment available or retire it. Handover must check equipment is still `AVAILABLE` in its own transaction.
- Staff can inspect and update all tickets; students see only tickets they reported. Authorization is enforced in the service, including direct requests with tampered IDs. All write endpoints use POST with CSRF protection.

## Routes

| Method | Route | Actor |
|---|---|---|
| GET | `/maintenance` | Student: own reports; staff/admin: all |
| GET/POST | `/maintenance/report?loanRequestId=...` | Student |
| GET | `/maintenance/new?equipmentId=...&loanRequestId=...` | Staff/admin |
| POST | `/maintenance` | Staff/admin |
| GET | `/maintenance/{id}` | Authorized viewer |
| POST | `/maintenance/{id}/assess` | Staff/admin |
| POST | `/maintenance/{id}/progress` | Staff/admin |
| POST | `/maintenance/{id}/complete` | Staff/admin |

POST `/maintenance/report` takes `loanRequestId,description`. POST `/maintenance` takes `equipmentId,loanRequestId(optional),description`. Assess/progress take `version,note`. Complete takes `version,outcome(RESOLVED/UNREPAIRABLE),note`.

## Verify

```powershell
.\mvnw.cmd test
```

The `test` profile uses an isolated in-memory H2 database. Integration tests exercise role boundaries, ownership, CSRF, form errors, state transitions, actual return requirements, history, stale forms and concurrent creation. H2 verification does not substitute for running the SQL Server schema and testing integration with the other modules.
