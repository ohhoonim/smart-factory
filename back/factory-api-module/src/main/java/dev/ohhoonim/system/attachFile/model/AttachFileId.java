package dev.ohhoonim.system.attachFile.model;

import com.github.f4b6a3.ulid.Ulid;
import com.github.f4b6a3.ulid.UlidCreator;

import dev.ohhoonim.component.unit.EntityId;

public record AttachFileId(String value) implements EntityId {
    public AttachFileId{
        if (value == null || !Ulid.isValid(value)) {
            throw new IllegalArgumentException("올바른 AttachFileId형식이 아닙니다.");
        }
    }

    public static final Creator<AttachFileId> Creator = new Creator<>() {
        @Override
        public AttachFileId from(String value) {
            return new AttachFileId(value);
        }

        @Override
        public AttachFileId generate() {
            return new AttachFileId(UlidCreator.getUlid().toString());
        }
    };

    @Override
    public String getRawValue() {
        return value;
    }
}

