## 메뉴 및 권한 (Menu/Permission) 도메인 모델

## 모델링 바이브

- Menu/Permission 바이브
    - 계층 구조 (Hierarchy): 대메뉴 > 중메뉴 > 소메뉴로 이어지는 트리 구조를 가집니다.
    - 세밀한 제어 (Fine-grained Control): "메뉴에 들어올 수 있는가(Role)"와 "특정 버튼을 누를 수 있는가(Permission)"를 분리하여 관리합니다.
    - 동적 가시성 (Dynamic Visibility): 권한이 없는 메뉴는 사용자에게 아예 보이지 않거나 비활성화되어야 합니다.
- 액티비티 흐름
    
    관리자가 먼저 구조를 짜고(H, I, A), 엔진이 상황에 맞춰 계산하고(D, E, B), 마지막에 맥락을 검증하여 실행(G, C)하는 순서로 정돈되었습니다.
    
    | 액티비티 구분 | 액티비티 명 | 상세 내용 (인가 엔진의 역할) |
    | --- | --- | --- |
    | 운영(관리) 단계 | H. 메뉴 트리 관리 | 시스템의 메뉴 구조(대/중/소) 정의 및 URL 매핑 |
    | 운영(관리) 단계 | I. 기능 정의 | 메뉴별 버튼(Permission) 등록 및 보안 정책(`isAudit`) 설정 |
    | 운영(관리) 단계 | J. 역할 권한 할당 | Role - Menu - Permission 간의 다대다 관계 설정 |
    | 인가 엔진(사용) 단계 | D. 권한 합산/유효성 | 로그인 시 다중/임시 Role의 유효성을 체크하여 합산(Union) |
    | 인가 엔진(사용) 단계 | B. 메뉴 렌더링 | 사용자가 볼 수 있는 메뉴 트리만 동적으로 구성하여 노출 |
    | 인가 엔진(사용) 단계 | G. 맥락/정책 검증 | 클릭 시 IP, SoD, 데이터 소유권 등을 따져 최종 승인 |
    | 인가 엔진(사용) 단계 | C. 버튼 권한 검증 | 최종 실행 및 `isAuditRequired`에 따른 로그 이벤트 발행 |
- 유비쿼터스 언어 점검
    - Menu Tree: 대/중/소 메뉴로 구성된 계층적 구조.
    - RBAC (Role-Based Access Control): 역할 기반의 접근 제어 방식.
    - Hidden Menu: 특정 권한자만 URL을 직접 입력하거나 특정 경로로만 진입할 수 있는 비노출 메뉴.
    - Functional Authority: 메뉴 진입 후 수행할 수 있는 개별 기능(Create, Update, Delete 등)에 대한 권한.
    - Union Permission: 다중 Role 보유 시 합산된 최종 권한.
    - Role Expiration: 설정된 일시가 지나면 자동으로 권한이 소멸되는 메커니즘.
    - Permission Override: 특정 상황에서 표준 Role 권한 외에 예외적으로 허용/차단하는 규칙.
    - Grant/Revoke: 특정 권한을 부여하거나 회수하는 행위.
    - Effective Date: 권한 할당이 실제 효력을 발휘하기 시작하는 시점.
    - Inheritance: 상위 메뉴 권한을 가지면 하위 메뉴 권한을 자동으로 가질 것인지에 대한 상속 규칙.
- 외부 도메인 모델과의 관계
    - Role-based 연결: `User` ↔ `Role` ↔ `Menu/Permission` 구조로 연결되어야 합니다.
    - 인가(Auth)와의 관계: 인가 엔진은 `User` 도메인 정보와 `Permission` 도메인 정보를 결합하여 "현재 버튼 클릭을 허용할지" 결정합니다.
    - 감사로그(Audit)와의 관계: `Permission` 모델의 `isAuditRequired` 속성은 어떤 행위가 로그로 박제되어야 하는지 결정하는 필터 정책이 됩니다.

---

## Model

### A. Menu (메뉴 엔티티)

- 구조 정보:
    - `menuId`: PK
    - `parentMenuId`: 상위 메뉴 ID (Self-reference로 계층 구조 형성)
    - `menuCode`: `USER_MGMT`, `LOG_VIEW` 등 고유 식별 코드
    - `menuName`: 화면에 표시될 이름
    - `sortOrder`: 출력 순서
    - `menuLevel`: 메뉴 계층 깊이 (1차, 2차, 3차)
    - `isExternalLink`: 외부 URL로 연결되는 메뉴인지 여부 (보안 체크 대상)
- 시스템 정보:
    - `urlPath`: 프론트엔드 라우팅 경로
    - `iconRes`: 메뉴 아이콘 리소스 정보
    - `isDisplay`: 메뉴판 노출 여부 (보안상 숨김 메뉴 처리용)

