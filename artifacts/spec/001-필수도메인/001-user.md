# User 도메인 모델

## 모델링 바이브

- User 바이브
    - B. 보안/인증 지향 (금융/SaaS): 계정의 상태(Active, Locked, Dormant), 로그인 시도 횟수, 권한(Role/Permission) 관리가 엄격한 바이브.
    - C. 비즈니스 도메인 지항 (사내 시스템): 조직도 내의 위치, 부서 정보, 직함 등이 연결된 정적인 정보가 강한 바이브.
- 액티비티 흐름
    - 가입(Registration): 외부의 존재가 시스템 내부의 `User`로 탄생하는 과정
    - 인증/인가(Auth): `User`가 자신의 존재를 증명하고 권한을 얻는 과정
        - 인증
        - 인가
    - 상태 변화(Status Management): 휴면 처리나 탈퇴 등 `User`의 생명주기 관리
        - Self-Update: 사용자가 직접 비밀번호를 바꾸거나 프로필을 수정하는 흐름.
        - Admin-Update: 관리자가 사용자의 부서를 옮기거나(`departmentId` 변경), 잠긴 계정을 해제(`LOCKED` -> `ACTIVE`)하는 흐름.
        - System-Update: HR 배치에 의해 정보가 일괄 업데이트되는 흐름.
    - 감사로그 (이벤트 발행)
    - 메뉴렌터링 (외부정보 참조)
- 외부 도메인 모델과의 관계
    - 감사 로그 모델 : User 모델은 `Audit Log`를 생성하는 주체이자 대상이다.
    - 메뉴 및 권한 모델 : User 모델은 `Role`이라는 브리지를 통해 메뉴/버튼 권한을 행사한다.
- 유비쿼터스 언어 점검
    - Source Verification: 가입 요청 시 인사시스템 등 외부 데이터와 대조하는 행위.
    - Provisioning: 배치나 관리자에 의해 사용자 계정이 미리 준비되는 과정.
    - Lifecycle Status: `User`의 생성부터 소멸까지의 상태 변화.
    - Credential (인증 정보): `User`가 본인임을 증명하기 위해 제시하는 것 (Password, OTP, SSO Token 등).
    - Role (역할): 권한의 묶음. 조직(C) 기반으로 할당됩니다 (예: 인사팀장, 시스템 관리자).
    - Permission (권한): 시스템 내 구체적인 행위 (예: 회원 조회, 급여 수정). Role은 여러 개의 Permission을 가집니다.
    - Access Policy (접근 정책): 특정 조건(IP, 시간, 상태)에 따라 인증/인가를 통제하는 보안 규칙.
    - Grace Period (유예 기간): 비밀번호가 만료되었을 때 즉시 차단하기보다, 며칠간 변경 안내를 하며 접속을 허용할 것인지에 대한 규칙입니다.
    - Step-up Authentication: 평소에는 ID/PW로 로그인하지만, 민감한 메뉴(예: 급여 관리)에 접근할 때만 2FA를 한 번 더 요구하는 방식입니다. (인가 액티비티와 연결됨)
    - Account Lockout Policy: 5회 실패 시 `LOCKED`가 되면, 관리자가 풀어줘야 하는지 혹은 30분 뒤 자동으로 풀리는지에 대한 정의가 필요합니다. (백오피스라면 '관리자 해제'가 보안상 유리합니다.)
    - 감사 로그 도메인 모델과의 상호작용
        - `logId`: PK
        - `actorId`: 행위자(User) ID
        - `targetId`: 대상(User 또는 메뉴 등) ID
        - `actionType`: `LOGIN`, `UPDATE_ROLE`, `STATUS_CHANGE` 등
        - `beforeData`: 수정 전 JSON 스냅샷
        - `afterData`: 수정 후 JSON 스냅샷
        - `clientIp`: 보안(B)을 위한 접속 IP
    - 메뉴 및 권한 도메인 모델과의 상호작용
        - `menuCode`: `USER_MGMT`, `PAYROLL_MGMT` (Role 매핑의 기준)
        - `permissionCode`: `BTN_CREATE`, `BTN_DELETE`
        - `isSystemMenu`: 관리자 전용 여부

---

## Model

### User 엔티티 속성

- 관련 액티비티 : 가입 (Registration)
    - 식별 정보: `userId`, `employeeNumber`(사번), `email`
    - 조직 정보 (C지향): `departmentId`, `position`(직급), `jobRole`(직무)
    - 보안 상태 (B지향): `status` (PENDING, ACTIVE, LOCKED, WITHDRAWN), `lastLoginAt`, `failedLoginAttempts`
    - 인증 원천: `authSource` (HR_SYSTEM, MANUAL, SSO) -> *배치로 등록되었는지 직접 가입했는지 구분*
