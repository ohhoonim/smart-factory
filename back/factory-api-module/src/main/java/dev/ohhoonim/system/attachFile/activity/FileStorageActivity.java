package dev.ohhoonim.system.attachFile.activity;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import dev.ohhoonim.system.attachFile.model.FileItem;

public interface FileStorageActivity {
    /**
     * 멀티파트 파일을 물리 디렉토리에 저장하고 FileItem 리스트 생성
     */
    List<FileItem> store(List<MultipartFile> files);

    /**
     * 실제 물리 경로에 있는 파일 삭제
     */
    void deletePhysicalFile(String uploadedPath);
}
