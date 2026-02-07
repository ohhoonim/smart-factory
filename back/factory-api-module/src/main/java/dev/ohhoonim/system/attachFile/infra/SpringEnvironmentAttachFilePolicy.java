package dev.ohhoonim.system.attachFile.infra;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import dev.ohhoonim.system.attachFile.model.AttachFileException;
import dev.ohhoonim.system.attachFile.model.AttachFilePolicy;

@Component
public class SpringEnvironmentAttachFilePolicy implements AttachFilePolicy {

    @Value("${attach-file.max-limit:5}")
    private int maxLimit;

    @Value("${attach-file.allowed-extensions:jpg,jpeg,png,gif,webp}")
    private Set<String> allowedExtensions;

    @Value("${attach-file.retention-days:7}") // 유예 기간 설정 (기존 7일)
    private int retentionDays;

    @Value("${attach-file.unlinked-hours:24}")
    private int unlinkedHours;

    private static final Set<String> BLACKLIST = Set.of("jsp", "php", "asp", "aspx", "exe", "sh");

    @Override
    public void verifyAddition(int currentCount) {
        if (currentCount >= maxLimit) {
            throw new AttachFileException("파일 개수 제한을 초과했습니다.");
        }
    }

    @Override
    public void verifyExtension(String extension) {
        String lowerExt = extension.toLowerCase();
        if (BLACKLIST.contains(lowerExt)) {
            throw new AttachFileException("보안상 금지된 확장자입니다: " + extension);
        }
        if (!allowedExtensions.contains(lowerExt)) {
            throw new AttachFileException("허용되지 않은 확장자입니다.");
        }
    }

    @Override
    public boolean isExpired(Instant modifiedAt) {
        if (modifiedAt == null) return false;
        
        // 현재 시각이 (수정시각 + 유예기간) 보다 뒤에 있으면 만료된 것
        return Instant.now().isAfter(modifiedAt.plus(retentionDays, ChronoUnit.DAYS));
    }

    @Override
    public Instant getUnlinkedThreshold() {
        return Instant.now().minus(unlinkedHours, ChronoUnit.HOURS);
    }
}