### B. Permission (권한 엔티티 - 버튼/기능)

메뉴 하위에 소속되어 구체적인 행위를 정의합니다.

- `permissionId`: PK
- `menuId`: FK (어떤 메뉴에 속한 기능인가)
- `permissionCode`: `BTN_CREATE`, `BTN_EXCEL_DOWN`, `API_SENSITIVE_VIEW`
- `permissionName`: "신규 등록", "엑셀 다운로드" 등
- `actionType`: `CREATE`, `READ`, `UPDATE`, `DELETE`, `DOWNLOAD` 등 행위 유형
- `description`: 해당 버튼이 수행하는 구체적인 비즈니스 로직 설명
- 보안 속성:
    - `isAuditRequired`: 이 기능을 수행할 때 Audit Log를 필수로 남겨야 하는지 여부
    - `is2faRequired`: 이 기능 클릭 시 Step-up 인증(2FA)을 요구할지 여부

임시 권한을 위해 `User`와 `Role` 사이의 관계 엔티티에 유효 기간을 도입합니다.

- 속성 추가: `roleAssignment` (User-Role 매핑)
    - `startedAt`: 권한 시작 일시
    - `expiredAt`: 권한 종료 일시
    - `isTemporary`: 임시 부여 여부 플래그
    - `assignedBy`: 해당 권한을 누가 부여했는가 (UserId)
    - `isDeny`: (선택 사항) 명시적 차단 권한인지 여부 (부정 우선 원칙용)

### 다중 Role 보유 시 권한 합산 규칙 (Union Rule)

사용자가 여러 부서의 일을 겸하거나, 프로젝트성으로 추가 Role을 부여받았을 때의 처리 방식입니다.

#### A. 합집합 원칙 (Additive/Union Approach)

- 바이브: 사용자가 가진 모든 Role의 권한을 합쳐서 가장 넓은 범위의 권한을 부여합니다.
- 로직:
    - Role A: [메뉴 1, 메뉴 2] 접근 가능 / [조회] 버튼 권한
    - Role B: [메뉴 2, 메뉴 3] 접근 가능 / [수정] 버튼 권한
    - 결과 (Effective): [메뉴 1, 2, 3] 모두 접근 가능하며, 메뉴 2에서는 [조회, 수정] 버튼을 모두 사용할 수 있음.

#### B. 충돌 해결: 부정 우선 원칙 (Deny Overrides) [선택적 보안 정책]

- 만약 특정 Role에서 보안상의 이유로 특정 메뉴를 '명시적 차단'했다면, 다른 Role에 권한이 있어도 차단이 우선되는 방식입니다. (금융권 등 고보안 시스템에서 사용)

### 액티비티 대응성 검토

| 관련 액티비티 | 체크 포인트 (속성 보강 제언) | 이유 |
| --- | --- | --- |
| H. 메뉴 트리 관리 | `menuLevel` (Depth) | 트리 구조 렌더링 시 몇 단계 메뉴인지 빠르게 판단하기 위함 |
| I. 기능 정의 | `actionType` (CRUD) | `isAuditRequired`와 연동되어 로그에 '등록', '수정' 등을 명시하기 위함 |
| J. 역할 권한 할당 | `isDeny` (부정 플래그) | '부정 우선 원칙(Deny Overrides)' 로직을 모델에서 지원하기 위함 |
| D. 권한 합산/유효성 | `assignmentReason` | 임시 권한 부여 시 "왜 부여했는지" 기록 (감사 목적) |
| G. 맥락/정책 검증 | `isIpRestricted` | 메뉴/버튼별로 IP 제한을 강하게 걸지 말지 결정하는 속성 |

### 도메인 연결성 검토

- 보안 시너지: `Permission.isAuditRequired` (메뉴도메인) → `AuditLog.actionType` (로그도메인)으로 이어지는 연결 고리가 완벽합니다.
- 조직 지향성: `User.departmentId` (유저도메인) → `Activity G-2` (맥락검증)로 이어지는 로직이 `C지향` 설계를 잘 반영하고 있습니다.
- 유연성: `roleAssignment`의 `expiredAt` 속성이 `Activity D, F`와 맞물려 별도의 수동 삭제 없이도 권한이 자동 회수되는 구조가 인상적입니다.

---

## Activity

## 관리 및 운영 (Admin Activities)

### H. 메뉴 트리 관리 (Menu Tree Management)

