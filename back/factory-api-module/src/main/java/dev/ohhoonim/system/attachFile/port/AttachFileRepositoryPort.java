package dev.ohhoonim.system.attachFile.port;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import dev.ohhoonim.system.attachFile.model.AttachFile;
import dev.ohhoonim.system.attachFile.model.AttachFileId;
import dev.ohhoonim.system.attachFile.model.FileItem;
import dev.ohhoonim.system.attachFile.model.FileItemId;

public interface AttachFileRepositoryPort {
    /**
     * Aggregate Root 전체 로드 (FileItem 목록 포함)
     */
    Optional<AttachFile> load(AttachFileId id);

    /**
     * 개별 파일 아이템 단독 로드
     */
    Optional<FileItem> loadItem(FileItemId itemId);

    /**
     * AttachFile 상태 및 하위 FileItem 목록 저장/수정 (Upsert)
     */
    void save(AttachFile attachFile);

    /**
     * 삭제 상태인 모든 FileItem 목록 조회 (물리 삭제 대상 추출용)
     */
    List<FileItem> findAllByRemovedStatus(boolean isRemoved);

    /**
     * DB에서 특정 FileItem 메타데이터 완전 삭제
     */
    void deleteItemMetadata(FileItemId itemId);

    /**
     * 연결되지 않은(is_linked = false) 오래된 AttachFile 목록 조회
     */
    List<AttachFile> findUnlinkedOldFiles(Instant threshold);

    /**
     * AttachFile 그룹 전체 삭제 (Cascade 설정을 통해 하위 아이템 자동 삭제 권장)
     */
    void deleteGroup(AttachFileId id);
}