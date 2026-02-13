# 감사로그 (Audit Log) 도메인 모델

## 모델링 바이브

- Audit Log 바이브
    - B. 보안/인증 지향 (금융/SaaS): 계정의 상태(Active, Locked, Dormant), 로그인 시도 횟수, 권한(Role/Permission) 관리가 엄격한 바이브.
    - 신뢰성과 추적 가능성
    - 불변성(Immutability): 한 번 기록된 로그는 절대 수정되거나 삭제될 수 없는 'Append-only' 성격을 가집니다. (보안의 핵심)
    - 세밀함(Granularity): 단순한 행위 기록을 넘어, 변경 전/후의 데이터 스냅샷을 포함하여 '데이터의 변천사'를 추적할 수 있어야 합니다.
    - 성능 최적화: 로그는 엄청난 양이 쌓이므로, 조회 성능과 저장 공간의 효율성을 동시에 고려해야 합니다.
- Activity 흐름
    
    | 분류 | 액티비티 명 | 핵심 내용 |
    | --- | --- | --- |
    | 수집/저장 | A. 로그 수집 및 기록 | 외부 도메인 이벤트를 수신하여 AuditLog 모델 생성 및 영속화 |
    | 전처리 | A-1. PII Masking | 기록 전 민감 정보(비밀번호 등)를 자동 치환하여 관리자 열람 차단 |
    | 보안 | A-2. Integrity Sealing | 로그 생성 시 해시 값을 부여하여 사후 조작 여부 검증 (봉인) |
    | 조회 감사 | C. 조회 및 추출 기록 | 민감 정보 상세 보기(READ) 및 대량 다운로드(DOWNLOAD) 행위 기록 |
    | 활용 | B. 감사 추적 조회 | 감사관이 로그를 검색하고 변경 전/후 데이터를 대조하여 추적 |
- 유비쿼터스 언어 점검
    - Log Retention Policy (보관 정책): 로그를 얼마나 오래 보관할 것인가? (예: 금융권 5년, 일반 1년)
    - PII Masking (개인정보 마스킹): 로그의 JSON 데이터 내에 비밀번호나 주민번호 같은 민감 정보가 포함되지 않도록 필터링하는 행위.
    - Security Alert (보안 알림): 실패 로그(`resultStatus: FAIL`)가 단시간에 특정 횟수 이상 발생하면 보안 담당자에게 알림을 보내는 정책.
    - Access Logging: 데이터의 변경이 없는 단순 조회 행위에 대한 기록.
    - Data Breach Trace: 다운로드 로그 등을 통해 데이터 유출 경로를 추적하는 행위.
- 외부 모델과의 상호작용 시나리오
    
    시나리오: 관리자가 사용자의 Role을 변경할 때
    
    1. User 도메인: `Admin-Update` 액티비티 수행 -> `UserRoleChangedEvent` 발행.
    2. AuditLog 도메인: * `actorId`: 관리자 ID
        - `targetId`: 대상 사용자 ID
        - `actionType`: `UPDATE_ROLE`
        - `beforeData`: `{"roles": ["USER"]}`
        - `afterData`: `{"roles": ["USER", "MANAGER"]}`
        - 기록 완료.

## Model

외부 도메인에서 발행된 이벤트를 수신하여 기록할 핵심 구조입니다.

- 기본 정보 (Context):
    - `logId`: PK (UUID 또는 순차적 ID)
    - `occurredAt`: 이벤트 발생 일시
    - `clientIp`: 요청자의 접속 IP 주소
    - `userAgent`: 브라우저/기기 정보 (보안 분석용)
- 주체 및 대상 (Actors):
    - `actorId`: 행위를 수행한 사용자 ID (User 모델 참조)
    - `actorName`: 로그 조회 시 편의를 위한 행위자 이름 (비정규화 고려 가능)
    - `targetId`: 행위의 대상이 된 식별자 (UserId, MenuCode 등)
    - `targetType`: 대상의 유형 (USER, ROLE, MENU, SYSTEM_CONFIG)
