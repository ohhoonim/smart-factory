package dev.ohhoonim.component.unit.order.application.impl;

import org.springframework.stereotype.Component;
import dev.ohhoonim.component.unit.order.application.OrderPlaceActivity;
import dev.ohhoonim.component.unit.order.application.OrderPlaceRequest;
import dev.ohhoonim.component.unit.order.application.OrderResponse;
import dev.ohhoonim.component.unit.order.model.Order;
import dev.ohhoonim.component.unit.order.model.OrderId;
import dev.ohhoonim.component.unit.order.model.ProductVo;
import dev.ohhoonim.component.unit.order.port.OrderPort;
import dev.ohhoonim.component.unit.order.port.ProductPort;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderPlaceActivityImpl implements OrderPlaceActivity {

    private final OrderPort orderPort;      
    private final ProductPort productPort;  

    @Override
    public OrderResponse execute(OrderPlaceRequest request, String operator) {
        Order order = new Order(
            OrderId.Creator.generate(), 
            request.purchaserId(), 
            operator
        );

        request.items().forEach(itemReq -> {
            ProductVo product = productPort.findById(itemReq.productId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다: " + itemReq.productId()));
            
            order.addItem(product, itemReq.quantity());
        });

        orderPort.save(order);

        return OrderResponse.from(order);
    }
}
