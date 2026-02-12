## 알림 및 통지 (Notification/Alert) 도메인

## 모델링 바이브

- Notification/Alert 바이브
    - 알림 도메인은 각 도메인에서 발생하는 이벤트를 가공하여 사용자에게 전달합니다.
    - Reliable Delivery (신뢰할 수 있는 전달): 결재나 보안 경고처럼 중요한 정보가 유실되지 않도록 보장합니다.
    - Contextual Routing (맥락적 라우팅): 긴급도에 따라 자체 알림(In-app), SMS, 이메일을 적절히 선택합니다.
    - Non-Intrusive (비방해성): 사용자가 업무에 집중할 수 있도록 알림의 빈도와 채널을 구독 설정(`Subscription`)에 따라 제어합니다.
- 유비쿼터스 언어 점검
    - Trigger Event (발생 이벤트): 알림을 일으키는 비즈니스 사건입니다. (예: 결재 요청 완료, 비정상 IP 접속 감지)
    - Multi-Channel Dispatch (멀티 채널 발송): 하나의 메시지를 SMS, 알림톡, 이메일, 시스템 알람 등 여러 매체를 통해 동시에 혹은 순차적으로 내보내는 행위입니다.
    - Fallback (폴백): 우선순위가 높은 발송 채널(예: PUSH)이 실패했을 때, 차선책(예: SMS)으로 자동 전환하여 발송하는 안정성 확보 메커니즘입니다.
    - Payload (페이로드): 알림 템플릿 내의 변수(사용자명, 문서번호 등)를 채우기 위해 타 도메인으로부터 전달받은 실제 데이터 덩어리입니다.
    - Read Trace (읽음 추적): 사용자가 메시지를 단순히 수신했는지, 아니면 실제로 클릭하여 내용을 확인했는지를 추적하는 활동입니다.
    - Subscription Category (구독 카테고리): 사용자가 수신 여부를 선택할 수 있는 알림의 분류입니다. (예: 마케팅성 제외, 결재 알림 필수 등)
    - In-app Notification (종알림/자체 알람): 별도의 외부 매체 없이 백오피스 시스템 우측 상단 등 내부 UI에서 보여주는 휘발성 알림입니다.
- 외부 도메인과의 관계
    1. Notification ↔ Approval: 결재선 상태가 `CURRENT`로 변하는 즉시 알림 도메인이 승인자에게 푸시를 보냅니다.
    2. Notification ↔ Shift: 근무 투입 30분 전, 또는 대근이 배정되었을 때 알림을 발송합니다.
- 액티비티 흐름
    
    | 구분 | ID | 액티비티 명 | 상세 내용 및 비즈니스 영향 |
    | --- | --- | --- | --- |
    | 운영 | X | 알림 규칙 및 라우팅 설정 | 이벤트 종류에 따른 수신자 선정 및 최적 발송 채널 매핑 |
    | 실행 | Y | 멀티 채널 메시지 발송 | 이메일, SMS, 시스템 알람 등 매체별 실제 전송 및 재시도 |
    | 추적 | Z | 읽음 추적 및 피드백 분석 | 사용자의 수신/확인 여부 모니터링 및 미확인 시 후속 조치 |

---

## Model

- A. Notification Template (알림 템플릿)
    - `templateId`, `triggerEvent` (예: APP_REQ, SHIFT_START, SEC_BREACH)
    - `titleLayout`, `contentLayout` (변수 치환이 가능한 메시지 규격)
    - `priority`: 긴급(Immediate), 보통(Normal), 요약(Batch)
- B. Message (발송 메시지)
    - `messageId`, `receiverId` (User FK), `templateId` (FK)
    - `channelType`: `SYSTEM`(백오피스 내 종알림), `SMS`(문자), `EMAIL`(이메일), `PUSH`(모바일 푸시)
    - `sendStatus`: `READY`, `PROCESSING`, `SUCCESS`, `FAIL`
    - `readYn`: 사용자의 확인 여부 (특히 `SYSTEM` 채널에서 중요)
- C. Subscription (수신 설정)
    - `userId`, `category` (결재, 근태, 보안, 시스템 공지)
    - `isAgreed`: 수신 동의 여부
    - `preferredChannel`: 카테고리별 선호 채널
- ‘sendStatus’ 상태 변화 흐름도
    
    | 현재 상태 (Status) | 전이 이벤트 (Event) | 결과 상태 (Target) | 비고 |
    | --- | --- | --- | --- |
    | (None) | 알림 생성 이벤트 발생 | READY | 발송 큐(Queue)에 적재된 초기 상태 |
    | READY | 발송 엔진의 작업 시작 | PROCESSING | 메시지 조립 및 채널 사업자 API 호출 중 |
    | PROCESSING | 채널 사업자의 성공 응답 수신 | SUCCESS | 발송 완료 (수신 여부와는 별개) |
    | PROCESSING | 기술적 오류 및 타임아웃 발생 | FAIL | 발송 실패 (재시도 로직의 대상) |

- ‘readYn’과 ‘sendStatus’와의 관계
    - `sendStatus == SUCCESS` && `readYn == N`: 사용자에게 알림은 전달되었으나 아직 내용을 인지하지 못한 상태. (중요 결재의 경우 재알림 대상)
    - `sendStatus == SUCCESS` && `readYn == Y`: 사용자가 알림을 클릭하여 상세 페이지로 진입하거나 확인을 마친 상태.

---

## Activity

### X. 알림 규칙 및 라우팅 설정 (Notification Routing)

- 행위: 특정 이벤트 발생 시 발송할 템플릿과 기본 채널, 수신 대상을 매핑합니다.
- 로직: 1. 타 도메인(Approval, Shift 등)에서 발행한 이벤트를 수신합니다.
2. 해당 이벤트의 중요도와 사용자의 `Subscription` 설정을 대조합니다.
3. 최적의 발송 채널(예: 결재는 `SYSTEM`+`PUSH`, 보안 경고는 `SMS`)을 결정합니다.
- 비즈니스 영향: 정보의 중요도에 따른 차별화된 전달력을 확보하고, 무분별한 알림으로 인한 피로도를 감소시킵니다.

### Y. 멀티 채널 메시지 발송 (Multi-Channel Dispatch)

- 행위: 결정된 채널별 프로토콜(SMTP, SMS Gateway 등)을 통해 실제 메시지를 송신합니다.
- 로직: 1. 메시지 내의 변수(사용자 이름, 결재 문서명 등)를 실시간 데이터로 치환합니다.
2. 각 채널 사업자(Provider)의 API를 호출하여 발송합니다.
3. 발송 실패 시 재시도(Retry) 로직을 가동하거나 보조 채널(Fallback)로 전환합니다.
- 비즈니스 영향: 시스템 장애나 네트워크 문제 상황에서도 중요한 비즈니스 알림이 사용자에게 도달할 수 있는 가용성을 보장합니다.

### Z. 읽음 추적 및 피드백 분석 (Read Trace & Analysis)

- 행위: 발송된 알림을 사용자가 실제로 확인했는지 추적합니다.
- 로직: 1. 자체 시스템 알림의 경우 클릭 시 `readYn`을 업데이트합니다.
2. 이메일이나 SMS는 발송 성공 여부(Delivery Report)를 기록합니다.
3. 장기간 미확인된 중요 알림(예: 최종 결재 대기)은 재발송 이벤트를 트리거합니다.
- 비즈니스 영향: 의사결정 지연을 방지하고, 보안 사고 발생 시 사용자가 인지했는지에 대한 증적을 남깁니다.

---