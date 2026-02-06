package dev.ohhoonim.system.attachFile.activity;

import java.util.List;
import org.springframework.core.io.Resource;
import dev.ohhoonim.system.attachFile.model.FileItem;

public interface FileStreamActivity {
    /**
     * 여러 파일 아이템을 하나의 Zip 리소스로 압축하여 반환
     */
    Resource createZipResource(List<FileItem> items);
    
    /**
     * 리소스의 Content-Length를 계산 (물리 파일 크기 합계 등)
     */
    long getContentLength(Resource resource);
}
