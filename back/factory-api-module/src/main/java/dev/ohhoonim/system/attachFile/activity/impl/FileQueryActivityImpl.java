package dev.ohhoonim.system.attachFile.activity.impl;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import dev.ohhoonim.system.attachFile.activity.FileQueryActivity;
import dev.ohhoonim.system.attachFile.model.AttachFile;
import dev.ohhoonim.system.attachFile.model.AttachFileException;
import dev.ohhoonim.system.attachFile.model.AttachFileId;
import dev.ohhoonim.system.attachFile.model.FileItem;
import dev.ohhoonim.system.attachFile.model.FileItemId;
import dev.ohhoonim.system.attachFile.port.AttachFileRepositoryPort;
@Component
public class FileQueryActivityImpl implements FileQueryActivity {

    @Value("${attachFile.upload-path}")
    private String rootPath;

    private final AttachFileRepositoryPort repositoryPort;

    public FileQueryActivityImpl(AttachFileRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public AttachFile findById(AttachFileId id) {
        return repositoryPort.load(id)
            .orElseThrow(() -> new AttachFileException("첨부파일 그룹을 찾을 수 없습니다: " + id.value()));
    }

    @Override
    public FileItem findItemById(FileItemId itemId) {
        // AR 구조를 유지하기 위해 Port에 findByItemId 같은 메서드를 추가하여 
        // 해당 아이템이 포함된 AttachFile을 로드하거나, 
        // 혹은 아이템만 단독 조회하는 Port 메서드를 호출합니다.
        return repositoryPort.loadItem(itemId)
            .orElseThrow(() -> new AttachFileException("파일 항목을 찾을 수 없습니다: " + itemId.getRawValue()));
    }

    @Override
    public Resource loadResource(FileItem item) {
        try {
            // AR에서 관리하는 FileItem의 uploadedPath(상대경로)와 rootPath 결합
            Path filePath = Paths.get(rootPath).resolve(item.uploadedPath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new AttachFileException("물리 파일이 존재하지 않거나 읽기 권한이 없습니다.");
            }
        } catch (MalformedURLException e) {
            throw new AttachFileException("파일 경로 형식이 잘못되었습니다.");
        }
    }

    @Override
    public List<FileItem> findRemovedItems() {
        return repositoryPort.findAllByRemovedStatus(true);
    }

   @Override
    public List<AttachFile> findUnlinkedOldFiles(Instant threshold) {
        return repositoryPort.findUnlinkedOldFiles(threshold);
    }
}
