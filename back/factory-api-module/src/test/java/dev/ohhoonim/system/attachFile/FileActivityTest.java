package dev.ohhoonim.system.attachFile;

import static org.assertj.core.api.Assertions.assertThat;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import dev.ohhoonim.system.attachFile.activity.impl.LocalFileStorageActivityImpl;
import dev.ohhoonim.system.attachFile.activity.impl.LocalFileStreamActivity;
import dev.ohhoonim.system.attachFile.model.FileItem;
import dev.ohhoonim.system.attachFile.model.FileItemId;

class FileActivityTest {

    private LocalFileStorageActivityImpl storageActivity;
    private LocalFileStreamActivity streamActivity;

    @TempDir
    Path tempDir; // JUnit5가 제공하는 가상 임시 디렉토리

    @BeforeEach
    void setUp() {
        storageActivity = new LocalFileStorageActivityImpl();
        streamActivity = new LocalFileStreamActivity();

        // @Value("${attachFile.upload-path}") 값을 임시 디렉토리로 주입
        ReflectionTestUtils.setField(storageActivity, "rootPath", tempDir.toString());
        ReflectionTestUtils.setField(streamActivity, "rootPath", tempDir.toString());
    }

    @Test
    @DisplayName("파일 저장 시 ULID 기반의 계층형 경로(2단)에 파일이 물리적으로 생성되어야 한다")
    void storePhysicalFileTest() throws IOException {
        // Given
        String originalFileName = "vibe_handbook.pdf";
        MockMultipartFile mockFile = new MockMultipartFile(
                "files", originalFileName, "application/pdf", "dummy content".getBytes());

        // When
        List<FileItem> savedItems = storageActivity.store(List.of(mockFile));

        // Then
        FileItem item = savedItems.get(0);
        Path expectedPath = tempDir.resolve(item.uploadedPath());

        assertThat(Files.exists(expectedPath)).isTrue(); // 파일 존재 확인
        assertThat(item.originName()).isEqualTo("vibe_handbook.pdf");
        assertThat(item.extension()).isEqualTo("pdf");
        
        // 경로 규칙 확인 (예: ab/cd/ULID...)
        String uploadedPath = item.uploadedPath();
        assertThat(uploadedPath).contains(File.separator);
        assertThat(item.fileItemId().getRawValue()).endsWith(Paths.get(uploadedPath).getFileName().toString());
    }

    @Test
    @DisplayName("물리 파일 삭제 요청 시 해당 경로의 파일이 제거되어야 한다")
    void deletePhysicalFileTest() throws IOException {
        // Given: 파일 미리 생성
        Path subDir = tempDir.resolve("ab/cd");
        Files.createDirectories(subDir);
        Path fileToId = subDir.resolve("test-file.txt");
        Files.writeString(fileToId, "delete me");

        // When
        storageActivity.deletePhysicalFile("ab/cd/test-file.txt");

        // Then
        assertThat(Files.exists(fileToId)).isFalse();
    }

    @Test
    @DisplayName("ZIP 생성 시 여러 개의 파일이 하나의 압축 파일로 합쳐져야 한다")
    void zipResourceTest() throws IOException {
        // Given: 물리 파일 2개 생성
        String file1Path = createDummyFile("file1.txt", "content 1");
        String file2Path = createDummyFile("file2.txt", "content 2");

        FileItem item1 = new FileItem(FileItemId.Creator.generate(), "file1", file1Path, 9L, "txt", false);
        FileItem item2 = new FileItem(FileItemId.Creator.generate(), "file2", file2Path, 9L, "txt", false);

        // When
        Resource zipResource = streamActivity.createZipResource(List.of(item1, item2));

        // Then
        assertThat(zipResource.exists()).isTrue();
        assertThat(zipResource.getFilename()).startsWith("download-");
        assertThat(zipResource.getFilename()).endsWith(".zip");

        // ZIP 내용물 검증 (실제 압축이 풀리는지 확인)
        try (ZipInputStream zis = new ZipInputStream(zipResource.getInputStream())) {
            ZipEntry entry;
            List<String> entryNames = new ArrayList<>();
            while ((entry = zis.getNextEntry()) != null) {
                entryNames.add(entry.getName());
            }
            assertThat(entryNames).containsExactlyInAnyOrder("file1.txt", "file2.txt");
        }
    }

    // 테스트용 물리 파일 생성을 위한 편의 메서드
    private String createDummyFile(String name, String content) throws IOException {
        String relativePath = "test/" + name;
        Path fullPath = tempDir.resolve(relativePath);
        Files.createDirectories(fullPath.getParent());
        Files.writeString(fullPath, content);
        return relativePath;
    }
}