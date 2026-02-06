package dev.ohhoonim.system.attachFile.activity.impl;


import org.springframework.stereotype.Component;
import dev.ohhoonim.system.attachFile.activity.FileCommandActivity;
import dev.ohhoonim.system.attachFile.model.AttachFile;
import dev.ohhoonim.system.attachFile.model.AttachFileId;
import dev.ohhoonim.system.attachFile.model.FileItemId;
import dev.ohhoonim.system.attachFile.port.AttachFileRepositoryPort;

@Component
public class DatabaseFileCommandActivity implements FileCommandActivity {

    private final AttachFileRepositoryPort repositoryPort;

    public DatabaseFileCommandActivity(AttachFileRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public void save(AttachFile attachFile) {
        // Port를 통해 AR 전체를 영속화합니다.
        // Adapter(JdbcClient) 구현 시 AttachFile과 하위 FileItem들을 함께 저장하게 됩니다.
        repositoryPort.save(attachFile);
    }

    @Override
    public void markAsDeleted(AttachFileId id, FileItemId itemId, boolean isRemoved) {
        // 특정 아이템의 상태만 변경하는 경우에도 먼저 AR을 로드한 뒤 
        // 도메인 로직을 수행하고 다시 save 하는 흐름을 권장하지만,
        // 성능 최적화를 위해 Port에 직접 상태 변경 메서드를 추가할 수도 있습니다.
        repositoryPort.load(id).ifPresent(attachFile -> {
            // 논리적 삭제 로직 수행 후 저장
            attachFile.removeFileItem(itemId);
            repositoryPort.save(attachFile);
        });
    }

    /**
     * DB에서 특정 파일 아이템의 메타데이터를 영구 삭제합니다.
     */
    @Override
    public void deleteMetadata(FileItemId itemIdentifier) {
        // Port에 추가했던 deleteItemMetadata 메서드를 호출하여 
        // FILE_ITEM 테이블에서 해당 레코드를 DELETE 합니다.
        repositoryPort.deleteItemMetadata(itemIdentifier);
    }

    @Override
    public void deleteGroupMetadata(AttachFileId id) {
        repositoryPort.deleteGroup(id);
    }
}