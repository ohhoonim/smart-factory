package dev.ohhoonim.system.attachFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import dev.ohhoonim.system.attachFile.model.AttachFile;
import dev.ohhoonim.system.attachFile.model.AttachFileException;
import dev.ohhoonim.system.attachFile.model.AttachFileId;
import dev.ohhoonim.system.attachFile.model.FileItem;
import dev.ohhoonim.system.attachFile.model.FileItemId;

class AttachFileModelTest {

    private AttachFileId attachFileId;
    private final String operator = "matthew";

    @BeforeEach
    void setUp() {
        attachFileId = AttachFileId.Creator.generate();
    }

    @Test
    @DisplayName("신규 첨부파일 그룹 생성 시 초기 상태가 올바라야 한다")
    void createNewGroup() {
        // When
        var attachFile = new AttachFile(attachFileId, operator);

        // Then
        assertThat(attachFile.getId()).isEqualTo(attachFileId);
        assertThat(attachFile.getIsLinked()).isFalse();
        assertThat(attachFile.getAttachFiles()).isEmpty();
        assertThat(attachFile.getModifiedBy()).isEqualTo(operator);
    }

    @Test
    @DisplayName("최대 파일 개수를 초과하여 추가할 수 없다")
    void fileCountLimitTest() {
        // Given
        var attachFile = new AttachFile(attachFileId, operator);
        var item = createMockItem();

        // When & Then
        assertThatThrownBy(() -> {
            IntStream.range(0, 6).forEach(_ -> attachFile.addFileItem(item));
        })
        .isInstanceOf(AttachFileException.class)
        .hasMessageContaining("허용된 파일 개수를 초과");
    }

    @Test
    @DisplayName("파일 삭제 시 논리 삭제 처리되어야 하며, 활성 파일 목록에서 제외되어야 한다")
    void logicalRemoveTest() {
        // Given
        var attachFile = new AttachFile(attachFileId, operator);
        var item1Id = FileItemId.Creator.generate();
        var item2Id = FileItemId.Creator.generate();
        
        attachFile.addFileItem(createMockItem(item1Id));
        attachFile.addFileItem(createMockItem(item2Id));

        // When
        attachFile.removeFileItem(item1Id);

        // Then
        assertThat(attachFile.getAttachFiles())
                .hasSize(1)
                .extracting(FileItem::fileItemId)
                .containsExactly(item2Id);

        assertThat(attachFile.getRemovedFiles())
                .hasSize(1)
                .extracting(FileItem::fileItemId)
                .containsExactly(item1Id);
    }

    @Test
    @DisplayName("삭제된 파일이 없을 때 getRemovedFiles 호출 시 예외가 발생한다")
    void getRemovedFilesExceptionTest() {
        // Given
        var attachFile = new AttachFile(attachFileId, operator);
        attachFile.addFileItem(createMockItem());

        // When & Then
        assertThatThrownBy(attachFile::getRemovedFiles)
                .isInstanceOf(AttachFileException.class)
                .hasMessage("삭제할 파일이 없습니다.");
    }

    @Test
    @DisplayName("연결 확정 시 상태가 변경되어야 한다")
    void confirmLinkTest() {
        // Given
        var attachFile = new AttachFile(attachFileId, operator);

        // When
        attachFile.confirmLink();

        // Then
        assertThat(attachFile.getIsLinked()).isTrue();
    }

    // Helper Method
    private FileItem createMockItem() {
        return createMockItem(FileItemId.Creator.generate());
    }

    private FileItem createMockItem(FileItemId id) {
        return new FileItem(id, "guide.pdf", "path/to/file", 1024L, "pdf", false);
    }
}