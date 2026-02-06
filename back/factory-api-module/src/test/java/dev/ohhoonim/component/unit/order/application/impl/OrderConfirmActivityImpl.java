package dev.ohhoonim.component.unit.order.application.impl;

import org.springframework.stereotype.Component;
import dev.ohhoonim.component.unit.order.application.OrderConfirmActivity;
import dev.ohhoonim.component.unit.order.model.Order;
import dev.ohhoonim.component.unit.order.model.OrderId;
import dev.ohhoonim.component.unit.order.port.OrderPort;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderConfirmActivityImpl implements OrderConfirmActivity {

    private final OrderPort orderPort;

    @Override
    public void execute(String orderId, String operator) {
        OrderId id = OrderId.Creator.from(orderId);

        Order order = orderPort.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다: " + orderId));

        order.confirm(operator);

        orderPort.save(order);
    }
}