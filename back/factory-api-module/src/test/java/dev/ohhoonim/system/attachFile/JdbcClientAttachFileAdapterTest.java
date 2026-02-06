package dev.ohhoonim.system.attachFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import dev.ohhoonim.system.attachFile.infra.JdbcClientAttachFileAdapter;
import dev.ohhoonim.system.attachFile.model.AttachFile;
import dev.ohhoonim.system.attachFile.model.AttachFileId;
import dev.ohhoonim.system.attachFile.model.FileItem;
import dev.ohhoonim.system.attachFile.model.FileItemId;
@Testcontainers
@JdbcTest // DB 관련 빈들만 로드하여 속도 최적화
@Import(JdbcClientAttachFileAdapter.class) // 테스트 대상 어댑터 주입
class JdbcClientAttachFileAdapterTest {

    @Container
    @ServiceConnection // 컨테이너 정보를 자동으로 DataSource에 연결
    private static final PostgreSQLContainer postgres = 
            new PostgreSQLContainer(DockerImageName.parse("postgres:18.1-alpine"));

    @Autowired
    private JdbcClientAttachFileAdapter adapter;

    private AttachFileId commonGroupId;
    private final String operator = "matthew";

    @BeforeEach
    void setUp() {
        commonGroupId = AttachFileId.Creator.generate();
    }

    @Test
    @DisplayName("파일 그룹과 아이템들을 저장하고 AR로 다시 로드했을 때 정합성이 유지되어야 한다")
    void saveAndLoadAggregateTest() {
        // Given
        var itemId = FileItemId.Creator.generate();
        var item = new FileItem(itemId, "handbook.pdf", "/storage/system", 2048L, "pdf", false);
        var attachFile = new AttachFile(commonGroupId, operator);
        attachFile.addFileItem(item);

        // When
        adapter.save(attachFile);
        
        // Then
        AttachFile loaded = adapter.load(commonGroupId).orElseThrow();
        assertAll(
            () -> assertThat(loaded.getId()).isEqualTo(commonGroupId),
            () -> assertThat(loaded.getFileItems()).hasSize(1),
            () -> assertThat(loaded.getFileItems().get(0).fileItemId()).isEqualTo(itemId),
            () -> assertThat(loaded.getCreatedBy()).isEqualTo(operator)
        );
    }

    @Test
    @DisplayName("UPSERT: 동일한 ID로 저장 시 기존 레코드가 업데이트되어야 한다 (ON CONFLICT)")
    void upsertLogicTest() {
        // Given
        var attachFile = new AttachFile(commonGroupId, operator);
        adapter.save(attachFile);

        // When: 연결 확정 상태로 변경 후 재저장
        attachFile.confirmLink();
        adapter.save(attachFile);

        // Then
        AttachFile updated = adapter.load(commonGroupId).orElseThrow();
        assertThat(updated.getIsLinked()).isTrue();
        assertThat(updated.getModifiedAt()).isAfter(updated.getCreatedAt());
    }

    @Test
    @DisplayName("파일 아이템 삭제 시 논리적 상태 변화가 DB에 반영되어야 한다")
    void itemLogicalDeleteUpdateTest() {
        // Given
        var itemId = FileItemId.Creator.generate();
        var item = new FileItem(itemId, "delete_me.txt", "/tmp", 10L, "txt", false);
        var attachFile = new AttachFile(commonGroupId, operator);
        attachFile.addFileItem(item);
        adapter.save(attachFile);

        // When: AR에서 아이템 제거(isRemoved=true) 후 저장
        attachFile.removeFileItem(itemId);
        adapter.save(attachFile);

        // Then
        FileItem result = adapter.loadItem(itemId).orElseThrow();
        assertThat(result.isRemoved()).isTrue();
    }

    @Test
    @DisplayName("그룹 삭제 시 부모 레코드와 연관된 모든 아이템 메타데이터가 제거되어야 한다")
    void deleteGroupCascadeEffectTest() {
        // Given
        var itemId = FileItemId.Creator.generate();
        var attachFile = new AttachFile(commonGroupId, operator);
        attachFile.addFileItem(new FileItem(itemId, "sub.img", "/img", 50L, "jpg", false));
        adapter.save(attachFile);

        // When
        adapter.deleteGroup(commonGroupId);

        // Then
        assertThat(adapter.load(commonGroupId)).isEmpty();
        assertThat(adapter.loadItem(itemId)).isEmpty();
    }
}