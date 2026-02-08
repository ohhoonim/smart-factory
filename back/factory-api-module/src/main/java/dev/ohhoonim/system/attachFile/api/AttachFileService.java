package dev.ohhoonim.system.attachFile.api;

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
import dev.ohhoonim.system.attachFile.activity.FileCommandActivity;
import dev.ohhoonim.system.attachFile.activity.FileQueryActivity;
import dev.ohhoonim.system.attachFile.activity.FileStorageActivity;
import dev.ohhoonim.system.attachFile.activity.FileStreamActivity;
import dev.ohhoonim.system.attachFile.model.AttachFile;
import dev.ohhoonim.system.attachFile.model.AttachFileId;
import dev.ohhoonim.system.attachFile.model.AttachFilePolicy;
import dev.ohhoonim.system.attachFile.model.FileItem;
import dev.ohhoonim.system.attachFile.model.FileItemId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttachFileService {

    private final FileStorageActivity storageActivity;
    private final FileCommandActivity commandActivity;
    private final FileQueryActivity queryActivity;
    private final FileStreamActivity streamActivity;
    private final AttachFilePolicy attachFilePolicy;

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
        newItems.forEach(item -> newAttachFile.addFileItem(item, attachFilePolicy));

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
        newItems.forEach(item -> attachFile.addFileItem(item, attachFilePolicy));

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
     * 특정 파일을 물리 저장소와 DB에서 즉시 영구 삭제
     */
    @Transactional
    public void purgeFile(FileItemId fileItemId) {
        FileItem item = queryActivity.findItemById(fileItemId);
        // DB 메타데이터 먼저 삭제 시도
        commandActivity.deleteMetadata(fileItemId);
        // 확실히 커밋될 때만 물리 파일 삭제
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    storageActivity.deletePhysicalFile(item.uploadedPath());
                } catch (Exception e) {
                    log.error("물리 파일 삭제 실패 (고아 파일 발생): {}", item.uploadedPath(), e);
                    // 여기서 실패해도 DB는 이미 지워졌으니, 나중에 별도 스캔으로 지워야 함
                }
            }
        });
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
        Instant threshold = attachFilePolicy.getUnlinkedThreshold();

        List<AttachFile> unlinkedGroups = queryActivity.findUnlinkedOldFiles(threshold);

        for (AttachFile group : unlinkedGroups) {
            // SQL에서 이미 시간으로 걸러왔으니 바로 지워도 되지만,
            // 한 번 더 정책적으로 검증하고 싶다면 canBePurged를 쓸 수도 있어.
            if (group.canBePurged(attachFilePolicy)) {
                commandActivity.deleteGroupMetadata(group.getId());
                registerPhysicalFileDeletion(group.getFileItems());
            }
        }
    }

    /**
     * 논리 삭제된 파일들을 물리 저장소와 DB에서 완전히 제거 (배치용)
     */
    @Transactional
    public void cleanupRemovedFiles() {
        // 1. 삭제된 아이템이 포함된 '그룹(AR)'들을 조회해오도록 Activity 메서드 변경 필요
        // (기존 findRemovedItems() 대신 findGroupsWithRemovedItems() 사용 제안)
        List<AttachFile> groups = queryActivity.findGroupsWithRemovedItems();

        for (AttachFile group : groups) {
            // 2. 그룹(AR)의 수정 시점이 유예 기간 정책에 따라 만료되었는지 확인
            if (attachFilePolicy.isExpired(group.getModifiedAt())) {
                
                // 3. 해당 그룹 내에서 진짜 'isRemoved=true'인 녀석들만 골라냄
                List<FileItem> removedItems = group.getRemovedFiles();

                for (FileItem item : removedItems) {
                    // 4. DB 메타데이터 삭제 (FileItem 단위)
                    commandActivity.deleteMetadata(item.fileItemId());
                    
                    // 5. 물리 파일 삭제 예약
                    registerPhysicalFileDeletion(List.of(item));
                }
                
                log.info("[Cleanup] 그룹 {} 내 논리 삭제 파일 {}개 영구 제거", 
                    group.getId().value(), removedItems.size());
            }
        }
    }

    // 중복되는 동기화 로직 공통화
    private void registerPhysicalFileDeletion(List<FileItem> items) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                items.forEach(item -> storageActivity.deletePhysicalFile(item.uploadedPath()));
            }
        });
    }
}
