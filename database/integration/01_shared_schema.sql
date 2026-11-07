-- Integrated schema for the shared model. Run against lab_equipment in SSMS.
-- Creates missing shared tables. Never drops tables or changes existing rows.
-- A database using the old lab_users/devices model needs a separate data migration.
USE [lab_equipment];
GO
SET XACT_ABORT ON;
BEGIN TRY
    BEGIN TRANSACTION;

    IF OBJECT_ID(N'dbo.lab_users', N'U') IS NOT NULL OR OBJECT_ID(N'dbo.devices', N'U') IS NOT NULL
        THROW 51000, 'Legacy lab_users/devices tables detected. Review a data migration first; this script will not rewrite them.', 1;

    IF OBJECT_ID(N'dbo.users', N'U') IS NOT NULL AND COL_LENGTH('dbo.users', 'username') IS NULL
        THROW 51001, 'Existing users table needs an additive migration before using the integrated schema.', 1;
    IF OBJECT_ID(N'dbo.equipment', N'U') IS NOT NULL AND COL_LENGTH('dbo.equipment', 'specifications') IS NULL
        THROW 51002, 'Existing equipment table needs an additive migration before using the integrated schema.', 1;
    IF OBJECT_ID(N'dbo.loan_requests', N'U') IS NOT NULL AND COL_LENGTH('dbo.loan_requests', 'request_code') IS NULL
        THROW 51003, 'Existing loan_requests table needs an additive migration before using the integrated schema.', 1;

    IF OBJECT_ID(N'dbo.users', N'U') IS NULL
    CREATE TABLE dbo.users (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        username VARCHAR(100) NULL,
        full_name NVARCHAR(100) NOT NULL,
        email VARCHAR(255) NOT NULL UNIQUE,
        password_hash VARCHAR(255) NOT NULL,
        phone VARCHAR(20) NULL,
        role VARCHAR(30) NOT NULL CHECK (role IN ('STUDENT','STAFF','ADMIN')),
        active BIT NOT NULL DEFAULT 1,
        created_at DATETIME2 NULL
    );

    IF OBJECT_ID(N'dbo.categories', N'U') IS NULL
    CREATE TABLE dbo.categories (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        name NVARCHAR(100) NOT NULL UNIQUE,
        description NVARCHAR(500) NULL
    );

    IF OBJECT_ID(N'dbo.equipment', N'U') IS NULL
    CREATE TABLE dbo.equipment (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        category_id BIGINT NOT NULL REFERENCES dbo.categories(id),
        code VARCHAR(50) NOT NULL UNIQUE,
        name NVARCHAR(150) NOT NULL,
        description NVARCHAR(500) NULL,
        specifications NVARCHAR(4000) NULL,
        image_url VARCHAR(300) NULL,
        location NVARCHAR(100) NULL,
        created_at DATETIME2 NULL,
        status VARCHAR(30) NOT NULL CHECK (status IN ('AVAILABLE','ON_LOAN','UNDER_MAINTENANCE','RETIRED'))
    );

    IF OBJECT_ID(N'dbo.loan_requests', N'U') IS NULL
    CREATE TABLE dbo.loan_requests (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        request_code VARCHAR(50) NULL,
        student_id BIGINT NOT NULL REFERENCES dbo.users(id),
        equipment_id BIGINT NOT NULL REFERENCES dbo.equipment(id),
        processed_by_id BIGINT NULL REFERENCES dbo.users(id),
        status VARCHAR(30) NOT NULL CHECK (status IN ('PENDING','APPROVED','REJECTED','CANCELLED','BORROWED','RETURN_REQUESTED','RETURNED')),
        requested_at DATETIME2 NOT NULL,
        borrow_date DATETIME2 NULL,
        due_at DATETIME2 NOT NULL,
        handed_over_at DATETIME2 NULL,
        return_requested_at DATETIME2 NULL,
        returned_at DATETIME2 NULL,
        purpose NVARCHAR(500) NULL,
        notes NVARCHAR(500) NULL,
        cancel_reason NVARCHAR(500) NULL,
        cancelled_at DATETIME2 NULL,
        reject_reason NVARCHAR(500) NULL,
        staff_notes NVARCHAR(500) NULL
    );

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
        updated_at DATETIME2 NOT NULL,
        resolved_at DATETIME2 NULL,
        version BIGINT NOT NULL DEFAULT 0
    );

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

    IF OBJECT_ID(N'dbo.loan_notifications', N'U') IS NULL
    CREATE TABLE dbo.loan_notifications (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        student_id BIGINT NOT NULL REFERENCES dbo.users(id),
        loan_request_id BIGINT NULL REFERENCES dbo.loan_requests(id),
        title NVARCHAR(150) NOT NULL,
        message NVARCHAR(MAX) NOT NULL,
        type VARCHAR(30) NOT NULL,
        is_read BIT NOT NULL DEFAULT 0,
        created_at DATETIME2 NOT NULL
    );

    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.users') AND name='UX_users_username')
        CREATE UNIQUE INDEX UX_users_username ON dbo.users(username) WHERE username IS NOT NULL;
    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.loan_requests') AND name='UX_loan_request_code')
        CREATE UNIQUE INDEX UX_loan_request_code ON dbo.loan_requests(request_code) WHERE request_code IS NOT NULL;
    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.maintenance_tickets') AND name='IX_maintenance_equipment_status')
        CREATE INDEX IX_maintenance_equipment_status ON dbo.maintenance_tickets(equipment_id,status);
    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.maintenance_ticket_events') AND name='IX_maintenance_events_ticket_date')
        CREATE INDEX IX_maintenance_events_ticket_date ON dbo.maintenance_ticket_events(ticket_id,created_at,id);
    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.loan_requests') AND name='IX_loan_student_date')
        CREATE INDEX IX_loan_student_date ON dbo.loan_requests(student_id,requested_at);

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO
