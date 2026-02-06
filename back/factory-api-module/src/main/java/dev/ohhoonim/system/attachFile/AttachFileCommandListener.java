package dev.ohhoonim.system.attachFile;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import dev.ohhoonim.system.attachFile.activity.AttachFileService;
import dev.ohhoonim.system.attachFile.model.AttachFileId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AttachFileCommandListener {

    private final AttachFileService attachFileService;

    @ApplicationModuleListener
    public void on(AttachFileConfirmCommand command) {
        // String -> 내부 도메인 ID 타입으로 변환 (Value Object의 정적 팩토리 메서드 활용)
        AttachFileId groupId = AttachFileId.Creator.from(command.fileGroupId());
        
        attachFileService.confirmLink(groupId);
        
        log.info("[Event] 파일 그룹 {}이 연결 확정되었습니다.", 
            command.fileGroupId());
    }
}