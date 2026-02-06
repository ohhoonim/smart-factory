package dev.ohhoonim.system.attachFile;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.moments.support.TimeMachine;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.TestPropertySource;
import dev.ohhoonim.system.attachFile.model.AttachFile;
import dev.ohhoonim.system.attachFile.model.AttachFileId;
import dev.ohhoonim.system.attachFile.model.FileItem;
import dev.ohhoonim.system.attachFile.model.FileItemId;
import dev.ohhoonim.system.attachFile.port.AttachFileRepositoryPort;



@ApplicationModuleTest(mode = ApplicationModuleTest.BootstrapMode.DIRECT_DEPENDENCIES)
@TestPropertySource(properties = {"spring.modulith.moments.enable-time-machine=true",
                "spring.modulith.moments.granularity=days"})
class AttachFileEventListenerTest {

        @Autowired
        AttachFileRepositoryPort repositoryPort;

        @Autowired
        TimeMachine timeMachine;

        @Test
        @DisplayName("리스너 통합 테스트: DayHasPassed 이벤트 발생 시 미연결 파일과 논리 삭제 파일이 모두 정리되어야 한다")
        void timeEventListenerIntegrationTest(Scenario scenario) {
                // 1. Given: 정리 대상 데이터 준비

                // (A) 25시간 전 생성된 미연결(Orphan) 그룹
                AttachFileId orphanId = AttachFileId.Creator.generate();
                AttachFile orphanFile = AttachFile.reconstitute(orphanId, List.of(), false,
                                Instant.now().minus(25, ChronoUnit.HOURS), "SYSTEM",
                                Instant.now().minus(25, ChronoUnit.HOURS), "SYSTEM");
                repositoryPort.save(orphanFile);

                // (B) 논리 삭제(isRemoved=true)된 파일 아이템
                AttachFileId groupId = AttachFileId.Creator.generate();
                FileItemId removedItemId = FileItemId.Creator.generate();
                FileItem removedItem = new FileItem(removedItemId, "deleted.txt", "path/del", 100L,
                                "txt", true);
                AttachFile group = AttachFile.reconstitute(groupId, List.of(removedItem), true,
                                Instant.now(), "USER", Instant.now(), "USER");
                repositoryPort.save(group);

                // 2. When & Then: 하나의 대기 로직으로 두 상태 변화를 감시
                scenario.stimulate(() -> timeMachine.shiftBy(Duration.ofDays(1)))
                                .andWaitForStateChange(() -> {
                                        // 두 조건이 모두 만족되었는지(DB에서 사라졌는지) 여부를 판단
                                        boolean isOrphanDeleted =
                                                        repositoryPort.load(orphanId).isEmpty();
                                        boolean isRemovedItemDeleted = repositoryPort
                                                        .loadItem(removedItemId).isEmpty();
                                        return isOrphanDeleted && isRemovedItemDeleted;
                                }, isFinished -> isFinished // 반환된 boolean 값이 true가 될 때까지 폴링
                                ).andVerify(allDeleted -> {
                                        // 최종적으로 두 데이터가 모두 없는지 확증 (allDeleted가 true임이 보장됨)
                                        assertThat(allDeleted).isTrue();
                                        assertThat(repositoryPort.load(orphanId)).isEmpty();
                                        assertThat(repositoryPort.loadItem(removedItemId))
                                                        .isEmpty();
                                });
        }

        @Test
        @DisplayName("String 기반의 Confirm 명령 이벤트를 받으면 도메인 ID로 변환하여 처리해야 한다")
        void handleStringBasedConfirmCommand(Scenario scenario) {
                // Given
                AttachFileId groupId = AttachFileId.Creator.generate();
                String rawGroupId = groupId.getRawValue(); // String 값 추출

                repositoryPort.save(AttachFile.reconstitute(groupId, List.of(), false,
                                Instant.now(), "SYSTEM", Instant.now(), "SYSTEM"));

                // When: String ID를 담은 이벤트 발행
                scenario.publish(new AttachFileConfirmCommand(rawGroupId))
                                // Then
                                .andWaitForStateChange(() -> repositoryPort.load(groupId)
                                                .map(AttachFile::getIsLinked).orElse(false),
                                                isLinked -> isLinked == true)
                                .andVerify(result -> assertThat(result).isTrue());
        }
}