- 행위: 대/중/소 메뉴의 계층 구조를 생성, 수정, 삭제합니다.
- 로직: 상위 메뉴(`parentMenuId`)를 지정하여 트리 구조를 형성하고, `sortOrder`를 통해 UI 노출 순서를 결정합니다. `urlPath`를 매핑하여 실제 시스템 페이지와 연결합니다.

### I. 기능(Permission) 정의 및 정책 설정

- 행위: 각 메뉴 안에서 수행할 수 있는 구체적인 버튼(Permission)들을 등록합니다.
- 보안 바이브: 단순히 버튼을 등록하는 것에 그치지 않고, `isAuditRequired`나 `is2faRequired` 같은 보안 정책 플래그를 여기서 결정합니다. 이것이 인가 엔진의 작동 기준이 됩니다.

### J. 역할별 권한 할당 (Role-Permission Assignment)

- 행위: 특정 `Role`이 접근할 수 있는 메뉴와 그 안에서 행사할 수 있는 권한 집합을 맵핑합니다.
- 바이브: `Permission Mapping(A)`이 이 액티비티의 구체적인 실행 형태입니다. 관리자는 체크박스 형태로 "인사팀장 Role은 급여 수정 권한을 가짐"과 같이 설정합니다.

## 인가 엔진 작동 (Engine Activities)

### D. 권한 합산 및 유효성 검증 (Permission Consolidation)

- 행위: 사용자의 모든 Role을 모으고, 현재 시간(`now()`)을 기준으로 기간이 만료된 임시 Role을 제외(필터링)한 후 최종 합집합(`Union`)을 구합니다.
- 규칙: 1. 기간 만료된 임시 Role 제거.
    
    2. 중복된 메뉴/버튼 권한은 하나로 합침(Union).
    
    3. `isHighPrivilege` 속성이 포함된 Role이 하나라도 있다면, 해당 세션에 2FA 요구 플래그를 세움.
    

### E. 권한 대행 활성화 (Delegation Activation)

- 행위: 사용자가 '대리인 모드'를 활성화할 때 발생합니다.
- 로직: `User` 모델의 `delegatedFromUserId` 정보를 확인하여 원 소유자의 Role을 합산 목록에 추가하고, 이 사실을 `Audit Log`에 기록합니다.

### B. 동적 메뉴 렌더링 (Menu Rendering)

1. 사용자가 로그인하면 `User.assignedRoles`를 가져옵니다.
2. 해당 Role들에 연결된 모든 `Menu`를 조회합니다.
3. 계층 구조로 조립(Tree 구조화)하여 사용자 화면에 전송합니다.

### G. 맥락 및 정책 검증 (Contextual Policy Evaluation) 상세

이 액티비티는 버튼 레벨 권한 검증(C) 직후 혹은 동시에 실행되며, `effectivePermissions`가 있더라도 최종적으로 행위를 차단하거나 추가 절차를 요구할 수 있습니다.

- G-1. 네트워크 맥락 검증 (Network Context):
    - 행위: 현재 요청의 클라이언트 IP와 `User.allowedIpRanges`를 대조합니다.
    - 로직: 허용되지 않은 외부 IP에서 접근 시, 일반 메뉴는 허용하되 `isHighPrivilege`가 설정된 권한(예: 개인정보 다운로드)은 즉시 차단하거나 2FA를 강제합니다.
- G-2. 데이터 소유권 및 관계 검증 (Relationship Context):
    - 행위: 수정하려는 데이터의 주체와 사용자의 `departmentId`를 대조합니다.
    - 로직: '인사팀장' 권한이 있더라도, 조회하려는 직원이 '본인 부서' 소속이 아니면 행위를 거부하는 C지향(조직) 필터링을 수행합니다.
- G-3. 직무 분리 검증 (SoD: Separation of Duties):
    - 행위: 해당 트랜잭션의 이전 단계 수행자와 현재 요청자를 대조합니다.
    - 로직: `isSodExempt`가 `false`인 경우, 본인이 기안한 결재 건에 대해 본인이 승인 버튼을 누르는 행위를 차단합니다.
- G-4. 보안 정책 피드백 (Step-up Trigger):
    - 행위: `is2faRequired` 속성이 있는 버튼 클릭 시 현재 세션의 인증 수준을 확인합니다.
    - 로직: 최근 30분 내 2FA 이력이 없다면 행위를 일시 중지하고 2FA 인증 페이지로 사용자를 유도합니다.

### C. 버튼 레벨 권한 검증 (Action Authorization)

1. 사용자가 특정 버튼을 클릭합니다.
2. 시스템은 해당 사용자의 `effectivePermissions` 목록에 해당 `permissionCode`가 있는지 확인합니다.
3. `isAuditRequired`가 `true`라면, 행위 완료 후 즉시 Audit Log 이벤트를 발행합니다.

---