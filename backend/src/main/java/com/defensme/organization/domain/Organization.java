package com.defensme.organization.domain;

import com.defensme.core.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "organizations")
public class Organization extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String name;

    protected Organization() {
    }

    private Organization(String name) {
        this.name = requireText(name, "name");
    }

    public static Organization create(String name) {
        return new Organization(name);
    }

    public String getName() {
        return name;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }
}
