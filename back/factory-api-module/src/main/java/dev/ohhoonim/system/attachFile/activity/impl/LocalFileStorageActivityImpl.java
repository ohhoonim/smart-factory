package dev.ohhoonim.system.attachFile.activity.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import dev.ohhoonim.system.attachFile.activity.FileStorageActivity;
import dev.ohhoonim.system.attachFile.model.FileItem;
import dev.ohhoonim.system.attachFile.model.FileItemId;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LocalFileStorageActivityImpl implements FileStorageActivity {

    @Value("${attachFile.upload-path}")
    private String rootPath;

    @Override
    public List<FileItem> store(List<MultipartFile> files) {
        return files.stream().map(this::processSingleFile).toList();
    }

    private FileItem processSingleFile(MultipartFile file) {
        // 1. ULID 식별자 생성
        FileItemId fileId = FileItemId.Creator.generate();
        String idValue = fileId.getRawValue();

        // 2. 중간 경로 생성 (예: ab/cd/ulid)
        String p1 = idValue.substring(0, 2);
        String p2 = idValue.substring(2, 4);
        String relativePath = p1 + File.separator + p2 + File.separator + idValue;

        Path targetDir = Paths.get(rootPath, p1, p2);
        Path targetFile = Paths.get(rootPath, relativePath);

        try {
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }
            // 3. 물리 복사
            Files.copy(file.getInputStream(), targetFile);

            return new FileItem(
                fileId,
                file.getOriginalFilename(),
                relativePath,
                file.getSize(),
                FilenameUtils.getExtension(file.getOriginalFilename()),
                false
            );
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 중 시스템 오류 발생: " + file.getOriginalFilename(), e);
        }
    }

    @Override
    public void deletePhysicalFile(String uploadedPath) {
        try {
            Path fullPath = Paths.get(rootPath).resolve(uploadedPath);
            Files.deleteIfExists(fullPath);
        } catch (IOException e) {
            // 삭제 실패 시 로그만 남기고 배치가 중단되지 않도록 처리
            log.error("물리 파일 삭제 실패 (경로: {}): {}", uploadedPath, e.getMessage());
        }
    }
}
