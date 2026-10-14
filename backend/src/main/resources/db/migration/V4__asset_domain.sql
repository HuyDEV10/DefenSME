CREATE TABLE assets (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE RESTRICT,
    name VARCHAR(150) NOT NULL,
    type VARCHAR(30) NOT NULL,
    identifier VARCHAR(200) NOT NULL,
    criticality VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    owner_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    archived_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_assets_name_not_blank CHECK (btrim(name) <> ''),
    CONSTRAINT chk_assets_identifier_not_blank CHECK (btrim(identifier) <> ''),
    CONSTRAINT chk_assets_type CHECK (
        type IN ('SERVER', 'WORKSTATION', 'NETWORK_DEVICE', 'APPLICATION', 'CLOUD_SERVICE', 'DATABASE', 'OTHER')
    ),
    CONSTRAINT chk_assets_criticality CHECK (criticality IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    CONSTRAINT chk_assets_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED')),
    CONSTRAINT chk_assets_archive_state CHECK (
        (status = 'ARCHIVED' AND archived_at IS NOT NULL)
        OR (status <> 'ARCHIVED' AND archived_at IS NULL)
    )
);

CREATE UNIQUE INDEX uq_assets_active_identifier
    ON assets (organization_id, type, lower(identifier))
    WHERE archived_at IS NULL;

CREATE INDEX idx_assets_organization_status
    ON assets (organization_id, status);

CREATE INDEX idx_assets_organization_criticality
    ON assets (organization_id, criticality);

CREATE INDEX idx_assets_owner
    ON assets (owner_user_id)
    WHERE owner_user_id IS NOT NULL;
