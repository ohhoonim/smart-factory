## Matthew's Architecture: 5-Step Policy-Driven Spec

마스터, 요청하신 대로 **`docs/architecture-spec.md`**에 박제될 **Matthew's Architecture**의 최종 명세서를 작성했어. 이 문서는 우리 프로젝트의 헌법이자, 새로운 팀원이 합류했을 때 건네줄 가장 강력한 가이드북이 될 거야.

---

# 🏛️ Matthew's Architecture: 5-Step Policy-Driven Spec

이 문서는 헥사고날 아키텍처를 실무적으로 변형하고, DDD의 전략적 설계를 극대화한 **Matthew's Architecture**의 표준 공정을 정의한다.

## 📌 Core Philosophy

> "Service는 배달부(Messenger)일 뿐, 뇌(Brain)는 **Model**에, 법(Law)은 **Policy**에 둔다."
> 

---

## 🏗️ The 5-Step Standard Process

### Step 1: 도메인 자아 확립 (Domain Model Discovery)

비즈니스의 핵심 상태와 행위를 관리하는 **Aggregate Root(AR)**를 정의한다.

- **원칙**: 데이터 필드가 아닌 '상태 변화의 규칙'과 '생애주기'에 집중한다.
- **핵심**: AR 내부에서만 상태 변경이 가능하도록 캡슐화하며, 외부로부터의 직접적인 필드 수정을 금지한다.

### Step 2: 법전 정의 (Policy Abstraction)

비즈니스 제약 조건 중 변하기 쉬운 규칙(보안, 한도, 계산 로직)을 인터페이스로 추상화한다.

- **원칙**: 도메인 모델이 프레임워크(Spring)나 환경 설정에 오염되지 않도록 보호한다.
- **효과**: 정책의 변경이 모델이나 서비스의 코드 수정을 유발하지 않도록 결합도를 낮춘다.

### Step 3: 도구 제작 (Activity Implementation)

도메인이 외부 세계와 소통하기 위한 구체적인 기술(DB, Storage, 외부 API)을 구현한다.

- **원칙**: 헥사고날의 **Output Adapter** 역할을 수행하며, 기술적 복잡성을 이 계층에 가둔다.
- **명칭**: `QueryActivity`(조회), `CommandActivity`(변경), `StorageActivity`(물리 저장) 등으로 세분화한다.

### Step 4: 흐름 조율 (Service Orchestration)

서비스는 비즈니스 로직을 구현하지 않고, 모델과 액티비티를 연결하는 오케스트레이션만 담당한다.

- **원칙**: **Thin Service**. 트랜잭션 경계 설정과 전체적인 흐름 제어에만 집중한다.
- **프로세스**: `Activity로 조회 -> Policy를 Model에 주입하며 비즈니스 수행 -> Activity로 결과 저장`.

### Step 5: 반복 공정 검증 (Multilayered Testing)

각 계층의 특성에 맞는 테스트를 통해 공정의 완결성을 검증한다.

- **Model Test**: 정책을 Mocking하여 비즈니스 로직의 순수성을 ms 단위로 검증.
- **Adapter Test**: Testcontainers 등을 활용하여 실제 인프라(DB 등)와의 정합성 검증.
- **Service Test**: 각 컴포넌트 간의 호출 흐름만 검증 (로직 테스트 지양).

---

## 📊 Legacy vs Matthew's Architecture

| **구분** | **Legacy (Layered)** | **Matthew's Architecture** |
| --- | --- | --- |
| **핵심 로직 위치** | Service (Fat Service) | **Domain Model & Policy** |
| **변경 대응력** | 코드 전반의 수정 필요 | **Policy 구현체** 교체만으로 대응 |
| **코드 가독성** | 절차지향적, 명령형 | 객체지향적, 선언적 |
| **테스트 전략** | 통합 테스트 중심 (무거움) | **모델 단위 테스트** 중심 (가벼움) |

---

## 🛠️ Implementation Guidelines

1. **Strict Encapsulation**: AR 외부로 내부 컬렉션을 노출할 때는 반드시 `Collections.unmodifiableList()` 등을 사용하여 방어적 복사를 수행한다.
2. **Explicit Policy Injection**: 모델의 메서드가 정책을 필요로 할 경우, 항상 파라미터로 정책 인터페이스를 명시적으로 전달받는다.
3. **No Logic in Activity**: Activity는 오직 데이터의 '이동'과 '변환'에만 집중하며, 어떤 비즈니스 판단도 내리지 않는다.

---

### 🛡️ Gemini의 마침표

마스터, 이 명세서는 단순한 텍스트가 아니라 우리가 함께 고민하고 증명해낸 **'승리의 기록'**이야. 이 가이드라인이 있는 한, 어떤 복잡한 도메인이 와도 우리는 당황하지 않고 1단계부터 차근차근 밟아 나갈 수 있어.

**"자, 이 아키텍처를 적용할 다음 도메인 모듈을 바로 생성해 볼까? 아니면 이 문서를 바탕으로 프로젝트의 전체 패키지 구조를 한 번 재정비해 볼까?"**