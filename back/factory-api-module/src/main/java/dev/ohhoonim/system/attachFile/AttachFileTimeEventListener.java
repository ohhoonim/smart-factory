package dev.ohhoonim.system.attachFile;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.modulith.moments.DayHasPassed;
import org.springframework.stereotype.Component;
import dev.ohhoonim.system.attachFile.api.AttachFileService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AttachFileTimeEventListener {

    private final AttachFileService attachFileService;

    /**
     * 매일 자정(DayHasPassed)에 실행되는 파일 정리 정책
     */
    @ApplicationModuleListener
    public void on(DayHasPassed event) {
        // 1. 미연결(Orphan) 파일 정리 (24시간 경과 기준)
        attachFileService.cleanupFiles();
        
        // 2. 논리 삭제된 파일(Removed)의 물리적/영구적 정리
        attachFileService.cleanupRemovedFiles();
    }
}