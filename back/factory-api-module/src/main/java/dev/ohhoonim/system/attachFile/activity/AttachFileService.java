package dev.ohhoonim.system.attachFile.activity;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import dev.ohhoonim.system.attachFile.model.AttachFile;
import dev.ohhoonim.system.attachFile.model.AttachFileId;
import dev.ohhoonim.system.attachFile.model.FileItem;
import dev.ohhoonim.system.attachFile.model.FileItemId;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttachFileService {

    private final FileStorageActivity storageActivity;
    private final FileCommandActivity commandActivity;
    private final FileQueryActivity queryActivity;
    private final FileStreamActivity streamActivity;

    /**
     * 완전히 새로운 첨부파일 그룹 생성 및 업로드
     */
    @Transactional
    public void addFilesToNewGroup(AttachFileId id, List<MultipartFile> files, String operator) {
        // 1. 새로운 빈 Aggregate Root 생성
        AttachFile newAttachFile = AttachFile.reconstitute(id, new ArrayList<>(), false,
                Instant.now(), operator, Instant.now(), operator);

        // 2. 물리 저장 및 아이템 추가
        List<FileItem> newItems = storageActivity.store(files);
        newItems.forEach(newAttachFile::addFileItem);

        // 3. 저장
        commandActivity.save(newAttachFile);
    }

    @Transactional
    public void addFilesToGroup(AttachFileId id, List<MultipartFile> files, String operator) {
        // 1. 기존 Aggregate Root 조회
        AttachFile attachFile = queryActivity.findById(id);

        // 2. 물리 파일 저장 및 Item 생성 (Activity)
        List<FileItem> newItems = storageActivity.store(files);

        // 3. 도메인 모델(AR)에 비즈니스 로직 위임 (모델 내부에서 LIMIT_MAX 등 체크)
        newItems.forEach(attachFile::addFileItem);

        // 4. 변경된 AR 상태 저장
        commandActivity.save(attachFile);
    }

    @Transactional
    public void removeFileFromGroup(AttachFileId id, FileItemId itemId) {
        AttachFile attachFile = queryActivity.findById(id);

        // 도메인 모델 내부에서 해당 아이템을 찾아 isRemoved = true 처리
        attachFile.removeFileItem(itemId);

        commandActivity.save(attachFile);
    }

    public Resource downloadFile(String fileId) {
        FileItem item = queryActivity.findItemById(new FileItemId(fileId));
        return queryActivity.loadResource(item);
    }

    public Resource downloadZip(List<String> fileIds) {
        List<FileItem> items =
                fileIds.stream().map(id -> queryActivity.findItemById(new FileItemId(id))).toList();
        return streamActivity.createZipResource(items);
    }

    public List<FileItem> getFilesFromGroup(AttachFileId id) {
        return queryActivity.findById(id).getAttachFiles();
    }

    public FileItem getFileItem(String fileId) {
        return queryActivity.findItemById(new FileItemId(fileId));
    }

    /**
     * 논리 삭제된 파일들을 물리 저장소와 DB에서 완전히 제거 (배치용)
     */
    @Transactional
    public void cleanupRemovedFiles() {
        List<FileItem> targets = queryActivity.findRemovedItems();

    for (FileItem item : targets) {
        // 1. DB 메타데이터 먼저 삭제 (트랜잭션 범위 내)
        commandActivity.deleteMetadata(item.fileItemId());

        // 2. 트랜잭션이 성공적으로 'Commit' 된 후에만 물리 파일 삭제 수행
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                storageActivity.deletePhysicalFile(item.uploadedPath());
            }
        });
    }
    }

    /**
     * 특정 파일을 물리 저장소와 DB에서 즉시 영구 삭제
     */
    @Transactional
    public void purgeFile(FileItemId fileItemId) {
        // 1. 삭제할 파일 정보 조회 (경로 정보가 필요함)
        FileItem item = queryActivity.findItemById(fileItemId);

        // 2. 물리 저장소에서 실제 파일 제거 (Storage Activity)
        storageActivity.deletePhysicalFile(item.uploadedPath());

        // 3. DB 메타데이터 완전 삭제 (Command Activity)
        commandActivity.deleteMetadata(fileItemId);
    }

    /**
     * [API] 외부 모듈에서 호출하거나 리스너가 호출
     */
    @Transactional
    public void confirmLink(AttachFileId id) {
        AttachFile attachFile = queryActivity.findById(id);
        attachFile.confirmLink();
        commandActivity.save(attachFile);
    }

    /**
     * [배치/이벤트 정책] 미사용 파일 및 논리 삭제 파일 정리 Moments API에 의해 정기적으로 실행됨
     */
    @Transactional
    public void cleanupFiles() {
        // 1. 논리 삭제 대상 즉시 정리 (isRemoved=true)
        List<FileItem> removedItems = queryActivity.findRemovedItems();
        removedItems.forEach(item -> {
            storageActivity.deletePhysicalFile(item.uploadedPath());
            commandActivity.deleteMetadata(item.fileItemId());
        });

        // 2. 미연결 파일(isLinked=false) 정리 (24시간 경과 기준)
        // Moments 이벤트 발생 시점 기준으로 만료 시간 계산
        Instant threshold = Instant.now().minus(24, ChronoUnit.HOURS);
        List<AttachFile> unlinkedGroups = queryActivity.findUnlinkedOldFiles(threshold);

        for (AttachFile group : unlinkedGroups) {
            group.getFileItems().forEach(i -> storageActivity.deletePhysicalFile(i.uploadedPath()));
            commandActivity.deleteGroupMetadata(group.getId());
        }
    }
}
