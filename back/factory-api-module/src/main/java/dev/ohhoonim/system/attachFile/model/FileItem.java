package dev.ohhoonim.system.attachFile.model;

import java.util.Objects;

public record FileItem (
    FileItemId fileItemId,
    String originName,
    String uploadedPath,
    Long capacity,
    String extension,
    Boolean isRemoved
) {
    public FileItem {
        isRemoved = Objects.requireNonNullElse(isRemoved, false);
    }
}