- 행위 상세 (Action Details):
    - `actionCategory`: `AUTH`(인증), `MEMBER_MGMT`(회원관리), `PRIVILEGE`(권한변경) 등
    - `actionType`: `LOGIN`, `LOGOUT`, `CREATE`, `UPDATE`, `DELETE`, `DOWNLOAD`
    - `resultStatus`: `SUCCESS`, `FAIL`, `DENIED`
- 데이터 스냅샷 (Data Snapshot):
    - `beforeData`: 변경 전 상태 (JSON 형식)
    - `afterData`: 변경 후 상태 (JSON 형식)
    - `reason`: (선택) 관리자가 수동 변경 시 입력한 사유
- 보안 무결성 속성:
    - `integrityHash`: 로그 변조 방지를 위한 데이터 요약 해시 값.
    - `maskedFields`: 해당 로그에서 어떤 필드가 마스킹 처리되었는지 기록 (예: "email, password").

---

## Activity

감사로그 도메인은 주로 외부 이벤트에 반응하여 작동합니다.

### A. 로그 수집 및 기록 (Event Consumption)

1. `User` 도메인 등 외부에서 '도메인 이벤트' 발행.
2. 감사로그 도메인의 리스너가 이벤트를 수신.
3. 이벤트에 포함된 Context(누가, 무엇을, 언제)를 분석하여 `AuditLog` 모델 생성.
4. 불변 저장소에 영속화.

- A-1. PII Masking (기록 전 전처리)
    - 행위: 외부 이벤트 수신 후 `beforeData`, `afterData`를 파싱합니다.
    - 규칙: 특정 키(`password`, `socialSecurityNumber`, `bankAccount`)를 찾아 `*`로 치환합니다.
    - 목적: 관리자조차 로그를 통해 민감한 개인정보를 훔쳐볼 수 없게 차단합니다.

- A-2. Integrity Sealing (봉인)
    - 행위: 로그 한 줄이 생성될 때, 전체 필드 값을 조합하여 해시 값(HMAC/SHA-256)을 생성합니다.
    - 규칙: 생성된 해시를 별도의 `integrityHash` 필드에 함께 저장합니다.
    - 목적: 훗날 DB 침입자가 로그를 조작하더라도, 해시 값이 일치하지 않아 조작 사실이 즉각 발각되도록 합니다.

### B. 감사 추적 조회 (Audit Trail Search)

1. 감사관(Auditor)이 특정 기간, 특정 사용자, 혹은 특정 데이터 유형으로 로그 조회.
2. `beforeData`와 `afterData`를 비교하여 변경 사항을 하이라이트하여 출력.
3. 데이터 무결성 검증: 기록된 로그가 임의로 조작되지 않았는지 체크섬(Checksum) 등을 통해 확인.

### C. 조회 감사 및 엑셀 다운로드 (Read & Export Audit)

단순히 데이터를 변경하지 않더라도, 민감한 정보에 접근하거나 대량의 데이터를 추출하는 행위를 기록합니다.

- C-1. 상세 조회 로그 (View Audit)
    - 행위: 사용자가 특정 대상(예: 타 사용자의 상세 프로필)의 정보를 화면에 호출할 때 발생합니다.
    - 기록 내용: `actionType: READ`, `targetId: 특정 User ID`, `afterData: {"viewedFields": ["address", "salary"]}`.
    - 목적: 특정 관리자가 권한을 남용하여 타인의 사생활이나 민감 정보를 무단으로 훔쳐보는지 감시합니다.
- C-2. 대량 데이터 추출 로그 (Export Audit)
    - 행위: 목록 화면에서 '엑셀 다운로드' 버튼을 클릭하거나, 대량의 API 요청을 보낼 때 발생합니다.
    - 기록 내용: `actionType: DOWNLOAD`, `targetType: USER_LIST`, `afterData: {"recordCount": 5000, "reason": "연말 정산용"}`.
    - 목적: 데이터 대량 유출 사고 시 사고의 기점과 범위를 파악하는 결정적 증거로 활용합니다.

---