package dev.ohhoonim.system.attachFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.time.Instant;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import dev.ohhoonim.system.attachFile.model.AttachFile;
import dev.ohhoonim.system.attachFile.model.AttachFileException;
import dev.ohhoonim.system.attachFile.model.AttachFileId;
import dev.ohhoonim.system.attachFile.model.AttachFilePolicy;
import dev.ohhoonim.system.attachFile.model.FileItem;
import dev.ohhoonim.system.attachFile.model.FileItemId;

class AttachFileModelTest {

    private AttachFileId attachFileId;
    private final String operator = "matthew";
    private AttachFilePolicy stubPolicy; // 테스트용 정책

    @BeforeEach
    void setUp() {
        attachFileId = AttachFileId.Creator.generate();
        
        // 테스트용 기본 정책 설정 (5개 제한, 모든 확장자 허용)
        stubPolicy = new AttachFilePolicy() {
            @Override
            public void verifyAddition(int currentCount) {
                if (currentCount >= 5) throw new AttachFileException("허용된 파일 개수를 초과하였습니다.");
            }

            @Override
            public void verifyExtension(String extension) {
                // 테스트 편의를 위해 모든 확장자 허용
            }

            @Override
            public boolean isExpired(Instant modifiedAt) {
                return false;
            }

            @Override
            public Instant getUnlinkedThreshold() {
                return Instant.now().minusSeconds(3600);
            }
        };
    }

    @Test
    @DisplayName("신규 첨부파일 그룹 생성 시 초기 상태가 올바라야 한다")
    void createNewGroup() {
        var attachFile = new AttachFile(attachFileId, operator);

        assertThat(attachFile.getId()).isEqualTo(attachFileId);
        assertThat(attachFile.getIsLinked()).isFalse();
        assertThat(attachFile.getAttachFiles()).isEmpty();
    }

    @Test
    @DisplayName("최대 파일 개수를 초과하여 추가할 수 없다")
    void fileCountLimitTest() {
        // Given
        var attachFile = new AttachFile(attachFileId, operator);
        var item = createMockItem();

        // When & Then
        assertThatThrownBy(() -> {
            // 이제 policy를 인자로 넘겨야 함
            IntStream.range(0, 6).forEach(_ -> attachFile.addFileItem(item, stubPolicy));
        })
        .isInstanceOf(AttachFileException.class)
        .hasMessageContaining("허용된 파일 개수를 초과");
    }

    @Test
    @DisplayName("보안상 금지된 확장자는 추가할 수 없다")
    void extensionSecurityTest() {
        // Given
        var attachFile = new AttachFile(attachFileId, operator);
        var jspFile = new FileItem(FileItemId.Creator.generate(), "attack.jsp", "path", 10L, "jsp", false);
        
        // 확장자 정책만 엄격하게 적용한 Mock 정책
        AttachFilePolicy securityPolicy = mock(AttachFilePolicy.class);
        doThrow(new AttachFileException("보안상 금지")).when(securityPolicy).verifyExtension("jsp");

        // When & Then
        assertThatThrownBy(() -> attachFile.addFileItem(jspFile, securityPolicy))
            .isInstanceOf(AttachFileException.class)
            .hasMessageContaining("보안상 금지");
    }

    @Test
    @DisplayName("파일 삭제 시 논리 삭제 처리되어야 한다")
    void logicalRemoveTest() {
        // Given
        var attachFile = new AttachFile(attachFileId, operator);
        var item1Id = FileItemId.Creator.generate();
        
        attachFile.addFileItem(createMockItem(item1Id), stubPolicy);

        // When
        attachFile.removeFileItem(item1Id);

        // Then
        assertThat(attachFile.getAttachFiles()).isEmpty();
        assertThat(attachFile.getRemovedFiles()).hasSize(1);
    }

    @Test
    @DisplayName("유예 기간이 지난 그룹은 Purge 대상이 된다")
    void canBePurgedTest() {
        // Given
        var attachFile = new AttachFile(attachFileId, operator); // isLinked = false 상태
        
        AttachFilePolicy expiredPolicy = mock(AttachFilePolicy.class);
        when(expiredPolicy.isExpired(any())).thenReturn(true);

        // When
        boolean result = attachFile.canBePurged(expiredPolicy);

        // Then
        assertThat(result).isTrue();
    }

    // Helper Methods
    private FileItem createMockItem(FileItemId id) {
        return new FileItem(id, "guide.pdf", "path/to/file", 1024L, "pdf", false);
    }
    
    private FileItem createMockItem() {
        return createMockItem(FileItemId.Creator.generate());
    }
}