- 관련 액티비티 : 인증/인가 (Auth)
    - 조직 기반 권한 속성:
        - `assignedRoles`: `User`에게 직접 부여되거나 부서/직급에 의해 자동 부여된 역할 목록.
        - `isHighPrivilege`: 민감한 데이터 접근 권한 여부 (보안 감사 대상).
    - 보안 제약 관련 속성:
        - `allowedIpRanges`: 접속 가능한 특정 IP 대역 (보안 강화).
        - `passwordChangedAt`: 비밀번호 마지막 변경일 (정기 변경 정책).
        - `dormantAt`: 장기 미사용으로 인해 권한이 제한되기 시작한 시점.
        - `effectivePermissions`: (가공 속성) 상태와 역할을 종합하여 현재 행사 가능한 최종 권한 리스트.
    - 2FA 관련 속성:
        - `twoFactorSecret`: 2차 인증을 위한 공유 비밀키(또는 등록된 기기 정보).
        - `isTwoFactorEnabled`: 사용자의 2FA 활성화 여부.
    - 비밀번호 정책 속성:
        - `isPasswordExpired`: (가공 속성) `passwordChangedAt` 기준으로 만료 여부 판단.
        - `temporaryPassword`: 임시 비밀번호 발급 여부 (최초 로그인 시 변경 유도용).
    - HR 배치 연동 관련:
        - `isRoleSyncedWithHR`: HR 정보 변경 시 역할(Role)을 자동으로 다시 계산할지 여부(수동으로 고정된 특수 권한 보유자 식별용).
    - 권한 대행 관련 속성:
        - `delegatedFromUserId`: 권한을 넘겨준 원 소유자의 ID.
        - `delegationExpiredAt`: 위임 권한이 만료되는 일시.
    - 보안 감사 및 정책 관련 속성:
        - `lastSecurityAuditAt`: 마지막 보안 검토 일자 (고권한 사용자의 주기적 점검용).
        - `isSodExempt`: 직무 분리 예외 적용 여부 (특수 상황을 대비한 비상 권한자).
- 관련 액티비티 : 상태 변화 (Status Management)
    - Audit 및 관리 속성:
        - `updatedBy`: 마지막으로 정보를 수정한 주체 (UserId 혹은 'SYSTEM').
        - `updatedAt`: 정보 수정 일시.
        - `statusUpdateReason`: 상태가 `LOCKED`나 `WITHDRAWN`으로 바뀐 구체적인 사유.
    - 권한 구조 (가공/연결 속성):
        - `accessibleMenus`: `assignedRoles`를 통해 계산된 접근 가능 메뉴 리스트.
        - `allowedActions`: 현재 메뉴 맥락에서 실행 가능한 버튼(Permission) 리스트.

### User 모델의 동적 변화 (Lifecycle Status)

액티비티가 진행됨에 따라 `User` 모델 내부의 상태 값은 다음과 같이 변이합니다.

| 현재 상태 (Status) | 전이 이벤트 (Activity) | 결과 상태 (Target Status) | 비고 |
| --- | --- | --- | --- |
| (None) | HR 배치 생성 | `PENDING` | 인사 정보는 있으나 본인 인증 전 |
| (None) | 관리자 초대 생성 | `INVITED` | 외부 협력사 등 임시 계정 |
| (None) | 사용자 가입 신청 | `WAITING_APPROVAL` | HR 정보가 없는 외부인 등 |
| PENDING / INVITED | 본인 인증 완료 | `ACTIVE` | 정상 이용 가능 상태 |
| WAITING_APPROVAL | 관리자 가입 승인 | `ACTIVE` | 관리자 수동 검토 완료 |
| ACTIVE | 퇴사 배치 / 관리자 차단 | `WITHDRAWN / LOCKED` | 보안 정책에 따른 접근 차단 |
| ACTIVE | 로그인 5회 실패 | LOCKED | 보안상 계정 잠금 처리 |
| LOCKED | 관리자 본인확인 후 잠금 해제 | ACTIVE | 수동 복구 프로세스 |
| ACTIVE | 비밀번호 만료 확인 | PASSWORD_EXPIRED | (임시 상태) 변경 전까지 기능 제한 |
| ACTIVE | 90일 이상 미접속 (배치) | LOCKED | 휴면 계정 정책에 따른 자동 잠금 |
| ACTIVE | 부서 이동 (HR 배치) | ACTIVE | 상태 유지, 단 `effectivePermissions`는 실시간 변경 |
| ACTIVE | 직무 분리 위반 시도 | ACTIVE (Deny) | 행위 거부 및 보안 감사 로그 생성 |
| ACTIVE | 30일 미접속 (배치/인가) | ACTIVE (Restricted) | 상태는 유지하되 권한만 최소화 (`dormantAt` 기록) |
| ACTIVE | 권한 위임 수락 | ACTIVE (Delegated) | 본인 권한 + 위임 권한 행사 가능 상태 |
| ACTIVE | 위임 기한 만료 | ACTIVE | 위임받은 권한 자동 회수 |
| ACTIVE | 미승인 IP에서 고권한 시도 | ACTIVE (Step-up Req) | 행위 일시 정지 및 추가 인증(2FA) 요구 |
| PASSWORD_EXPIRED (Self) | 비밀번호 변경 | ACTIVE | `passwordChangedAt` 갱신, 로그인 차단 해제 |
| LOCKED (Admin) | 계정 잠금 해제 | ACTIVE | `failedLoginAttempts` 0으로 초기화 |
| ACTIVE (System) | 퇴사자 처리 | WITHDRAWN | 모든 `Role` 및 `Permission` 즉시 상실 |
| ACTIVE (Admin) | 권한 부여 | ACTIVE | `assignedRoles` 변경 → `effectivePermissions` 즉시 반영 |

