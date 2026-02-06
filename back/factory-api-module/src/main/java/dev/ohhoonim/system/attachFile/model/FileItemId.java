package dev.ohhoonim.system.attachFile.model;

import com.github.f4b6a3.ulid.Ulid;
import com.github.f4b6a3.ulid.UlidCreator;

import dev.ohhoonim.component.unit.EntityId;

public record FileItemId(String value) implements EntityId {
    
    public FileItemId{
        if (value == null || !Ulid.isValid(value)) {
            throw new IllegalArgumentException("올바른 AttachFileId형식이 아닙니다.");
        }
    }

    public static final Creator<FileItemId> Creator = new Creator<>() {
        @Override
        public FileItemId from(String value) {
            return new FileItemId(value);
        }

        @Override
        public FileItemId generate() {
            return new FileItemId(UlidCreator.getUlid().toString());
        }
    };

    @Override
    public String getRawValue() {
        return value;
    }
}
