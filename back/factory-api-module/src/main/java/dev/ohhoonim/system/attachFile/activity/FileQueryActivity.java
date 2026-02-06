package dev.ohhoonim.system.attachFile.activity;

import java.time.Instant;
import java.util.List;
import org.springframework.core.io.Resource;
import dev.ohhoonim.system.attachFile.model.AttachFile;
import dev.ohhoonim.system.attachFile.model.AttachFileId;
import dev.ohhoonim.system.attachFile.model.FileItem;
import dev.ohhoonim.system.attachFile.model.FileItemId;

public interface FileQueryActivity {
    /**
     * ID를 통해 AttachFile Aggregate Root 전체를 조회
     */
    AttachFile findById(AttachFileId id);

    /**
     * 특정 FileItem의 경로 정보를 바탕으로 Spring Resource 생성
     */
    Resource loadResource(FileItem item);

    /**
     * 개별 파일 식별자(FileItemId)만으로 파일 정보를 조회해야 할 때 (예: 단일 다운로드)
     * 구현 시 내부적으로 RepositoryPort를 통해 해당 아이템이 속한 AR을 찾거나 
     * 전용 쿼리를 사용할 수 있도록 Port에 메서드 추가가 필요할 수 있습니다.
     */
    FileItem findItemById(FileItemId itemId);

    /**
     * 논리 삭제(is_removed = true)된 개별 파일 아이템 목록 조회
     */
    List<FileItem> findRemovedItems();

    /**
     * 연결 확정(is_linked = false)되지 않은 지 오래된 Aggregate Root 목록 조회
     */
    List<AttachFile> findUnlinkedOldFiles(Instant threshold);

}