### Role(메뉴)과 Permission(버튼)의 모델링

`User` 모델과 연결되는 권한 구조를 구체화합니다.

- Role (메뉴 접근권):
    - 예: `USER_MANAGEMENT_MENU`, `SYSTEM_SETTINGS_MENU`
    - 사용자가 해당 Role을 가지고 있지 않으면 대시보드에서 메뉴 자체가 노출되지 않음.
- Permission (버튼/기능 제어):
    - 예: `USER_CREATE_BTN`, `USER_LOCK_BTN`, `USER_EXCEL_DOWNLOAD_BTN`
    - 메뉴에 들어왔더라도 특정 버튼이 비활성화되거나 클릭 시 "권한이 없습니다" 메시지 발생.

---

## Activity

## 가입(Registration)

### A. Registration 흐름

- 경로 A: 배치 등록 (HR Provisioning)
    - Trigger: 스케줄러에 의한 자동 실행.
    - Flow: 외부 HR 데이터와 시스템 내 `User` 모델을 싱크.
    - Status: 신규 입사자는 `PENDING`(본인 확인 전) 혹은 `INACTIVE`로 자동 생성.
- 경로 B: 직접 가입 (Self-Registration)
    - Trigger: 사용자가 가입 페이지에서 정보 입력.
    - Flow: Source Verification 단계를 거침. HR 정보가 있으면 즉시 승인, 없으면 '관리자 승인 대기' 단계로 분기.
    - Status: 검증 성공 시 `ACTIVE`, 검증 실패(HR 정보 없음) 시 `WAITING_APPROVAL`.
- 경로 C: 초대/선등록 (Admin Pre-provisioning)
    - Trigger: 관리자가 백오피스에서 특정 이메일/사번으로 직접 등록.
    - Flow: 시스템에 계정을 미리 파생(Provision)하고 고유 가입 토큰이 포함된 이메일 발송.
    - Status: 최초 생성 시 `INVITED` 혹은 `PENDING`. 사용자가 링크를 타고 들어와 본인 인증을 완료하면 `ACTIVE`.

---

## 인증/인가(Auth)

### B. 인증(Authentication) 흐름

백오피스 보안(B)을 강화한 인증 프로세스입니다.

1. Identity Challenge (식별): `userId` 혹은 `email`을 입력받습니다.
2. Status Check (상태 검증): `User` 모델의 `status`가 `ACTIVE`인지 확인합니다. `LOCKED`나 `WITHDRAWN`이면 즉시 거절합니다.
3. Credential Verification (인증): `authSource`에 따라 검증 (SSO 위임 또는 MANUAL 해시 대조).
4. Password Policy Check (비밀번호 만료 체크) :  `passwordChangedAt`을 현재 날짜와 대조합니다.
    - 만료 기간(예: 90일)을 초과했다면, '비밀번호 변경 필요' 상태를 반환하고 로그인을 일시 중단합니다.
    - 
5. Multi-Factor Authentication (2FA) [추가]:
    - 패스워드 인증 성공 시, `isHighPrivilege` 속성이 `true`이거나 보안 정책상 2FA가 필수인 경우 실행합니다.
    - 등록된 이메일이나 별도 인증기(OTP 등)로 코드를 발송하고 검증합니다.
