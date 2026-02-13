package dev.ohhoonim.system.attachFile.api;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import dev.ohhoonim.system.attachFile.model.AttachFileId;
import dev.ohhoonim.system.attachFile.model.FileItem;
import dev.ohhoonim.system.attachFile.model.FileItemId;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/attachFile")
@RequiredArgsConstructor
public class AttachFileController {
    
    private final AttachFileService attachFileService;

    /**
     * 신규 파일 업로드 및 그룹 생성
     * @return 생성된 AttachFileId (ULID)
     */
    @PostMapping("/upload")
    public String uploadNewGroup(@RequestPart(value = "files") List<MultipartFile> files) {
        // 1. 신규 ULID 생성
        AttachFileId newId = AttachFileId.Creator.generate();
        // 2. 서비스 호출 (신규 생성을 위해 내부적으로 empty AR을 생성하거나 처리)
        attachFileService.addFilesToNewGroup(newId, files, "SYSTEM");
        // 3. 생성된 ID 반환 (클라이언트는 이 ID를 메인 엔티티 저장 시 함께 보냄)
        return newId.getRawValue();
    }

    /**
     * 파일 업로드 (신규 또는 기존 그룹에 추가)
     * - entityId가 있으면 기존 그룹에 추가, 없으면 신규 생성 로직으로 서비스에서 분기 가능
     */
    @PostMapping("/upload/{attachFileId}")
    public void upload(@PathVariable String attachFileId, 
                       @RequestPart(value = "files") List<MultipartFile> files) {
        attachFileService.addFilesToGroup(new AttachFileId(attachFileId), files, "SYSTEM");
    }

    /**
     * 파일 목록 조회
     */
    @GetMapping("/{attachFileId}")
    public List<FileItemResponse> searchFiles(@PathVariable("attachFileId") String attachFileId) {
        // 서비스에서 정제된 목록을 가져오도록 메서드 추가 권장
        var responses = attachFileService.getFilesFromGroup(new AttachFileId(attachFileId))
            .stream().map(FileItemResponse::from).toList();

        return responses;
    }

    /**
     * 파일 삭제 (논리 삭제)
     */
    @PostMapping("/remove/{attachFileId}/{fileItemId}")
    public void delete(@PathVariable String attachFileId, 
                       @PathVariable String fileItemId) {
        attachFileService.removeFileFromGroup(
            new AttachFileId(attachFileId), 
            new FileItemId(fileItemId)
        );
    }

    /**
     * 단일 파일 다운로드
     */
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> download(@PathVariable("fileId") String fileId) {
        // 서비스에서 파일 정보와 리소스를 한 번에 처리
        FileItem item = attachFileService.getFileItem(fileId);
        Resource resource = attachFileService.downloadFile(fileId);

        String filename = item.originName() + "." + item.extension();
        return createDownloadResponse(filename, resource, item.capacity());
    }

    /**
     * ZIP 다운로드
     */
    @GetMapping("/download-zip")
    public ResponseEntity<Resource> zipDownload(@RequestParam(name = "fileIds") List<String> fileIds) {
        Resource zipResource = attachFileService.downloadZip(fileIds);
        // Zip 파일의 용량은 Resource에서 직접 계산
        return createDownloadResponse("files.zip", zipResource, null);
    }

    /**
     * 다운로드 응답 헤더 공통 처리
     */
    private ResponseEntity<Resource> createDownloadResponse(String filename, Resource resource, Long size) {
        var responseBuilder = ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.builder("attachment")
                        .filename(filename, StandardCharsets.UTF_8)
                        .build().toString());
        
        // 사이즈 정보가 있으면 세팅 (Zip의 경우 리소스에서 직접 읽어야 함)
        if (size != null) {
            responseBuilder.contentLength(size);
        } else {
            try {
                responseBuilder.contentLength(resource.contentLength());
            } catch (IOException ignored) {}
        }

        return responseBuilder.body(resource);
    }

    /**
     * 파일 영구 삭제 (물리 파일 + DB 메타데이터 즉시 삭제)
     * @param fileId 삭제할 개별 파일의 식별자
     */
    @DeleteMapping("/purge/{fileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void purgeFile(@PathVariable("fileId") String fileId) {
        // 서비스에 즉시 삭제 위임
        attachFileService.purgeFile(new FileItemId(fileId));
    }
}