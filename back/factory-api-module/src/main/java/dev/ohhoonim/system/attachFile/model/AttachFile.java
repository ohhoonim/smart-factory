package dev.ohhoonim.system.attachFile.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ohhoonim.component.unit.BaseEntity;

public class AttachFile extends BaseEntity<AttachFileId> {

    private List<FileItem> attachFiles = new ArrayList<>();
    private boolean isLinked ;
    private int LIMIT_MAX = 5;

    public AttachFile(AttachFileId id, String operator) {
        super(id, operator);
        this.recordModification(operator);
    }

    public static AttachFile reconstitute(AttachFileId attachFileId, List<FileItem> attachFiles,
            boolean isLinked, Instant createdAt, String createdBy, Instant modifiedAt, String modifiedBy) {
        return new AttachFile(attachFileId, attachFiles, isLinked, createdAt, createdBy, modifiedAt,
                modifiedBy);
    }

    private AttachFile(AttachFileId attachFileId, List<FileItem> attachFiles, boolean isLinked, Instant createdAt,
            String createdBy, Instant modifiedAt, String modifiedBy) {
        super(attachFileId, createdAt, createdBy, modifiedAt, modifiedBy);
        this.attachFiles = attachFiles;
        this.isLinked = isLinked;
    }

    public List<FileItem> getFileItems() {
        return Collections.unmodifiableList(this.attachFiles);
    }

    public boolean getIsLinked() {
        return this.isLinked;
    }

    public void addFileItem(FileItem fileItem) {
        if (attachFiles.size() >= LIMIT_MAX) {
            throw new AttachFileException("허용된 파일 개수를 초과하였습니다.");
        }
        this.attachFiles.add(fileItem);
        recordModification(getCreatedBy());
    }

    public void removeFileItem(FileItemId fileItemId) {
        this.attachFiles = attachFiles.stream().map(f -> {
            if (f.fileItemId().equals(fileItemId)) {
                return new FileItem(fileItemId, f.originName(), f.uploadedPath(), f.capacity(),
                        f.extension(), true);
            }
            return f;
        }).toList();
        recordModification(getModifiedBy());
    }

    public List<FileItem> getAttachFiles() {
        return Collections
                .unmodifiableList(this.attachFiles.stream().filter(f -> !f.isRemoved()).toList());
    }

    public List<FileItem> getRemovedFiles() {
        List<FileItem> removedFiles =
                this.attachFiles.stream().filter((item) -> item.isRemoved()).toList();
        if (removedFiles.size() == 0) {
            throw new AttachFileException("삭제할 파일이 없습니다.");
        }
        return removedFiles;
    }

    // 비즈니스 로직: 연결 확정
    public void confirmLink() {
        this.isLinked = true;
        recordModification(getModifiedBy());
    }


}