6. Security Feedback (보안 피드백):
    - 실패 시: `failedLoginAttempts`를 +1 시킵니다. 임계치(예: 5회) 도달 시 `status`를 `LOCKED`로 변경합니다.
    - 성공 시: `failedLoginAttempts`를 0으로 초기화하고 `lastLoginAt`을 갱신합니다.

### C. 인가(Authorization) 흐름

이 흐름은 사용자가 시스템에 로그인한 직후부터 매 요청(Request) 시마다 권한을 검증하는 엔진의 역할을 합니다.

1. Status-Permission Interlock (상태-권한 연동 체크):
    - `User.status` 검증 및 `lastLoginAt` 기준 장기 미사용 여부 판단.
    - 30일 경과 시 `effectivePermissions`를 최소화(Read-only 등)하고, 90일 경과 시 배치에 의해 `status`를 `LOCKED`로 전이.
2. Dynamic Role Mapping (동적 역할 매핑):
    - `isRoleSyncedWithHR`이 `true`인 경우 현재 `departmentId`, `position`에 맞는 Role을 실시간 매핑.
    - Delegation Check: 현재 요청자가 타인으로부터 권한을 위임받은 상태(`delegatedFromUserId`)이고, 위임 기한(`delegationExpiredAt`) 내에 있다면 위임자의 Role을 추가로 로드.
3. Permission Expansion (권한 확장):
    - 본인의 Role + 위임받은 Role을 합산하여 전체 `Permission` 집합 산출.
4. Contextual Policy & SoD Check (맥락 및 직무 분리 검증):
    - B지향 (보안): `allowedIpRanges` 체크 및 고권한(`isHighPrivilege`) 작업 시 2FA 재인증 요구(Step-up Auth).
    - C지향 (조직): 데이터의 소속과 `User.departmentId` 대조.
    - SoD Check: 특정 트랜잭션(예: 결재)의 경우, '기안자'와 현재 '인가 요청자'가 동일인인지 체크하여 동일인일 경우 승인 권한 차단.

---

## User 정보 수정 (Update) 흐름

이 액티비티는 수정 주체(Self, Admin, System)에 따라 인가 규칙이 다르게 작동합니다.

### D. Self-Update (사용자 본인)

- 수정 범위: 주로 비밀번호, 연락처, 2FA 활성화 여부 등.
- 인가 규칙: `userId`가 현재 로그인한 세션의 ID와 일치해야 함.
- 특이점: 비밀번호 수정 시 `passwordChangedAt`이 갱신되며, 이 과정에서 기존 비밀번호 확인이 필수적인 'Step-up Auth'가 발생합니다.

### E. Admin-Update (관리자 수동 관리)

- 수정 범위: `status` 변경(잠금 해제 등), `assignedRoles` 부여, 부서 강제 이동 등.
- 인가 규칙 (Role/Permission 적용):
    - 관리자가 '사용자 관리 메뉴(Role)'에 진입할 수 있어야 함.
    - '수정 버튼(Permission)' 권한이 있어야 함.
    - C지향 (조직): 관리자가 본인보다 상위 직급이거나 타 부서인 사용자를 수정할 수 있는지에 대한 '맥락 기반 검증' 수행.
- 행위: `LOCKED` 상태를 `ACTIVE`로 변경하거나, 특정 사유로 계정을 직접 `WITHDRAWN` 처리.

### F. System-Update (HR 배치/시스템 자동)

- 수정 범위: `departmentId`, `position`, `jobRole`, 퇴사 시 `status` 변경 등.
- 인가 규칙: 별도의 사용자 인가는 없으나, `isRoleSyncedWithHR` 속성에 따라 시스템이 자동으로 `assignedRoles`를 재계산함.

---

## 이벤트 발행

### G. 로그 기록 이벤트 발행 (Auditlog Event Publishing)

- `User` 수정 액티비티가 완료되는 순간, 시스템은 `Audit Log` 모델을 생성합니다.
- "A 관리자가 B 사용자의 부서를 X에서 Y로 옮겼다"는 사실을 박제합니다.

## 외부 참조

### H. 메뉴 렌더링 외부 참조 (Menu Redering External Reference)

- 사용자가 로그인하면 `User` 모델의 `assignedRoles`를 기반으로 `Menu` 모델에서 접근 가능한 리스트만 필터링하여 UI에 던져줍니다.
- 페이지 내의 특정 버튼 클릭 시, 해당 `User`가 그 메뉴의 특정 `Permission`을 가졌는지 실시간 검증합니다.