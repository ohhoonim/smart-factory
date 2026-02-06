package dev.ohhoonim.system.attachFile;

/**
 * 파일 그룹을 실제 비즈니스 로직과 연결(확정)해달라는 명령 이벤트
 */
public record AttachFileConfirmCommand(
    String fileGroupId
    // 아래 두 필드는 나중에 필요하면 추가
    // String referenceType, // 예: "BOARD", "USER_PROFILE"
    // String referenceId    // 예: 게시글 번호 "123"
) {}