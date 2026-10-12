ALTER TABLE organizations
    ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE TABLE roles (
    id SMALLINT PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    CONSTRAINT chk_roles_code
        CHECK (code IN ('ADMIN', 'IT_MANAGER', 'ANALYST', 'VIEWER'))
);

CREATE TABLE users (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE RESTRICT,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(150) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    last_login_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'DISABLED')),
    CONSTRAINT chk_users_email_not_blank CHECK (btrim(email) <> ''),
    CONSTRAINT chk_users_display_name_not_blank CHECK (btrim(display_name) <> '')
);

CREATE UNIQUE INDEX uq_users_organization_email
    ON users (organization_id, lower(email));

CREATE INDEX idx_users_organization_status
    ON users (organization_id, status);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id SMALLINT NOT NULL REFERENCES roles(id) ON DELETE RESTRICT,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by UUID REFERENCES users(id) ON DELETE SET NULL,
    PRIMARY KEY (user_id, role_id)
);

CREATE INDEX idx_user_roles_role_id ON user_roles (role_id);

ALTER TABLE audit_events
    ADD COLUMN correlation_id VARCHAR(100),
    ADD CONSTRAINT fk_audit_events_actor
        FOREIGN KEY (actor_id) REFERENCES users(id) ON DELETE SET NULL;

CREATE INDEX idx_audit_events_correlation_id
    ON audit_events (correlation_id)
    WHERE correlation_id IS NOT NULL;

INSERT INTO roles (id, code, name) VALUES
    (1, 'ADMIN', 'Administrator'),
    (2, 'IT_MANAGER', 'IT Manager'),
    (3, 'ANALYST', 'Security Analyst'),
    (4, 'VIEWER', 'Read-only Viewer');
