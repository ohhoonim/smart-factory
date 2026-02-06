package dev.ohhoonim.component.unit.order.application;

import java.util.List;

public record OrderPlaceRequest(
    String purchaserId,
    List<OrderItemRequest> items
) {
    
}
