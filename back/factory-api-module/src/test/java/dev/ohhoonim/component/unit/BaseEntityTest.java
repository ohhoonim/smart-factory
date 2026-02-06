package dev.ohhoonim.component.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import dev.ohhoonim.component.unit.order.model.Order;
import dev.ohhoonim.component.unit.order.model.OrderId;
import dev.ohhoonim.component.unit.order.model.OrderItem;
import dev.ohhoonim.component.unit.order.model.OrderStatus;
import dev.ohhoonim.component.unit.order.model.ProductVo;
import dev.ohhoonim.component.unit.user.model.UserId;

class BaseEntityTest {

    @Test
    @DisplayName("신규 엔티티 생성 시 모든 Auditing 정보가 현재 시점으로 자동 설정되어야 한다.")
    void newEntityAuditingTest() {
        OrderId orderId = OrderId.Creator.generate();
        String purchaserId = new UserId("user-123").toValue();
        String operator = "system_admin";

        Instant testStartTime = Instant.now();
        Order order = new Order(orderId, purchaserId, operator);
        Instant testEndTime = Instant.now();

        assertAll(() -> assertEquals(orderId, order.getId(), "생성 시 주입한 ID가 일치해야 함"),

                () -> assertFalse(order.getCreatedAt().isBefore(testStartTime),
                        "생성 시간은 테스트 시작 이후여야 함"),
                () -> assertFalse(order.getCreatedAt().isAfter(testEndTime),
                        "생성 시간은 테스트 종료 이전이어야 함"),

                () -> assertThat(order.getCreatedAt())
                        .isCloseTo(order.getModifiedAt(), within(1, ChronoUnit.MILLIS))
                        .as("최초 생성 시간과 수정 시간은 동일하다(1밀리초 이내에서)"),

                () -> assertEquals(operator, order.getCreatedBy(), "생성자 정보가 올바르게 기록되어야 함"),
                () -> assertEquals(operator, order.getModifiedBy(), "수정자 정보가 생성자와 동일하게 초기화되어야 함"));
    }

    @Test
    @DisplayName("엔티티 수정 시 createdAt은 유지되고 modifiedAt만 갱신되어야 한다.")
    void recordModificationTest() throws InterruptedException {
        String creator = "user_1";
        Order order =
                new Order(OrderId.Creator.generate(), UserId.from("buyer_1").toValue(), creator);

        Instant originalCreatedAt = order.getCreatedAt();
        Instant originalModifiedAt = order.getModifiedAt();
        String modifier = "admin_1";

        Thread.sleep(1000);
        order.confirm(modifier);

        assertAll(() -> assertEquals(originalCreatedAt, order.getCreatedAt(), "생성 시각은 변하면 안 됨"),
                () -> assertEquals(creator, order.getCreatedBy(), "생성자는 변하면 안 됨"),

                () -> assertTrue(order.getModifiedAt().isAfter(originalModifiedAt),
                        "수정 시각은 이전보다 이후여야 함"),
                () -> assertEquals(modifier, order.getModifiedBy(), "수정자 정보가 새롭게 업데이트되어야 함"),

                () -> assertNotEquals(order.getCreatedAt(), order.getModifiedAt(),
                        "수정 후에는 생성 시각과 수정 시각이 달라야 함"));
    }


    @Test
    @DisplayName("식별자(ID)가 동일하면 서로 다른 객체라도 equals 결과가 true여야 한다.")
    void identityEqualityTest() {
        OrderId sameId = OrderId.Creator.generate();
        String purchaserId = UserId.from("user-1").toValue();

        Order order1 = new Order(sameId, purchaserId, "system");
        Order order2 = new Order(sameId, purchaserId, "admin"); // 생성자/상태가 달라도 ID는 같음

        assertAll(() -> assertEquals(order1, order2, "ID가 같으면 두 엔티티는 동일한 것으로 간주되어야 함"),
                () -> assertNotSame(order1, order2, "메모리 주소는 서로 달라야 함 (다른 인스턴스)"),

                () -> assertEquals(order1.hashCode(), order2.hashCode(),
                        "동일한 엔티티는 동일한 hashCode를 가져야 함"),

                () -> assertEquals(order1, order1, "자기 자신과의 비교는 당연히 true여야 함"),

                () -> assertNotEquals(null, order1, "null과의 비교는 false여야 함"),
                () -> assertNotEquals("some-string", order1, "다른 타입과의 비교는 false여야 함"));
    }

