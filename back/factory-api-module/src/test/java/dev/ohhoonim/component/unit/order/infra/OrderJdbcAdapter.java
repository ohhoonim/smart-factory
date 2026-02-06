package dev.ohhoonim.component.unit.order.infra;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import dev.ohhoonim.component.unit.MasterCode;
import dev.ohhoonim.component.unit.order.model.Order;
import dev.ohhoonim.component.unit.order.model.OrderId;
import dev.ohhoonim.component.unit.order.model.OrderItem;
import dev.ohhoonim.component.unit.order.model.OrderStatus;
import dev.ohhoonim.component.unit.order.port.OrderPort;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderJdbcAdapter implements OrderPort {
    private final JdbcClient jdbcClient;

    @Override
    @Transactional // 애그리거트 단위의 원자성 보장
    public void save(Order order) {
        // 1. Order Root 저장 (UPSERT)
        upsertOrder(order);

        // 2. 기존 OrderItem 삭제 (Orphan Removal 재현)
        deleteOrderItems(order.getId());

        // 3. 현재 OrderItem 일괄 삽입
        insertOrderItems(order);
    }

    private void upsertOrder(Order order) {
        String sql =
                """
                        INSERT INTO orders (id, purchaser_id, status, total_amount, created_at, created_by, modified_at, modified_by)
                        VALUES (:id, :purchaserId, :status, :totalAmount, :createdAt, :createdBy, :modifiedAt, :modifiedBy)
                        ON CONFLICT (id) 
                        DO UPDATE SET
                            status = EXCLUDED.status,
                            total_amount = EXCLUDED.total_Amount,
                            modified_at = EXCLUDED.modified_At,
                            modified_by = EXCLUDED.modified_By
                        """;

        jdbcClient.sql(sql).param("id", order.getId().toValue())
                .param("purchaserId", order.getPurchaseUserId())
                .param("status", order.getStatus().masterCode()) // "01", "02" 등 코드값
                .param("totalAmount", order.getTotalAmount())
                .param("createdAt", Timestamp.from(order.getCreatedAt()))
                .param("createdBy", order.getCreatedBy())
                .param("modifiedAt", Timestamp.from(order.getModifiedAt()))
                .param("modifiedBy", order.getModifiedBy()).update();
    }

    private void deleteOrderItems(OrderId orderId) {
        jdbcClient.sql("DELETE FROM order_items WHERE order_id = :orderId")
                .param("orderId", orderId.toValue()).update();
    }

    private void insertOrderItems(Order order) {
        if (order.getItems().isEmpty())
            return;

        String sql =
                "INSERT INTO order_items (order_id, product_id, price, quantity) VALUES (?, ?, ?, ?)";

        // Batch Update로 성능 최적화
        order.getItems().forEach(item -> jdbcClient.sql(sql)
                .params(order.getId().toValue(), item.productId(), item.price(), item.quantity())
                .update());
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        // 1. Order 기본 정보 조회
        var orderRow = jdbcClient.sql("SELECT * FROM orders WHERE id = :id")
                .param("id", id.toValue()).query(OrderRow.class) // DB 전용 매핑 record 또는 Map 활용
                .optional();

        if (orderRow.isEmpty())
            return Optional.empty();

        // 2. 하위 OrderItem 리스트 조회
        List<OrderItem> items = jdbcClient.sql("SELECT * FROM order_items WHERE order_id = :id")
                .param("id", id.toValue())
                .query((rs, rowNum) -> new OrderItem(rs.getString("product_id"),
                        rs.getLong("price"), rs.getInt("quantity")))
                .list();

        // 3. 보강된 reconstitute를 통해 완전한 애그리거트로 부활
        OrderRow row = orderRow.get();
        return Optional.of(Order.reconstitute(OrderId.Creator.from(row.id()), row.purchaserId(),
                MasterCode.enumCode(OrderStatus.class, row.status()).orElseThrow(
                        () -> new IllegalStateException("정의되지 않은 주문 상태 코드입니다: " + row.status())),
                items, // 조회한 리스트 주입
                row.createdAt(), row.createdBy(), row.modifiedAt(), row.modifiedBy()));
    }

}


record OrderRow(String id, String purchaserId, String status, long totalAmount, Instant createdAt,
        String createdBy, Instant modifiedAt, String modifiedBy) {
}
