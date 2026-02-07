package dev.ohhoonim.system.attachFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import java.time.Instant;
import java.util.List;
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
import dev.ohhoonim.system.attachFile.model.AttachFilePolicy;
import dev.ohhoonim.system.attachFile.model.FileItem;
import dev.ohhoonim.system.attachFile.model.FileItemId;


@Testcontainers
@JdbcTest
@Import(JdbcClientAttachFileAdapter.class)
class JdbcClientAttachFileAdapterTest {

    @Container
    @ServiceConnection
    private static final PostgreSQLContainer postgres = 
            new PostgreSQLContainer(DockerImageName.parse("postgres:18.1-alpine")); // 버전은 마스터 환경에 맞춰

    @Autowired
    private JdbcClientAttachFileAdapter adapter;

    private AttachFileId commonGroupId;
    private final String operator = "matthew";
    
    // DB 테스트를 위한 관대한 더미 정책
    private final AttachFilePolicy permissivePolicy = new AttachFilePolicy() {
        @Override public void verifyAddition(int count) {} // 무제한 허용
        @Override public void verifyExtension(String ext) {} // 모두 허용
        @Override public boolean isExpired(Instant time) { return false; }
        @Override public Instant getUnlinkedThreshold() { return Instant.now(); }
    };

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
        
        // [수정] 정책 주입
        attachFile.addFileItem(item, permissivePolicy);

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
    @DisplayName("파일 아이템 삭제 시 논리적 상태 변화가 DB에 반영되어야 한다")
    void itemLogicalDeleteUpdateTest() {
        // Given
        var itemId = FileItemId.Creator.generate();
        var item = new FileItem(itemId, "delete_me.txt", "/tmp", 10L, "txt", false);
        var attachFile = new AttachFile(commonGroupId, operator);
        
        // [수정] 정책 주입
        attachFile.addFileItem(item, permissivePolicy);
        adapter.save(attachFile);

        // When
        attachFile.removeFileItem(itemId);
        adapter.save(attachFile);

        // Then
        FileItem result = adapter.loadItem(itemId).orElseThrow();
        assertThat(result.isRemoved()).isTrue();
    }

    @Test
    @DisplayName("삭제된 아이템을 가진 그룹들을 조회할 수 있어야 한다")
    void findGroupsWithRemovedItemsTest() {
        // Given
        var itemId = FileItemId.Creator.generate();
        var attachFile = new AttachFile(commonGroupId, operator);
        attachFile.addFileItem(new FileItem(itemId, "old.txt", "/tmp", 1L, "txt", false), permissivePolicy);
        
        // 삭제 상태로 만듦
        attachFile.removeFileItem(itemId);
        adapter.save(attachFile);

        // When
        List<AttachFile> groups = adapter.findGroupsWithRemovedItems();

        // Then
        assertThat(groups).isNotEmpty();
        assertThat(groups.get(0).getId()).isEqualTo(commonGroupId);
        assertThat(groups.get(0).getFileItems().get(0).isRemoved()).isTrue();
    }
}