package dev.ohhoonim.component.unit.order.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import dev.ohhoonim.component.unit.BaseEntity;

public class Order extends BaseEntity<OrderId> {

    private final String purchaserUserId; // 외부 애그리거트는 ID로 참조
    private OrderStatus status; // MasterCode 활용
    private final List<OrderItem> items = new ArrayList<>();
    private long totalAmount;

    // 가이드라인 준수: 생성자는 internal(package-private)로 격리
    public Order(OrderId id, String purchaserUserId, String operatorUserId) {
        super(id, operatorUserId);
        this.purchaserUserId = purchaserUserId;
        this.status = OrderStatus.INIT;
        this.recordModification(operatorUserId);
    }

    // 2. DB에서 읽어올 때 (4단계: infra에서 사용)
    public static Order reconstitute(OrderId id, String purchaserUserId, OrderStatus status,
            List<OrderItem> items, Instant createdAt, String createdBy, Instant modifiedAt,
            String modifiedBy) {
        return new Order(id, purchaserUserId, status, items, createdAt, createdBy, modifiedAt, modifiedBy);
    }

    private Order(OrderId id, String purchaserUserId, OrderStatus status, List<OrderItem> items,
            Instant createdAt, String createdBy, Instant modifiedAt, String modifiedBy) {
        super(id, createdAt, createdBy, modifiedAt, modifiedBy); // [B] 생성자 호출
        this.purchaserUserId = purchaserUserId;
        this.status = status;
        this.items.addAll(items); // 외부에서 읽어온 아이템들 주입
        calculateTotalAmount(); // 복원된 상태를 기반으로 합계 재계산
    }

    /**
     * 액티비티 지원 행위: 상품 추가
     * 모델이 스스로 비즈니스 규칙(금액 계산, 유효성 검증)을 판단함
     */
    public void addItem(ProductVo product, int quantity) {
        if (this.status != OrderStatus.INIT) {
            throw new IllegalStateException("주문 초기 상태에서만 상품을 추가할 수 있습니다.");
        }

        OrderItem newItem = new OrderItem(product.id(), product.price(), quantity);
        this.items.add(newItem);
        calculateTotalAmount();
    }

    /**
     * 액티비티 지원 행위: 주문 확정
     */
    public void confirm(String operator) {
        if (this.status != OrderStatus.INIT) {
            throw new IllegalStateException("주문 초기 상태에서만 확정이 가능합니다.");
        }
        this.status = OrderStatus.CONFIRMED;
        this.recordModification(operator);
    }

    private void calculateTotalAmount() {
        this.totalAmount = items.stream().mapToLong(OrderItem::getSubTotal).sum();
    }

    // 설계 격리: 외부로 노출할 때는 불변 리스트로 제공
    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public OrderStatus getStatus() {
        return status;
    }

    public long getTotalAmount() {
        return totalAmount;
    }

    public String getPurchaseUserId() {
        return this.purchaserUserId;
    }
}
