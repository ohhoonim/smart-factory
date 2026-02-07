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
        try {
            AttachFileId groupId = AttachFileId.Creator.from(command.fileGroupId());
            attachFileService.confirmLink(groupId);
            log.info("[Event Success] 파일 그룹 {} 연결 확정", command.fileGroupId());
        } catch (Exception e) {
            log.error("[Event Failed] 파일 그룹 {} 확정 중 오류 발생: {}", command.fileGroupId(),
                    e.getMessage());
        }
    }
}
