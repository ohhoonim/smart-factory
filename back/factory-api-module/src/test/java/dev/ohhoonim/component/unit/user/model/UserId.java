package dev.ohhoonim.component.unit.user.model;

import dev.ohhoonim.component.unit.EntityId;

public record UserId(String value) implements EntityId {
    @Override
    public String getRawValue() {
        return value;
    }

    public static UserId from(String value) {
        return new UserId(value);
    }
}