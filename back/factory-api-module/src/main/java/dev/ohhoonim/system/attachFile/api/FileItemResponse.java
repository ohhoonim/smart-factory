package dev.ohhoonim.system.attachFile.api;

import dev.ohhoonim.system.attachFile.model.FileItem;

public record FileItemResponse(
        String fileId,
        String originName,
        long capacity,
        String extension
    ) {
        public static FileItemResponse from(FileItem item) {
            return new FileItemResponse(
                item.fileItemId().getRawValue(),
                item.originName(),
                item.capacity(),
                item.extension()
            );
        }
    }