    @Test
    @DisplayName("DB에서 복원된 엔티티는 과거의 시각 정보를 유지해야 하며, 수정 시 수정 시각만 갱신되어야 한다.")
    void reconstituteEntityTest() throws InterruptedException {
        OrderId savedId = OrderId.Creator.generate();
        String purchaserId = UserId.from("user-999").toValue();

        // 1년 전 시점 설정
        Instant pastCreatedAt = Instant.parse("2024-01-01T10:00:00Z");
        String originalCreator = "old_admin";

        // 6개월 전 수정 시점 설정
        Instant pastModifiedAt = Instant.parse("2024-06-01T15:30:00Z");
        String lastModifier = "old_manager";

        // 복원(Reconstitute) 공정을 통해 엔티티 부활
        Order restoredOrder = Order.reconstitute(savedId, purchaserId, OrderStatus.INIT,
                Collections.EMPTY_LIST, pastCreatedAt, originalCreator, pastModifiedAt, lastModifier);

        assertAll(
                () -> assertEquals(pastCreatedAt, restoredOrder.getCreatedAt(),
                        "복원된 생성 시각이 주입한 과거 시각과 일치해야 함"),
                () -> assertEquals(originalCreator, restoredOrder.getCreatedBy(),
                        "복원된 생성자 정보가 일치해야 함"),
                () -> assertEquals(pastModifiedAt, restoredOrder.getModifiedAt(),
                        "복원된 수정 시각이 주입한 과거 시각과 일치해야 함"),
                () -> assertEquals(lastModifier, restoredOrder.getModifiedBy(),
                        "복원된 수정자 정보가 일치해야 함"));

        // (Step 2): 복원된 엔티티에 새로운 수정 액티비티 발생
        String newModifier = "current_admin";
        Thread.sleep(1000); // 현재 시각과의 차이를 위해 미세 대기
        restoredOrder.confirm(newModifier);

        // (Step 2): 수정 후 상태 검증
        assertAll(
                () -> assertEquals(pastCreatedAt, restoredOrder.getCreatedAt(),
                        "수정 후에도 과거의 생성 시각은 절대 변하면 안 됨"),
                () -> assertTrue(restoredOrder.getModifiedAt().isAfter(pastModifiedAt),
                        "수정 시각은 과거의 수정 시각보다 이후(현재)여야 함"),
                () -> assertEquals(newModifier, restoredOrder.getModifiedBy(),
                        "수정자 정보가 현재 작업자로 갱신되어야 함"));
    }

    @Test
    @DisplayName("Getter로 반환된 객체를 외부에서 수정하더라도 엔티티 내부 상태는 보호되어야 한다.")
    void immutabilityTest() {
        OrderId orderId = OrderId.Creator.generate();
        Order order = new Order(orderId, UserId.from("user-1").toValue(), "system");
        order.addItem(new ProductVo("p1", "상품1", 1000), 1);

        // (Step 1: Instant와 String은 Java에서 이미 불변)
        // Instant는 불변 객체이므로 plusSeconds를 해도 새로운 객체를 반환할 뿐 원본은 변하지 않음
        Instant createdAt = order.getCreatedAt();
        createdAt.plusSeconds(10000);
        assertEquals(order.getCreatedAt(), createdAt, "외부의 시간 연산이 엔티티 내부 시각을 변하게 해서는 안 됨");

        // Step 2: 컬렉션 불변성 검증)
        List<OrderItem> items = order.getItems();

        // 가이드라인에 따라 Collections.unmodifiableList()를 사용했다면 예외가 발생해야 함
        assertThrows(UnsupportedOperationException.class, () -> {
            items.add(new OrderItem("p2", 2000, 1));
        }, "Getter로 얻은 리스트에 요소를 추가하면 예외가 발생해야 함");

        assertEquals(1, order.getItems().size(), "외부에서의 조작 시도에도 엔티티 내부 리스트 크기는 유지되어야 함");
    }

    @Test
    void instant() {
        var order = new Order(OrderId.Creator.generate(), new UserId("user-123").toValue(),
                "sys-admin");
        assertThat(order.getCreatedAt()).isCloseTo(order.getModifiedAt(),
                within(1, ChronoUnit.MILLIS));
    }
}


