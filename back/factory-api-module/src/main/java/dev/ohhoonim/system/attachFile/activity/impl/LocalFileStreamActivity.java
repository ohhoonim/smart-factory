package dev.ohhoonim.system.attachFile.activity.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import dev.ohhoonim.system.attachFile.activity.FileStreamActivity;
import dev.ohhoonim.system.attachFile.model.FileItem;

@Component
public class LocalFileStreamActivity implements FileStreamActivity {

    @Value("${attachFile.upload-path}")
    private String rootPath;

    @Override
    public Resource createZipResource(List<FileItem> items) {
        try {
            Path zipFilePath = Files.createTempFile("download-", ".zip");
            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFilePath))) {
                for (FileItem item : items) {
                    Path sourcePath = Paths.get(rootPath).resolve(item.uploadedPath());
                    if (!Files.exists(sourcePath)) continue;

                    ZipEntry entry = new ZipEntry(item.originName() + "." + item.extension());
                    zos.putNextEntry(entry);
                    Files.copy(sourcePath, zos);
                    zos.closeEntry();
                }
            }
            // 주의: 실제 환경에서는 다운로드 후 임시 파일을 지우는 스케줄러나 별도 처리가 필요할 수 있습니다.
            return new FileSystemResource(zipFilePath);
        } catch (IOException e) {
            throw new RuntimeException("ZIP 생성 실패", e);
        }
    }

    @Override
    public long getContentLength(Resource resource) {
        try {
            return resource.contentLength();
        } catch (IOException e) {
            return 0L;
        }
    }
}
