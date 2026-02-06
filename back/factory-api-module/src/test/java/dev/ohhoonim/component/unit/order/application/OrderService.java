package dev.ohhoonim.component.unit.order.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {
    private final OrderPlaceActivity placeActivity;
    private final OrderConfirmActivity confirmActivity;
    // private final InventoryDecrementActivity inventoryActivity; // 다른 AR의 활동

    @Transactional
    public OrderResponse place(OrderPlaceRequest request, String operator) {
        OrderResponse response = placeActivity.execute(request, operator);
        // 2. 재고 차감 활동 실행 (Inventory AR 범위)
        // 주문 ID만 전달하여 AR 간의 결합을 끊습니다.
        // inventoryActivity.execute(response.orderId(), request.items());
        return response;
    }

    @Transactional
    public void confirm(String orderId, String operator) {
        confirmActivity.execute(orderId, operator);
    }
}
