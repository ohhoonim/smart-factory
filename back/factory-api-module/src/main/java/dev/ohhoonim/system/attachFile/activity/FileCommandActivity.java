package dev.ohhoonim.system.attachFile.activity;

import dev.ohhoonim.system.attachFile.model.AttachFile;
import dev.ohhoonim.system.attachFile.model.AttachFileId;
import dev.ohhoonim.system.attachFile.model.FileItemId;

public interface FileCommandActivity {
    /**
     * 신규 AR(AttachFile)을 저장하거나, 기존 AR의 변경사항(FileItem 추가/삭제)을 저장
     */
    public void save(AttachFile attachFile);

    /**
     * 특정 FileItem을 논리 삭제 상태로 변경하는 명령 (AR을 통한 변경 권장)
     */
    public void markAsDeleted(AttachFileId id, FileItemId itemId, boolean isRemoved);


    void deleteMetadata(FileItemId itemId); // DB에서 레코드 삭제

    public void deleteGroupMetadata(AttachFileId id);
}
