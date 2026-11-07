-- Run this script in SSMS against the existing lab_equipment database.
-- Creates the agreed shared tables if missing. It never drops tables or data.
USE [lab_equipment];
GO
IF OBJECT_ID(N'dbo.users', N'U') IS NULL
CREATE TABLE dbo.users (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    full_name NVARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL CHECK (role IN ('STUDENT','STAFF','ADMIN')),
    active BIT NOT NULL DEFAULT 1
);
GO
IF OBJECT_ID(N'dbo.categories', N'U') IS NULL
CREATE TABLE dbo.categories (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    name NVARCHAR(100) NOT NULL UNIQUE,
    description NVARCHAR(500) NULL
);
GO
IF OBJECT_ID(N'dbo.equipment', N'U') IS NULL
CREATE TABLE dbo.equipment (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    category_id BIGINT NOT NULL REFERENCES dbo.categories(id),
    code VARCHAR(50) NOT NULL UNIQUE,
    name NVARCHAR(150) NOT NULL,
    status VARCHAR(30) NOT NULL CHECK (status IN ('AVAILABLE','ON_LOAN','UNDER_MAINTENANCE','RETIRED')),
    description NVARCHAR(500) NULL
);
GO
IF OBJECT_ID(N'dbo.loan_requests', N'U') IS NULL
CREATE TABLE dbo.loan_requests (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES dbo.users(id),
    equipment_id BIGINT NOT NULL REFERENCES dbo.equipment(id),
    processed_by_id BIGINT NULL REFERENCES dbo.users(id),
    status VARCHAR(30) NOT NULL CHECK (status IN ('PENDING','APPROVED','REJECTED','CANCELLED','BORROWED','RETURN_REQUESTED','RETURNED')),
    requested_at DATETIME2 NOT NULL,
    due_at DATETIME2 NOT NULL,
    handed_over_at DATETIME2 NULL,
    return_requested_at DATETIME2 NULL,
    returned_at DATETIME2 NULL
);
GO
IF OBJECT_ID(N'dbo.maintenance_tickets', N'U') IS NULL
CREATE TABLE dbo.maintenance_tickets (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    equipment_id BIGINT NOT NULL REFERENCES dbo.equipment(id),
    reported_by_id BIGINT NOT NULL REFERENCES dbo.users(id),
    assigned_staff_id BIGINT NULL REFERENCES dbo.users(id),
    loan_request_id BIGINT NULL REFERENCES dbo.loan_requests(id),
    description NVARCHAR(1000) NOT NULL,
    status VARCHAR(30) NOT NULL CHECK (status IN ('OPEN','IN_PROGRESS','RESOLVED','UNREPAIRABLE')),
    resolution_note NVARCHAR(1000) NULL,
    reported_at DATETIME2 NOT NULL,
    updated_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    resolved_at DATETIME2 NULL,
    version BIGINT NOT NULL DEFAULT 0
);
GO
-- The basic ERD did not have updated_at/version. Add only these maintenance columns if needed.
IF COL_LENGTH('dbo.maintenance_tickets', 'updated_at') IS NULL
    ALTER TABLE dbo.maintenance_tickets ADD updated_at DATETIME2 NOT NULL DEFAULT SYSDATETIME();
IF COL_LENGTH('dbo.maintenance_tickets', 'version') IS NULL
    ALTER TABLE dbo.maintenance_tickets ADD version BIGINT NOT NULL DEFAULT 0;
GO
IF OBJECT_ID(N'dbo.maintenance_ticket_events', N'U') IS NULL
CREATE TABLE dbo.maintenance_ticket_events (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    ticket_id BIGINT NOT NULL REFERENCES dbo.maintenance_tickets(id),
    actor_id BIGINT NOT NULL REFERENCES dbo.users(id),
    event_type VARCHAR(30) NOT NULL CHECK (event_type IN ('CREATED','ASSESSED','PROGRESS_UPDATED','COMPLETED')),
    status VARCHAR(30) NOT NULL CHECK (status IN ('OPEN','IN_PROGRESS','RESOLVED','UNREPAIRABLE')),
    note NVARCHAR(1000) NOT NULL,
    created_at DATETIME2 NOT NULL
);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.maintenance_tickets') AND name='IX_maintenance_equipment_status')
    CREATE INDEX IX_maintenance_equipment_status ON dbo.maintenance_tickets(equipment_id,status);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.maintenance_tickets') AND name='IX_maintenance_reporter_date')
    CREATE INDEX IX_maintenance_reporter_date ON dbo.maintenance_tickets(reported_by_id,reported_at);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.maintenance_ticket_events') AND name='IX_maintenance_events_ticket_date')
    CREATE INDEX IX_maintenance_events_ticket_date ON dbo.maintenance_ticket_events(ticket_id,created_at,id);
GO
